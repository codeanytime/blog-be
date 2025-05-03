package com.blog.controller;

import com.blog.model.Post;
import com.blog.model.User;
import com.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class TestController {

    @Autowired
    private PostService postService;

    @GetMapping("/api/test-connection")
    public Map<String, Object> testConnection() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Spring Boot backend is connected and running!");
        response.put("timestamp", System.currentTimeMillis());

        // Add basic server info that doesn't require database access
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("osName", System.getProperty("os.name"));

        // We'll avoid database access for now to prevent errors
        response.put("databaseTest", "skipped");

        return response;
    }

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Spring Boot Blog Platform API");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * Public endpoint to get basic system status
     */
    @GetMapping("/api/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Blog Platform API");
        status.put("timestamp", System.currentTimeMillis());

        // Additional environment information (safe to expose)
        Map<String, String> env = new HashMap<>();
        env.put("javaVersion", System.getProperty("java.version"));
        env.put("osName", System.getProperty("os.name"));

        status.put("environment", env);

        return ResponseEntity.ok(status);
    }
}