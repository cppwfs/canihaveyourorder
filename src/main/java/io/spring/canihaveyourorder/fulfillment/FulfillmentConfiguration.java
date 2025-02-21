package io.spring.canihaveyourorder.fulfillment;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FulfillmentConfiguration {

    @Bean
    Fulfillment fullfillment(StreamBridge streamBridge) {
        return new Fulfillment(streamBridge);
    }
}
