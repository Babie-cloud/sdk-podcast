package com.ngpodcast.auth.service;

import com.ngpodcast.auth.dto.*;
import com.ngpodcast.auth.entity.PasswordResetToken;
import com.ngpodcast.auth.repository.PasswordResetTokenRepository;
import com.ngpodcast.security.JwtService;
import com.ngpodcast.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthService {

    private final UserRepository               userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService                   jwtService;
    private final PasswordEncoder              passwordEncoder;
    private final SecureRandom                 secureRandom = new SecureRandom();

    private final boolean exposePasswordResetToken;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.password-reset.expose-token:false}") boolean exposePasswordResetToken) {
        this.userRepository               = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.jwtService                   = jwtService;
        this.passwordEncoder             = passwordEncoder;
        this.exposePasswordResetToken    = exposePasswordResetToken;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email deja utilise.");
        }

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

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect.");
        }

        return toResponse(jwtService.generateToken(user.getEmail()), user);
    }

    /**
     * Demande de reinitialisation : reponse toujours 202 pour ne pas reveler si l'email existe.
     * En local, activer {@code app.password-reset.expose-token=true} pour recevoir le jeton dans le corps JSON.
     */
    @Transactional
    public PasswordResetInitResponse requestPasswordReset(ResetPasswordRequest req) {
        return userRepository.findByEmail(req.email())
                .map(user -> {
                    passwordResetTokenRepository.deleteUnusedByUserId(user.getId());
                    byte[] raw = new byte[32];
                    secureRandom.nextBytes(raw);
                    String plainToken = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
                    String tokenHash = sha256Hex(raw);
                    Instant expiresAt = Instant.now().plus(Duration.ofHours(1));
                    passwordResetTokenRepository.save(new PasswordResetToken(user, tokenHash, expiresAt));
                    String exposed = exposePasswordResetToken ? plainToken : null;
                    return new PasswordResetInitResponse(exposed);
                })
                .orElse(new PasswordResetInitResponse(null));
    }

    @Transactional
    public void confirmPasswordReset(ResetPasswordConfirmRequest req) {
        byte[] raw;
        try {
            raw = Base64.getUrlDecoder().decode(req.token());
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("Token invalide ou expire.");
        }
        String hash = sha256Hex(raw);
        PasswordResetToken prt = passwordResetTokenRepository
                .findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(hash, Instant.now())
                .orElseThrow(() -> new BadCredentialsException("Token invalide ou expire."));

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        prt.setUsedAt(Instant.now());
        userRepository.save(user);
        passwordResetTokenRepository.save(prt);
    }

    private static String sha256Hex(byte[] data) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private AuthResponse toResponse(String token, User user) {
        return new AuthResponse(token,
                new AuthResponse.UserDto(
                        user.getId(),
                        user.getEmail(),
                        user.getPublicHandle(),
                        user.getRole().name(),
                        user.getName(),
                        user.getPrenom()
                )
        );
    }
}
