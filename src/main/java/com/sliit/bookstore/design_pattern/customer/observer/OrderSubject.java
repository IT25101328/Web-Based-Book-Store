package com.sliit.bookstore.design_pattern.customer.observer;

public interface OrderSubject {
    void attach(OrderObserver observer);
    void detach(OrderObserver observer);
    void notifyObservers(Long orderId, String newStatus);
}
