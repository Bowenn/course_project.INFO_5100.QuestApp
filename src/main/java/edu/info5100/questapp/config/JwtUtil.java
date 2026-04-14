package edu.info5100.questapp.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT utility class for generating and validating JWT tokens.
 */
@Component
public class JwtUtil {

    private final SecretKey key = Keys.hmacShaKeyFor("questapp-secret-key-for-jwt-tokens-2026".getBytes());
    private final long expirationTime = 604800000; // 7 days

    /**
     * Generate JWT token for user.
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    /**
     * Extract email from token.
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract role from token.
     */
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Validate token.
     */
    public boolean isTokenValid(String token, String email) {
        try {
            Claims claims = getClaims(token);
            return email.equals(claims.getSubject()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is expired.
     */
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    /**
     * Get claims from token.
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}