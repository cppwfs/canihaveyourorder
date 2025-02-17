package io.spring.canihaveyourorder.order;

public record OrderItem(String itemName, String size, Integer quantity, Double price) {
}

