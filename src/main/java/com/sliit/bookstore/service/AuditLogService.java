package com.sliit.bookstore.service;

import com.sliit.bookstore.model.AuditLog;
import com.sliit.bookstore.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAction(String actionType, String entityName, String details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser"))
                ? auth.getName()
                : "SYSTEM";

        // Only log actions for ADMINs or staff if you want to filter, 
        // but for now we log whoever hits an admin-restricted endpoint.
        AuditLog log = new AuditLog(username, actionType, entityName, details);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }



    public void deleteLog(Long id) {
        auditLogRepository.deleteById(id);
    }
}
