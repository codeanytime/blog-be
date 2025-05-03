package com.blog.security;

import com.blog.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.Base64;

@Component
public class JwtTokenProvider {

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    private final JwtKeyProvider jwtKeyProvider;

    @Autowired
    public JwtTokenProvider(JwtKeyProvider jwtKeyProvider) {
        this.jwtKeyProvider = jwtKeyProvider;
    }

    private SecretKey getSigningKey() {
        return jwtKeyProvider.getSigningKey();
    }

    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.getSubject();
        String role = (String) claims.get("role");

        // Debug the role from token
        System.out.println("Role from JWT token: " + role);

        // Make sure role is not null and handle ROLE_ prefix properly
        String prefixedRole;
        if (role == null || role.isEmpty()) {
            prefixedRole = "ROLE_USER";  // Default role
        } else if (role.startsWith("ROLE_")) {
            prefixedRole = role; // Already has prefix
        } else {
            prefixedRole = "ROLE_" + role; // Add prefix
        }

        System.out.println("Using authority: " + prefixedRole);

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(prefixedRole);

        return new UsernamePasswordAuthenticationToken(email, "", Collections.singletonList(authority));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException |
                 UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
