package com.sliit.bookstore.repository;

import com.sliit.bookstore.model.Notification;
import com.sliit.bookstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCustomerOrderByCreatedDateDesc(User customer);
    long countByCustomerAndIsReadFalse(User customer);
}
