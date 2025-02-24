package io.spring.canihaveyourorder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.canihaveyourorder.curbside.ChatService;
import io.spring.canihaveyourorder.curbside.SpeechHandler;
import io.spring.canihaveyourorder.fulfillment.Fulfillment;
import io.spring.canihaveyourorder.order.Order;
import io.spring.canihaveyourorder.order.OrderItem;
import javafx.application.Platform;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class CanIHaveYourOrderApplication {


    public static void main(String[] args) {
        SpringApplication.run(CanIHaveYourOrderApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(SpeechHandler speechHandler,
                                        ChatService chatService,
                                        Fulfillment fulfillment) {
        return args -> {
            try {
                Platform.startup(() ->
                {
                });
                while (true) {
                    System.out.println("Press <Enter> to start Order");
                    System.in.read();
                    String wavAbsolutePath = speechHandler.recordAudio("recording.wav");

                    String order = speechHandler.speechToText( wavAbsolutePath);
                    String response = respond(order, chatService);
                    String orderJson = getOrderJson(order, chatService);
                    String responseTotal = respondWithTotal(orderJson, chatService);
                    Order orderObj = getOrder(orderJson, chatService);
                    fulfillment.fulfill(orderObj);
                    respondViaVoice(response +"\n " + responseTotal, speechHandler, chatService);
                    //TODO: stuff happens
                    //TODO: Send Event to order fullfillment using Pulsar-Binder
                    //TODO: Order fish food and we have no fishfood
                    //TODO: Send Event to Judger
                    //TODO: App sends or updates VectorDB with fishfood
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    String respond(String order, ChatService chatService) {
        return chatService.promptToText(" You are a drive through employee. From the order given, extract the items from the following order and give " +
                        "them a friendly curt acknowledgement confirming their order,  " +
                        "ask them if " +
                        "this order is correct. Also verify that the items ordered are on the menu, do not mention that it is not on the menu unless they order something that is not on the menu, only if they are not.   If you don't understand please let them know. : \"" + order + "\"");
    }

    String getOrderJson(String order, ChatService chatService) {
        String orderItems = chatService.promptToText("From the order given, extract the items from the following order in unformatted JSON in the smallest size possible with the following fields:itemName, size, and quantity:  \"" + order + "\"");
        orderItems = orderItems.substring(8);
        orderItems = orderItems.substring(0,orderItems.length()-4);
        return orderItems;
    }
    Order getOrder(String orderJson, ChatService chatService) {
        ObjectMapper objectMapper = new ObjectMapper();
        Order result = null;
        try {
            List<OrderItem> orderItemsVal = objectMapper.readValue(orderJson, new TypeReference<List<OrderItem>>() {
            });
            result = new Order(orderItemsVal);
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }

    String respondWithTotal(String orderJson, ChatService chatService) {
        return chatService.promptForPrice("Get the total price from the string provided : \"" +
                orderJson + "\"");
    }

    void respondViaVoice(String response, SpeechHandler speechHandler, ChatService chatService) {
        speechHandler.playResponse(speechHandler.textToSpeech(response));
    }
}
