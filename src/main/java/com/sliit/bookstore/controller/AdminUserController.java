package com.sliit.bookstore.controller;

import com.sliit.bookstore.model.Role;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.sliit.bookstore.service.AuditLogService auditLogService;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByIdDesc();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Username is already taken!"));
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Email is already in use!"));
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Allow any role except ADMIN or OWNER to be assigned by admin
        if (user.getRole() == null || user.getRole() == Role.ADMIN || user.getRole() == Role.OWNER) {
            user.setRole(Role.CUSTOMER);
        }
        
        userRepository.save(user);
        auditLogService.logAction("ADD", "User", "Created new user: " + user.getUsername());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            // Do not allow escalation to ADMIN or OWNER through this endpoint
            if (userDetails.getRole() != null && userDetails.getRole() != Role.ADMIN && userDetails.getRole() != Role.OWNER) {
                user.setRole(userDetails.getRole());
            }
            if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            userRepository.save(user);
            auditLogService.logAction("UPDATE", "User", "Updated user ID: " + id);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            if (user.getRole() == Role.OWNER) {
                return ResponseEntity.badRequest().body(Map.of("message", "Error: Cannot delete OWNER account!"));
            }

            // Manually clear dependencies to prevent Foreign Key constraint failures
            jdbcTemplate.update("DELETE FROM notifications WHERE customer_id = ?", id);
            jdbcTemplate.update("DELETE FROM support_messages WHERE ticket_id IN (SELECT ticket_id FROM support_tickets WHERE customer_id = ?)", id);
            jdbcTemplate.update("DELETE FROM support_tickets WHERE customer_id = ?", id);
            jdbcTemplate.update("DELETE FROM cart_items WHERE cart_id IN (SELECT cart_id FROM carts WHERE customer_id = ?)", id);
            jdbcTemplate.update("DELETE FROM carts WHERE customer_id = ?", id);
            jdbcTemplate.update("DELETE FROM wishlist_items WHERE wishlist_id IN (SELECT id FROM wishlists WHERE customer_id = ?)", id);
            jdbcTemplate.update("DELETE FROM wishlists WHERE customer_id = ?", id);
            jdbcTemplate.update("DELETE FROM password_reset_token WHERE user_id = ?", id);
            jdbcTemplate.update("UPDATE orders SET user_id = NULL WHERE user_id = ?", id);

            userRepository.delete(user);
            auditLogService.logAction("DELETE", "User", "Deleted user ID: " + id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
