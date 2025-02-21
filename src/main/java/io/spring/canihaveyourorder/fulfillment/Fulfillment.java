package io.spring.canihaveyourorder.fulfillment;

import io.spring.canihaveyourorder.order.Order;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;

public class Fulfillment {

    final private StreamBridge steamBridge;

    public Fulfillment(StreamBridge streamBridge) {
        this.steamBridge = streamBridge;
    }

    public void fulfill(Order order) {
        steamBridge.send("orders", MessageBuilder.withPayload(order));
    }
}
