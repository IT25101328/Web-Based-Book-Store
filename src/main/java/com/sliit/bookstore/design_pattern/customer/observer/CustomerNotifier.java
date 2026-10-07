package com.sliit.bookstore.design_pattern.customer.observer;

import org.springframework.stereotype.Component;

@Component
public class CustomerNotifier implements OrderObserver {
    @Override
    public void update(Long orderId, String newStatus) {
        // In a real app, send email/SMS to customer
        System.out.println("[CustomerNotifier] Customer has been notified: Order #" + orderId + " status changed to " + newStatus);
    }
}
