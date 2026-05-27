package com.ngpodcast.auth.controller;

import com.ngpodcast.auth.dto.*;
import com.ngpodcast.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${google.client-id:}")
    private String googleClientId;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/config")
    public ResponseEntity<AuthPublicConfigResponse> publicConfig() {
        String clientId = googleClientId == null ? "" : googleClientId.trim();
        return ResponseEntity.ok(new AuthPublicConfigResponse(clientId));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleLoginRequest req) {
        return ResponseEntity.ok(authService.googleLogin(req));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest req) {
        authService.resendVerification(req);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authService.verifyEmail(token)))
                .build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetInitResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(authService.requestPasswordReset(req));
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<Void> confirmReset(@Valid @RequestBody ResetPasswordConfirmRequest req) {
        authService.confirmPasswordReset(req);
        return ResponseEntity.noContent().build();
    }
}
