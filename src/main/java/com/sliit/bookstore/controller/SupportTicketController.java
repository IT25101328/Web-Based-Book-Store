package com.sliit.bookstore.controller;

import com.sliit.bookstore.model.SupportTicket;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.repository.UserRepository;
import com.sliit.bookstore.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.sliit.bookstore.service.AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<?> getTickets(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user != null && (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("SUPPORT") || user.getRole().name().equals("OWNER"))) {
            return ResponseEntity.ok(supportTicketService.getAllTickets());
        }
        List<SupportTicket> tickets = supportTicketService.getTicketsForUser(auth.getName());
        return ResponseEntity.ok(tickets);
    }

    @PostMapping
    public ResponseEntity<?> createTicket(Authentication auth, @RequestBody Map<String, String> body) {
        if (auth == null) return ResponseEntity.status(401).build();
        
        String issueCategory = body.get("issueCategory");
        String description = body.get("description");
        Long orderId = null;
        if (body.containsKey("orderId") && !body.get("orderId").trim().isEmpty()) {
            try {
                orderId = Long.parseLong(body.get("orderId"));
            } catch (NumberFormatException ignored) {}
        }
        
        SupportTicket ticket = supportTicketService.createTicket(auth.getName(), issueCategory, orderId, description);
        return ResponseEntity.ok(Map.of("message", "Support ticket created successfully", "ticketId", ticket.getId()));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getTicketDetails(Authentication auth, @PathVariable Long ticketId) {
        if (auth == null) return ResponseEntity.status(401).build();
        try {
            SupportTicket ticket = supportTicketService.getTicketDetails(auth.getName(), ticketId);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{ticketId}/status")
    public ResponseEntity<?> updateTicketStatus(Authentication auth, @PathVariable Long ticketId, @RequestBody Map<String, String> body) {
        if (auth == null) return ResponseEntity.status(401).build();
        String status = body.get("status");
        try {
            SupportTicket ticket = supportTicketService.updateTicketStatus(ticketId, status);
            auditLogService.logAction("UPDATE", "SupportTicket", "Updated status of ticket ID " + ticketId + " to " + status);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<?> deleteTicket(Authentication auth, @PathVariable Long ticketId) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user != null && (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("SUPPORT") || user.getRole().name().equals("OWNER"))) {
            try {
                supportTicketService.deleteTicket(ticketId);
                auditLogService.logAction("DELETE", "SupportTicket", "Deleted ticket ID " + ticketId);
                return ResponseEntity.ok(Map.of("message", "Ticket deleted successfully"));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
            }
        }
        return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
    }
}
