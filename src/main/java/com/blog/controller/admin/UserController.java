package com.blog.controller.admin;

import com.blog.dto.UserDTO;
import com.blog.model.User;
import com.blog.repository.UserRepository;
import com.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * Get a user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }
    }

    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            // Check if username already exists
            if (userRepository.existsByUsername(userDTO.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Username already exists"));
            }

            // Check if email already exists
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Email already exists"));
            }

            User newUser = new User();
            newUser.setName(userDTO.getName());
            newUser.setEmail(userDTO.getEmail());
            newUser.setUsername(userDTO.getUsername());
            newUser.setRole(userDTO.getRole());

            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                // Hash password before saving
                newUser.setPassword(userService.encodePassword(userDTO.getPassword()));
            }

            User savedUser = userRepository.save(newUser);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error creating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Update a user
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found"));
            }

            User user = userOpt.get();

            // Check if username already exists (and is not the same user)
            if (userDTO.getUsername() != null && !userDTO.getUsername().equals(user.getUsername())
                    && userRepository.existsByUsername(userDTO.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Username already exists"));
            }

            // Check if email already exists (and is not the same user)
            if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())
                    && userRepository.existsByEmail(userDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Email already exists"));
            }

            // Update fields if they are provided
            if (userDTO.getName() != null) {
                user.setName(userDTO.getName());
            }

            if (userDTO.getEmail() != null) {
                user.setEmail(userDTO.getEmail());
            }

            if (userDTO.getUsername() != null) {
                user.setUsername(userDTO.getUsername());
            }

            if (userDTO.getRole() != null) {
                user.setRole(userDTO.getRole());
            }

            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                // Hash password before saving
                user.setPassword(userService.encodePassword(userDTO.getPassword()));
            }

            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error updating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Delete a user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (!userRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found"));
            }

            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error deleting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
