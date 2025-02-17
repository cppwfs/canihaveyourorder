package io.spring.canihaveyourorder.curbside;

import io.spring.canihaveyourorder.order.OrderPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;


/**
 * Provides chat responses based on the prompts provided.
 */
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatModel chatModel;

    ChatService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String promptToText(String prompt) {
        ChatClient chatClient = ChatClient.create(chatModel);
        ChatResponse chatResponse = chatClient.prompt()
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
