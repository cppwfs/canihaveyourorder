package io.spring.canihaveyourorder.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public class OrderPriceService {

    private static final double PRICE_MULTIPLIER = 2.0;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool(description = "Get the total price from the string provided")
    Double priceOrder(String orderItems) throws JsonProcessingException {
        List<OrderItem> orderItemsVal = objectMapper.readValue(orderItems, new TypeReference<>() {
        });

        // Calculate total price using streams
        return orderItemsVal.stream()
                .mapToDouble(item -> item.quantity() * PRICE_MULTIPLIER)
                .sum();
    }
}
