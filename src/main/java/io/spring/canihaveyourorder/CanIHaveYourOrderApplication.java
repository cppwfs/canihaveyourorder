package io.spring.canihaveyourorder;

import io.spring.canihaveyourorder.curbside.ChatService;
import io.spring.canihaveyourorder.curbside.SpeechHandler;
import io.spring.canihaveyourorder.fulfillment.Fulfillment;
import io.spring.canihaveyourorder.order.Order;
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
                                        ChatService chatService,
                                        Fulfillment fulfillment) {
        return args -> {
            Platform.startup(() ->
            {
            });
            while (true) {
                System.out.println("Press <Enter> to start Order");
                System.in.read();
                takeOrder(speechHandler, chatService, fulfillment);
            }

        };
    }

    public void takeOrder(SpeechHandler speechHandler, ChatService chatService, Fulfillment fulfillment) {
        String wavAbsolutePath = null;
        try {
            // Capture Order
            wavAbsolutePath = speechHandler.recordAudio("recording.wav");
            String order = speechHandler.speechToText(wavAbsolutePath);
            // Verify the order for customer
            String response = chatService.respond(order, chatService);
            // Retrieve items from order and generate price response
            String orderJson = chatService.getOrderJson(order, chatService);
            String responseTotal = chatService.respondWithTotal(orderJson, chatService);
            // Send Order to order fulfillment
            Order orderObj = chatService.getOrder(orderJson, chatService);
            fulfillment.fulfill(orderObj);
            // Confirm the order and give the price of the order.
            speechHandler.respondViaVoice(response + "\n " + responseTotal, speechHandler, chatService);
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
