package io.spring.canihaveyourorder.order;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private List<OrderItem> orderItems;

    public Order(List<OrderItem> orderItems) {
        setOrderItems(orderItems);
    }
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = new ArrayList<OrderItem>(orderItems);
    }
}
