package com.sliit.bookstore.design_pattern.support.facade;

import org.springframework.stereotype.Component;

@Component
public class SupportTicketFacade {
    // In a real application, this facade would inject and coordinate multiple services:
    // private TicketService ticketService;
    // private EmailNotificationService emailService;
    // private AuditLogService auditService;

    public void createAndProcessTicket(String username, String issueCategory, String description) {
        System.out.println("[Facade] Simplifying ticket creation for Customer Service Supervisor...");
        
        // 1. Create Ticket
        System.out.println("[Facade] Creating ticket for " + username);
        
        // 2. Send Notification
        System.out.println("[Facade] Sending notification email for ticket creation");
        
        // 3. Log Audit
        System.out.println("[Facade] Logging ticket creation in audit system");
    }
}
