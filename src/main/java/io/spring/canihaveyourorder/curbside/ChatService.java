package io.spring.canihaveyourorder.curbside;

import io.spring.canihaveyourorder.order.OrderPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;


/**
 * Provides chat responses based on the prompts provided.
 */
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatModel chatModel;

    private VectorStore vectorStore;

    ChatService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    public String promptToText(String prompt) {
        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .build();
        ChatClient chatClient = ChatClient.create(chatModel);
        ChatResponse chatResponse = chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
                .user(prompt)
                .call()
                .chatResponse();
        String result =  chatResponse.getResult().getOutput().getText();
        log.info(result);
        return result;
    }
    public String promptForPrice(String prompt) {
        ChatClient chatClient = ChatClient.create(chatModel);
        ChatResponse chatResponse = chatClient.prompt()
                .user(prompt)
                .tools(new OrderPriceService())
                .call()
                .chatResponse();
        String result =  chatResponse.getResult().getOutput().getText();
        log.info(result);
        return result;
    }

}
