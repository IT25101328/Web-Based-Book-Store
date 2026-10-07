package com.sliit.bookstore.design_pattern.customer.observer;

public interface OrderObserver {
    void update(Long orderId, String newStatus);
}
