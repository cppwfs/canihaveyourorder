package io.spring.canihaveyourorder.curbside;

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
                .call()
                .chatResponse();
        String result =  chatResponse.getResult().getOutput().getText();
        log.info(result);
        return result;
    }

    public String respond(String order, ChatService chatService) {
        return chatService.promptToText(" You are a drive through employee. From the order given, extract the items from the following order and give " +
                "them a friendly curt acknowledgement confirming their order,  " +
                "ask them if " +
                "this order is correct. Also verify that the items ordered are on the menu, do not mention that it is not on the menu unless they order something that is not on the menu, only if they are not.   If you don't understand please let them know. : \"" + order + "\"");
    }

    public String getOrderJson(String order, ChatService chatService) {
        String orderItems = chatService.promptToText("From the order given, extract the items from the following order in unformatted JSON in the smallest size possible with the following fields:itemName, size, and quantity:  \"" + order + "\"");
        orderItems = orderItems.substring(8);
        orderItems = orderItems.substring(0,orderItems.length()-4);
        return orderItems;
    }

    public String respondWithTotal(String orderJson, ChatService chatService) {
        return chatService.promptForPrice("Get the total price from the string provided : \"" +
                orderJson + "\"");
    }
}
