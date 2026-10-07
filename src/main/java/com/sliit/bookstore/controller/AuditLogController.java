package com.sliit.bookstore.controller;

import com.sliit.bookstore.model.AuditLog;
import com.sliit.bookstore.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public List<AuditLog> getRecentLogs() {
        return auditLogService.getRecentLogs();
    }



    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<?> deleteLog(@org.springframework.web.bind.annotation.PathVariable Long id) {
        try {
            auditLogService.deleteLog(id);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Log deleted successfully"));
        } catch (RuntimeException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
