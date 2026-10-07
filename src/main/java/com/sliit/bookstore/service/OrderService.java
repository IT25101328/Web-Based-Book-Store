package com.sliit.bookstore.service;

import com.sliit.bookstore.dto.OrderRequest;
import com.sliit.bookstore.exception.ResourceNotFoundException;
import com.sliit.bookstore.model.Book;
import com.sliit.bookstore.model.Order;
import com.sliit.bookstore.model.OrderItem;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.repository.BookRepository;
import com.sliit.bookstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookRepository bookRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public Order createOrder(OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentReceipt(request.getPaymentReceipt());
        order.setAddress(request.getAddress());
        order.setPhone(request.getPhone());

        BigDecimal total = BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + itemReq.getBookId()));

            OrderItem item = new OrderItem();
            item.setBook(book);
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(book.getPrice());
            item.setOrder(order);
            order.getItems().add(item);

            total = total.add(book.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));

            // Reduce stock
            book.setStock(Math.max(0, book.getStock() - itemReq.getQuantity()));
            bookRepository.save(book);
        }

        BigDecimal discount = BigDecimal.ZERO;
        for (int i = 1; i <= 10; i++) {
            int cutoff = i * 2000;
            int couponDiscount = 100 + (i * 50);
            if (total.compareTo(BigDecimal.valueOf(cutoff)) >= 0) {
                discount = discount.add(BigDecimal.valueOf(couponDiscount));
            }
        }
        total = total.subtract(discount);

        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    public Order updateStatus(Long id, String newStatus) {
        Order order = getOrderById(id);
        try {
            order.setStatus(Order.OrderStatus.valueOf(newStatus.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public BigDecimal getTotalRevenue() {
        return orderRepository.getTotalRevenue();
    }

    public long countByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public long countAllOrders() {
        return orderRepository.count();
    }

    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }
}
