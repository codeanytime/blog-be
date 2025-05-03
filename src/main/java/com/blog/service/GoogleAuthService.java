package com.blog.service;

import com.blog.dto.AuthResponse;
import com.blog.dto.UserDTO;
import com.blog.model.User;
import com.blog.security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${google.clientId}")
    private String googleClientId;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public AuthResponse authenticateWithGoogle(String idTokenString) {
        try {
            // Check if Google Client ID is configured
            if (googleClientId == null || googleClientId.isEmpty()) {
                throw new IllegalStateException("Google Client ID is not configured");
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid ID token");
            }

            Payload payload = idToken.getPayload();

            // Get user identity
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            // Create or update user in our database
            User user = userService.createOrUpdateGoogleUser(googleId, name, email, pictureUrl);

            // Generate JWT token
            String token = tokenProvider.generateToken(user.getEmail());

            // Convert user to DTO
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setName(user.getName());
            userDTO.setEmail(user.getEmail());
            userDTO.setPictureUrl(user.getPictureUrl());
            userDTO.setRole(user.getRole());
            userDTO.setRoles(new String[]{user.getRole()});

            return AuthResponse.fromUser(token, user, null);
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage(), e);
        }
    }
}
