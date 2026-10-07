package com.sliit.bookstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Expose the "uploads" directory to be accessible via /uploads/**
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        
        if (uploadPath.startsWith("/")) {
            // For Linux/macOS
            registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadPath + "/");
        } else {
            // For Windows
            registry.addResourceHandler("/uploads/**").addResourceLocations("file:///" + uploadPath + "/");
        }
    }
}
