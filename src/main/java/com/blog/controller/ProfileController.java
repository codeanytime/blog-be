package com.blog.controller;

import com.blog.dto.PasswordChangeRequest;
import com.blog.dto.ProfileUpdateRequest;
import com.blog.dto.UserDTO;
import com.blog.model.User;
import com.blog.security.JwtAuthenticationFilter;
import com.blog.service.S3Service;
import com.blog.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private S3Service s3Service;

    /**
     * Get the current user's profile information
     */
    @GetMapping
    public ResponseEntity<UserDTO> getProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findUserByEmail(principal.getName());
        UserDTO userDTO = userService.convertToDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Update the current user's profile information
     */
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest updateRequest, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Get the current user
            User currentUser = userService.findUserByEmail(principal.getName());

            // Check if the username or email already exists (and it's not the current user's)
            if (updateRequest.getUsername() != null &&
                    !updateRequest.getUsername().equals(currentUser.getUsername()) &&
                    userService.existsByUsername(updateRequest.getUsername())) {
                return ResponseEntity.badRequest().body("Username already exists");
            }

            if (updateRequest.getEmail() != null &&
                    !updateRequest.getEmail().equals(currentUser.getEmail()) &&
                    userService.existsByEmail(updateRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            // Update the user
            User updatedUser = userService.updateUserProfile(currentUser.getId(), updateRequest);
            UserDTO userDTO = userService.convertToDTO(updatedUser);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating profile: " + e.getMessage());
        }
    }

    /**
     * Change the current user's password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest passwordRequest, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Get the current user
            User currentUser = userService.findUserByEmail(principal.getName());

            // Special case for admin user with hardcoded password
            boolean isCorrectPassword = false;

            if (currentUser.getUsername() != null && currentUser.getUsername().equals("admin") &&
                    passwordRequest.getCurrentPassword().equals("admin123456@")) {
                // Admin user with special hardcoded password
                isCorrectPassword = true;
                logger.info("Admin user changing password with special case password");
            } else {
                // Regular password verification
                isCorrectPassword = passwordEncoder.matches(passwordRequest.getCurrentPassword(), currentUser.getPassword());
            }

            if (!isCorrectPassword) {
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }

            // Update the password
            User updatedUser = userService.updateUserPassword(currentUser.getId(), passwordRequest.getNewPassword());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error changing password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error changing password: " + e.getMessage());
        }
    }

    /**
     * Upload a new avatar for the current user
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Get the current user
            User currentUser = userService.findUserByEmail(principal.getName());

            // Check if S3 is configured
            if (!s3Service.isConfigured()) {
                // Use a default avatar service when S3 is not available
                String avatarUrl = "https://ui-avatars.com/api/?name=" +
                        currentUser.getName().replace(" ", "+") +
                        "&background=random&size=200";

                User updatedUser = userService.updateUserAvatar(currentUser.getId(), avatarUrl);

                Map<String, String> response = new HashMap<>();
                response.put("url", avatarUrl);
                response.put("message", "Avatar updated using default service");
                return ResponseEntity.ok(response);
            }

            // Upload to S3
            String avatarUrl = s3Service.uploadFile(file, "avatars");

            // Delete old avatar if it exists
            if (currentUser.getPictureUrl() != null && currentUser.getPictureUrl().contains(s3Service.getBucketName())) {
                try {
                    s3Service.deleteFile(currentUser.getPictureUrl());
                } catch (Exception e) {
                    logger.warn("Could not delete old avatar: {}", e.getMessage());
                }
            }

            // Update user with new avatar
            User updatedUser = userService.updateUserAvatar(currentUser.getId(), avatarUrl);

            Map<String, String> response = new HashMap<>();
            response.put("url", avatarUrl);
            response.put("message", "Avatar uploaded successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("Error uploading avatar", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading avatar: " + e.getMessage());
        }
    }

    /**
     * Delete the current user's avatar
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<?> deleteAvatar(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Get the current user
            User currentUser = userService.findUserByEmail(principal.getName());

            // Delete the current avatar if it's in S3
            if (currentUser.getPictureUrl() != null &&
                    s3Service.isConfigured() &&
                    currentUser.getPictureUrl().contains(s3Service.getBucketName())) {
                try {
                    s3Service.deleteFile(currentUser.getPictureUrl());
                } catch (Exception e) {
                    logger.warn("Could not delete avatar: {}", e.getMessage());
                }
            }

            // Set default avatar
            String defaultAvatarUrl = "https://ui-avatars.com/api/?name=" +
                    currentUser.getName().replace(" ", "+") +
                    "&background=random&size=200";

            User updatedUser = userService.updateUserAvatar(currentUser.getId(), defaultAvatarUrl);

            Map<String, String> response = new HashMap<>();
            response.put("url", defaultAvatarUrl);
            response.put("message", "Avatar removed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting avatar", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting avatar: " + e.getMessage());
        }
    }
}