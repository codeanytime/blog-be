package com.blog.controller;

import com.blog.dto.AuthResponse;
import com.blog.dto.UserDTO;
import com.blog.model.User;
import com.blog.repository.UserRepository;
import com.blog.service.GoogleAuthService;
import com.blog.service.JwtService;
import com.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> handleOAuth2Success(
            @AuthenticationPrincipal OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String pictureUrl = oauth2User.getAttribute("picture");

        User user = userService.createOrUpdateGoogleUser(
                oauth2User.getName(),
                name,
                email,
                pictureUrl
        );

        String token = jwtService.generateToken(email);
        UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPictureUrl(), user.getRole());

        return ResponseEntity.ok(new AuthResponse(token, userDTO, null));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> authenticateWithGoogle(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        if (idToken == null) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, "ID token is required"));
        }

        try {
            AuthResponse response = googleAuthService.authenticateWithGoogle(idToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, e.getMessage()));
        }
    }

    @PostMapping("/test-login")
    public ResponseEntity<AuthResponse> testLogin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, "Email is required"));
        }

        try {
            // Find the user by email
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String token = jwtService.generateToken(user.getEmail());

                return ResponseEntity.ok(new AuthResponse(
                        token,
                        new UserDTO(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getPictureUrl(),
                                user.getRole()
                        ),
                        null
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, null, "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String email = authentication.getName();
        UserDTO user = userService.getUserByEmail(email);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, "Username and password are required"));
        }

        try {
            // Find user by username
            Optional<User> userOpt = userRepository.findByUsername(username);

            // If not found by username, try by email
            if (!userOpt.isPresent()) {
                userOpt = userRepository.findByEmail(username);
            }

            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, null, "Invalid username or password"));
            }

            User user = userOpt.get();

            // Verify password
            if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, null, "Invalid username or password"));
            }

            // Generate token
            String token = jwtService.generateToken(user.getEmail());

            // Create user DTO for response
            UserDTO userDTO = new UserDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPictureUrl(),
                    user.getRole()
            );

            return ResponseEntity.ok(new AuthResponse(token, userDTO, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, null, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT is stateless, so no server-side logout is needed
        // The client should remove the token
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String name = request.get("name");

        if (username == null || email == null || password == null || name == null) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, "All fields are required"));
        }

        try {
            // Check if user with this email or username already exists
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(new AuthResponse(null, null, "Email already in use"));
            }

            if (userRepository.findByUsername(username).isPresent()) {
                return ResponseEntity.badRequest().body(new AuthResponse(null, null, "Username already in use"));
            }

            // Create the new user
            User user = userService.createUser(username, name, email, password);

            // Generate token
            String token = jwtService.generateToken(user.getEmail());

            // Create user DTO for response
            UserDTO userDTO = new UserDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPictureUrl(),
                    user.getRole()
            );

            return ResponseEntity.ok(new AuthResponse(token, userDTO, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, null, e.getMessage()));
        }
    }
}