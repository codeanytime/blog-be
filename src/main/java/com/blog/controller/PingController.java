package com.blog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple controller to check if the backend is alive
 */
@RestController
@RequestMapping("/api/ping")
public class PingController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Backend is running");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "Blog Platform API");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> details() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Backend details");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "Blog Platform API");
        response.put("version", "1.0.0");
        response.put("environment", System.getenv("SPRING_PROFILES_ACTIVE") != null
                ? System.getenv("SPRING_PROFILES_ACTIVE")
                : "default");

        // Add runtime information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("max", runtime.maxMemory());

        response.put("system", memory);

        return ResponseEntity.ok(response);
    }
}