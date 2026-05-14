package com.ngpodcast.auth.controller;

import com.ngpodcast.auth.dto.*;
import com.ngpodcast.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetInitResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return ResponseEntity.ok(authService.requestPasswordReset(req));
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<Void> confirmReset(@Valid @RequestBody ResetPasswordConfirmRequest req) {
        authService.confirmPasswordReset(req);
        return ResponseEntity.noContent().build();
    }
}
