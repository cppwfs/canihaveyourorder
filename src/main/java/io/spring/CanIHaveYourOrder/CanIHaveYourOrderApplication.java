package io.spring.CanIHaveYourOrder;

import javafx.application.Platform;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CanIHaveYourOrderApplication {


    public static void main(String[] args) {
        SpringApplication.run(CanIHaveYourOrderApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(SpeechHandler speechHandler,
                                        ChatService chatService,
                                        VectorStore vectorStore) {
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
                    String response = respond(order, chatService, vectorStore);
                    respondViaVoice(response, speechHandler, chatService);
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



    String respond(String order, ChatService chatService, VectorStore vectorStore) {
        chatService.promptToText("From the order given, extract the items from the following order in pretty print JSON format : \"" + order + "\"", vectorStore);

        return chatService.promptToText("From the order given, extract the items from the following order and give " +
                        "them a friendly curt acknowledgement confirming their order, in the voice of a nasally drive through " +
                        "employee at a fast food restaurant ask them if " +
                        "this order is correct. If an item the ordered isn't on the menu tell them so. If you don't understand please let them know. : \"" + order + "\"", vectorStore);
    }

    void respondViaVoice(String response, SpeechHandler speechHandler, ChatService chatService) {
        speechHandler.playResponse(speechHandler.textToSpeech(response));
    }
}
