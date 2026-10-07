package com.sliit.bookstore.controller;

import com.sliit.bookstore.dto.OrderRequest;
import com.sliit.bookstore.model.Notification;
import com.sliit.bookstore.model.Order;

import com.sliit.bookstore.repository.UserRepository;
import com.sliit.bookstore.service.OrderService;
import com.sliit.bookstore.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.sliit.bookstore.repository.OrderRepository orderRepository;

    @Autowired
    private com.sliit.bookstore.service.AuditLogService auditLogService;
    
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<Order> getAllOrders() {
        auditLogService.logAction("VIEW", "Order", "Viewed list of all orders");
        return orderService.getAllOrders();
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        return userRepository.findByUsername(auth.getName())
                .map(user -> ResponseEntity.ok(orderService.getOrdersByUser(user)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PostMapping("/upload-receipt")
    public ResponseEntity<Map<String, Object>> uploadReceipt(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        if (file.isEmpty()) {
            response.put("error", "Please select a file to upload.");
            return ResponseEntity.badRequest().body(response);
        }
        
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("application/pdf"))) {
            response.put("error", "Only JPG, PNG, or PDF files are allowed.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Path uploadPath = Paths.get("uploads/receipts/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            response.put("message", "Receipt uploaded successfully.");
            response.put("fileName", uniqueFileName);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            response.put("error", "Failed to upload receipt: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping
    public Order createOrder(Authentication auth, @RequestBody OrderRequest orderRequest) {
        Order order = orderService.createOrder(orderRequest);
        if (auth != null) {
            userRepository.findByUsername(auth.getName()).ifPresent(user -> {
                order.setUser(user);
                orderRepository.save(order);
                
                // Trigger Order Placed Notification
                notificationService.createNotification(
                    user,
                    "📦 Order Placed",
                    "Your order #ORD" + String.format("%03d", order.getId()) + " has been placed successfully.",
                    Notification.NotificationType.ORDER_PLACED
                );
                
                // Trigger Payment Confirmed immediately as per plan to fulfill the requirement
                notificationService.createNotification(
                    user,
                    "💳 Payment Confirmed",
                    "Payment for order #ORD" + String.format("%03d", order.getId()) + " has been confirmed.",
                    Notification.NotificationType.PAYMENT_CONFIRMED
                );
            });
        }
        return order;
    }

    @PutMapping("/{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        Order updated = orderService.updateStatus(id, newStatus);
        
        if (updated.getUser() != null) {
            if ("SHIPPED".equalsIgnoreCase(newStatus)) {
                notificationService.createNotification(
                    updated.getUser(),
                    "🚚 Shipment Sent",
                    "Your order #ORD" + String.format("%03d", updated.getId()) + " has been shipped.",
                    Notification.NotificationType.SHIPMENT_SENT
                );
            } else if ("DELIVERED".equalsIgnoreCase(newStatus)) {
                notificationService.createNotification(
                    updated.getUser(),
                    "✅ Delivered",
                    "Your order #ORD" + String.format("%03d", updated.getId()) + " has been delivered.",
                    Notification.NotificationType.DELIVERED
                );
            } else if ("CANCELLED".equalsIgnoreCase(newStatus)) {
                notificationService.createNotification(
                    updated.getUser(),
                    "❌ Order Cancelled",
                    "Your order #ORD" + String.format("%03d", updated.getId()) + " has been cancelled.",
                    Notification.NotificationType.CANCELLED
                );
            }
        }
        
        auditLogService.logAction("UPDATE", "Order", "Updated status of order ID " + id + " to " + newStatus);
        return updated;
    }

    @PutMapping("/{id}/receipt")
    public ResponseEntity<?> attachReceipt(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String fileName = body.get("fileName");
        Order order = orderService.getOrderById(id);
        order.setPaymentReceipt(fileName);
        orderRepository.save(order);
        auditLogService.logAction("UPDATE", "Order", "Customer uploaded receipt for order ID " + id);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        auditLogService.logAction("DELETE", "Order", "Deleted order ID " + id);
        return ResponseEntity.ok(Map.of("message", "Order deleted successfully"));
    }
}

