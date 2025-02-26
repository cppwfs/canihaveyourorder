package io.spring.canihaveyourorder;

import io.spring.canihaveyourorder.curbside.ChatService;
import io.spring.canihaveyourorder.curbside.SpeechHandler;
import javafx.application.Platform;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;

@SpringBootApplication
public class CanIHaveYourOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CanIHaveYourOrderApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(SpeechHandler speechHandler,
                                        ChatService chatService) {
        return args -> {
            Platform.startup(() ->
            {
            });
            while (true) {
                System.out.println("Press <Enter> to start Order");
                System.in.read();
                takeOrder(speechHandler, chatService);
            }

        };
    }

    public void takeOrder(SpeechHandler speechHandler, ChatService chatService) throws Exception{
        String wavAbsolutePath = null;
        try {
            // Capture Order
            String order = speechHandler.recordOrder();
            // Verify the order for customer
            String response = chatService.respond(order, chatService);
            System.out.println(response);
            // Retrieve items from order and generate price response
            // Send Order to order fulfillment
            // Confirm the order and give the price of the order.
            speechHandler.respondViaVoice(response);
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
