package com.example.FoodWaste.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // Fixed, externally-configured signing key so tokens survive app
    // restarts and stay valid across multiple instances of the service.
    // Must be at least 256 bits (32 chars) for HS256.
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:36000000}")
    private long expirationMs;

    private SecretKey signingKey;

    @PostConstruct
    void init() {

        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be configured and at least 32 bytes long");
        }

        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Generate Token
    public String generateToken(String email) {

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + expirationMs)
                )
                .signWith(signingKey)
                .compact();
    }

    // Extract Email
    public String extractEmail(String token) {

        return extractClaims(token).getSubject();
    }

    // Extract Claims
    private Claims extractClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Validate Token
    public boolean validateToken(String token, String email) {

        String extractedEmail = extractEmail(token);

        return extractedEmail.equals(email);
    }
}