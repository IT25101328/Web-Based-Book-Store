package com.sliit.bookstore.controller;

import com.sliit.bookstore.service.BookService;
import com.sliit.bookstore.service.OrderService;
import com.sliit.bookstore.service.SupportTicketService;
import com.sliit.bookstore.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private BookService bookService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        auditLogService.logAction("VIEW", "Dashboard", "Viewed dashboard statistics");
        return Map.of(
            "totalBooks",      bookService.countBooks(),
            "totalOrders",     orderService.countAllOrders(),
            "totalRevenue",    orderService.getTotalRevenue(),
            "pendingOrders",   orderService.countByStatus(com.sliit.bookstore.model.Order.OrderStatus.PENDING),
            "openTickets",     supportTicketService.countOpenTickets()
        );
    }
}
