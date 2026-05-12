package com.example.todo.api;

import com.example.todo.dto.apiv1.LoginRequest;
import com.example.todo.dto.apiv1.LoginResponse;
import com.example.todo.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final long accessTokenValidityMs;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            @Value("${app.jwt.access-token-validity-ms}") long accessTokenValidityMs) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.accessTokenValidityMs = accessTokenValidityMs;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String token = jwtUtils.generateAccessToken(principal.getUsername(), principal.getAuthorities());
        return new LoginResponse(token, accessTokenValidityMs);
    }
}
