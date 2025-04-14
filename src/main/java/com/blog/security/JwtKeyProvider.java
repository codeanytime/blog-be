package com.blog.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Singleton provider for JWT signing key to ensure consistency across services
 */
@Component
public class JwtKeyProvider {

    private static SecretKey signingKey;

    /**
     * Get a properly sized secret key for JWT signing
     * This ensures the same key is used across different services
     */
    public SecretKey getSigningKey() {
        if (signingKey == null) {
            // Create a new secure key for HS512 (which requires at least 512 bits)
            signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }
        return signingKey;
    }
}