package com.example.todo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    public static final String AUTHORITIES_CLAIM = "authorities";

    private final SecretKey signingKey;
    private final long accessTokenValidityMs;

    public JwtUtils(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-validity-ms}") long accessTokenValidityMs) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 bytes (UTF-8) for HS256");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityMs = accessTokenValidityMs;
    }

    public String generateAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        String authoritiesJoined = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenValidityMs);
        return Jwts.builder()
                .subject(username)
                .claim(AUTHORITIES_CLAIM, authoritiesJoined)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Collection<GrantedAuthority> extractAuthorities(String token) {
        String raw = parseClaims(token).get(AUTHORITIES_CLAIM, String.class);
        if (raw == null || raw.isBlank()) {
            return java.util.List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
