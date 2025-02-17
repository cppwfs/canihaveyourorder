package io.spring.canihaveyourorder.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public class OrderPriceService {
    @Tool(description = "Get the total price from the string provided")
    Double priceOrder(String orderItems) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        double result = 0.0;
        try {
            List<OrderItem> orderItemsVal = objectMapper.readValue(orderItems, new TypeReference<List<OrderItem>>() {
            });

            for (OrderItem item : orderItemsVal) {
                result += item.quantity() * 2.0;
            }
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
