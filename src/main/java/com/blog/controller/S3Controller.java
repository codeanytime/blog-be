package com.blog.controller;

import com.blog.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/s3")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> getS3Status() {
        Map<String, Object> response = new HashMap<>();
        boolean isS3Available = s3Service.isS3Available();
        response.put("s3Available", isS3Available);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Check if S3 is available
            if (!s3Service.isS3Available()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "AWS S3 is not configured. Please provide AWS credentials.");
                return ResponseEntity.status(503).body(response);
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Only image files are allowed");
                return ResponseEntity.badRequest().body(response);
            }

            // Upload the file to S3
            String fileUrl = s3Service.uploadFile(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
