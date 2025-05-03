package com.blog.controller;

import com.blog.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getS3Status() {
        Map<String, Object> response = new HashMap<>();
        boolean isS3Available = imageService.isS3Available();
        response.put("s3Available", isS3Available);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            // Check if S3 is available
            if (!imageService.isS3Available()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "AWS S3 is not configured. Please provide AWS credentials.");
                return ResponseEntity.status(503).body(response);
            }

            String imageUrl = imageService.uploadImage(file);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
