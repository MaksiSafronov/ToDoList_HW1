package com.example.todo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final RequestMatcher permitAllMatcher;

    public JwtAuthFilter(JwtUtils jwtUtils, ObjectMapper objectMapper, RequestMatcher permitAllMatcher) {
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
        this.permitAllMatcher = permitAllMatcher;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            if (!jwtUtils.validateToken(token)) {
                respondInvalidToken(request, response, filterChain, "Invalid or expired JWT");
                return;
            }
            String username = jwtUtils.extractUsername(token);
            var authorities = jwtUtils.extractAuthorities(token);
            var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            respondInvalidToken(request, response, filterChain, "Invalid JWT");
        }
    }

    private void respondInvalidToken(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            String message) throws IOException, ServletException {
        if (permitAllMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        SecurityErrorResponses.writeJson(request, response, objectMapper, HttpStatus.UNAUTHORIZED, message);
    }
}
