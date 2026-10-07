package com.sliit.bookstore.design_pattern.owner.singleton;

import java.time.LocalDateTime;

public class AuditLoggerSingleton {
    private static AuditLoggerSingleton instance;

    // Private constructor prevents instantiation
    private AuditLoggerSingleton() { }

    // Thread-safe singleton implementation
    public static synchronized AuditLoggerSingleton getInstance() {
        if (instance == null) {
            instance = new AuditLoggerSingleton();
        }
        return instance;
    }

    public void logActivity(String adminUser, String action) {
        System.out.println("[AUDIT - " + LocalDateTime.now() + "] " + adminUser + ": " + action);
        // Could also write to file or trigger AuditLogService
    }
}
