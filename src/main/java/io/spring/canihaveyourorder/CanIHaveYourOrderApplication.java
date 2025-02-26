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
                int input = System.in.read();
                if (input == 'q') {
                    System.exit(0);
                }
                if (input == 'd') {
                    String order = "I want dogfood, catfood, and fish Food.";
                    takeOrder(speechHandler, chatService, order);
                    System.in.read();
                }
                else {
                    takeOrder(speechHandler, chatService, null);
                }
            }

        };
    }

    public void takeOrder(SpeechHandler speechHandler, ChatService chatService, String order) {
        String wavAbsolutePath = null;
        try {
            // Capture Order if one not provided
            if (order == null) {
                wavAbsolutePath = speechHandler.recordAudio("recording.wav");
                order = speechHandler.speechToText(wavAbsolutePath);
            }
            // Verify the order for customer
            String response = chatService.respond(order);
            System.out.println(response);
            speechHandler.respondViaVoice(response);
            // TODO Retrieve items from order and generate price response
            // TODO Confirm the order and give the price of the order.
            // TODO Send order to fulfillment
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
