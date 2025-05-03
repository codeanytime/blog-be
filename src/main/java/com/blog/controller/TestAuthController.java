package com.blog.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for testing authentication and authorization
 * These endpoints are intentionally not protected by security to help debug auth issues
 */
@RestController
@RequestMapping("/api/test-auth")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class TestAuthController {

    /**
     * Returns detailed information about the current authentication context
     * Useful for debugging authentication issues
     */
    @GetMapping("/debug")
    public Map<String, Object> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> debugInfo = new HashMap<>();

        if (auth == null) {
            debugInfo.put("authenticated", false);
            debugInfo.put("message", "No authentication found");
            return debugInfo;
        }

        debugInfo.put("authenticated", auth.isAuthenticated());
        debugInfo.put("name", auth.getName());
        debugInfo.put("principal", auth.getPrincipal());
        debugInfo.put("principalType", auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");
        debugInfo.put("credentials", auth.getCredentials());
        debugInfo.put("authType", auth.getClass().getName());

        // Extract authorities
        debugInfo.put("authorities", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // Check specific roles
        boolean hasAdminRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean hasUserRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        debugInfo.put("hasAdminRole", hasAdminRole);
        debugInfo.put("hasUserRole", hasUserRole);

        // Check for malformed role prefixes
        boolean hasUnprefixedAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        boolean hasUnprefixedUser = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER"));

        debugInfo.put("hasUnprefixedAdmin", hasUnprefixedAdmin);
        debugInfo.put("hasUnprefixedUser", hasUnprefixedUser);

        return debugInfo;
    }

    /**
     * Test endpoint that requires ADMIN role
     * Will return success only if user has proper ADMIN role
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminOnly() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "You successfully accessed an admin-only endpoint!");
        response.put("status", "success");
        return response;
    }

    /**
     * Test endpoint that requires USER role
     * Will return success only if user has proper USER role
     */
    @GetMapping("/user-only")
    @PreAuthorize("hasRole('USER')")
    public Map<String, String> userOnly() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "You successfully accessed a user-only endpoint!");
        response.put("status", "success");
        return response;
    }

    /**
     * Test endpoint that requires authentication but no specific role
     */
    @GetMapping("/authenticated")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> authenticatedOnly() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "You are authenticated!");
        response.put("status", "success");
        return response;
    }

    /**
     * Public test endpoint that doesn't require authentication
     */
    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a public endpoint");
        response.put("status", "success");
        return response;
    }
}