package io.spring.canihaveyourorder;

import io.spring.canihaveyourorder.curbside.ChatService;
import javafx.application.Platform;

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
    ApplicationRunner applicationRunner(ChatService chatService) {
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
                    String order = "I want dogfood, catfood, and fish Food.";
                    takeOrder(chatService, order);
                    System.in.read();
            }

        };
    }

    public void takeOrder(ChatService chatService, String order) throws Exception {
            // Capture Order if one not provided

            // Verify the order for customer
            String response = chatService.respond(order);
            System.out.println(response);
            // TODO Retrieve items from order and generate price response
            // TODO Confirm the order and give the price of the order.
            // TODO Send order to fulfillment
    }
}
