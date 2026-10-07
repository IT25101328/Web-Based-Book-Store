package com.sliit.bookstore.controller;

import com.sliit.bookstore.model.Notification;
import com.sliit.bookstore.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        List<Notification> notifications = notificationService.getUserNotifications(auth.getName());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Authentication auth) {
        if (auth == null) return ResponseEntity.ok(Map.of("count", 0));
        long count = notificationService.getUnreadCount(auth.getName());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(Authentication auth, @PathVariable Long id) {
        if (auth == null) return ResponseEntity.status(401).build();
        try {
            notificationService.markAsRead(id, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(Authentication auth, @PathVariable Long id) {
        if (auth == null) return ResponseEntity.status(401).build();
        try {
            notificationService.deleteNotification(id, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Notification deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
