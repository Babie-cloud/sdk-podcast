package com.ngpodcast.auth.service;

import com.ngpodcast.auth.dto.*;
import com.ngpodcast.security.JwtService;
import com.ngpodcast.user.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository  userRepository;
    private final JwtService      jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.jwtService      = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email()))
            throw new IllegalArgumentException("Email deja utilise.");

        String username = req.name().trim() + " " + req.prenom().trim();

        User user = User.builder()
            .name(req.name())
            .prenom(req.prenom())
            .username(username)
            .email(req.email())
            .password(passwordEncoder.encode(req.password()))
            .role(Role.USER)
            .build();

        userRepository.save(user);
        return toResponse(jwtService.generateToken(user.getEmail()), user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect."));

        if (!passwordEncoder.matches(req.password(), user.getPassword()))
            throw new BadCredentialsException("Email ou mot de passe incorrect.");

        return toResponse(jwtService.generateToken(user.getEmail()), user);
    }

    public void resetPassword(ResetPasswordRequest req) {
        // TODO Phase 2 : envoyer un email de reset
    }

    private AuthResponse toResponse(String token, User user) {
        return new AuthResponse(token,
            new AuthResponse.UserDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
            )
        );
    }
}
