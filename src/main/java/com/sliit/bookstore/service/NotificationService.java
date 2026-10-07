package com.sliit.bookstore.service;

import com.sliit.bookstore.model.Notification;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.repository.NotificationRepository;
import com.sliit.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void createNotification(User user, String title, String message, Notification.NotificationType type) {
        if (user == null) return;
        Notification notification = new Notification();
        notification.setCustomer(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(type);
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return notificationRepository.findByCustomerOrderByCreatedDateDesc(user);
    }

    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return notificationRepository.countByCustomerAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long id, String username) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (notification.getCustomer().getId().equals(user.getId())) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void deleteNotification(Long id, String username) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (notification.getCustomer().getId().equals(user.getId())) {
            notificationRepository.delete(notification);
        }
    }
}
