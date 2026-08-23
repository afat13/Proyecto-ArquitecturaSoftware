package com.example.aprendeaprender.api.controller;

import java.util.UUID;

import com.example.aprendeaprender.api.auth.AuthService;
import com.example.aprendeaprender.api.auth.AuthService.LoginRequest;
import com.example.aprendeaprender.api.auth.AuthService.RegisterRequest;
import com.example.aprendeaprender.api.auth.AuthService.SessionResponse;
import com.example.aprendeaprender.api.auth.AuthService.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public SessionResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public SessionResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.currentUser(UUID.fromString(authentication.getName()));
    }

    @DeleteMapping("/session")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization.substring(7).trim());
        return ResponseEntity.noContent().build();
    }
}
