package com.sliit.bookstore.repository;

import com.sliit.bookstore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != 'CANCELLED'")
    BigDecimal getTotalRevenue();

    long countByStatus(Order.OrderStatus status);

    java.util.List<Order> findByUserOrderByCreatedAtDesc(com.sliit.bookstore.model.User user);
}
