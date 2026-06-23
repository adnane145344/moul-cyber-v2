package com.adnane.moulcyber.configuration.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import com.adnane.moulcyber.domain.user.Role;
import com.adnane.moulcyber.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final Duration expiration;

    public JwtService(
            @Value("${security.jwt.secret}") String encodedSecret,
            @Value("${security.jwt.expiration:1h}") Duration expiration) {
        this.secretKey = Keys.hmacShaKeyFor(decodeAndValidateSecret(encodedSecret));
        this.expiration = validateExpiration(expiration);
    }

    public String generateToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public UserPrincipal parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Number userId = claims.get("userId", Number.class);
        String role = claims.get("role", String.class);
        return new UserPrincipal(
                userId.longValue(),
                claims.getSubject(),
                Role.valueOf(role));
    }

    private byte[] decodeAndValidateSecret(String encodedSecret) {
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured.");
        }

        byte[] secret;
        try {
            secret = Decoders.BASE64.decode(encodedSecret);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("JWT secret must be valid Base64.", exception);
        }
        if (secret.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes.");
        }
        return secret;
    }

    private Duration validateExpiration(Duration expiration) {
        if (expiration == null || expiration.isZero() || expiration.isNegative()) {
            throw new IllegalStateException("JWT expiration must be positive.");
        }
        return expiration;
    }
}
