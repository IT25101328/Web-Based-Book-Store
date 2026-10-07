package com.sliit.bookstore.design_pattern.customer.observer;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusManager implements OrderSubject {
    private List<OrderObserver> observers = new ArrayList<>();

    @Override
    public void attach(OrderObserver observer) {
        observers.add(observer);
    }

    @Override
    public void detach(OrderObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Long orderId, String newStatus) {
        for (OrderObserver observer : observers) {
            observer.update(orderId, newStatus);
        }
    }
    
    public void changeOrderStatus(Long orderId, String status) {
        // ... DB update logic ...
        System.out.println("Order #" + orderId + " status updated to " + status);
        notifyObservers(orderId, status);
    }
}
