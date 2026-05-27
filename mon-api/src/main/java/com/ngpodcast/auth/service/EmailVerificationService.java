package com.ngpodcast.auth.service;

import com.ngpodcast.auth.entity.EmailVerificationToken;
import com.ngpodcast.auth.repository.EmailVerificationTokenRepository;
import com.ngpodcast.user.User;
import com.ngpodcast.user.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.api-public-url:http://127.0.0.1:8080}")
    private String apiPublicUrl;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.mail.from:no-reply@ng-podcast.local}")
    private String from;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.mailSenderProvider = mailSenderProvider;
    }

    @Transactional
    public void sendVerification(User user) {
        if (user.isEmailVerified()) return;
        Instant now = Instant.now();
        tokenRepository.markUnusedByUserIdAsUsed(user.getId(), now);

        String token = createToken();
        tokenRepository.save(new EmailVerificationToken(
                user,
                token,
                now.plus(Duration.ofHours(24))
        ));

        String link = apiPublicUrl + "/auth/verify-email?token=" + token;
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null || mailHost == null || mailHost.isBlank()) {
            System.out.println("[DEV] Email verification link for " + user.getEmail() + ": " + link);
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(user.getEmail());
            msg.setSubject("Confirm your ng-podcast account");
            msg.setText("""
                    Hello %s,

                    Your ng-podcast account has been created.

                    Please confirm your email address by clicking this link:
                    %s

                    If you did not request this account, you can ignore this email.
                    """.formatted(user.getPrenom() == null ? "" : user.getPrenom(), link));
            sender.send(msg);
        } catch (RuntimeException ex) {
            System.out.println("[DEV] Could not send verification email. Link: " + link);
        }
    }

    @Transactional
    public String verify(String token) {
        EmailVerificationToken evt = tokenRepository
                .findByTokenAndUsedAtIsNullAndExpiresAtAfter(token, Instant.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lien de verification invalide ou expire."));

        User user = evt.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        evt.setUsedAt(Instant.now());
        tokenRepository.save(evt);

        return frontendUrl + "/login?verified=1";
    }

    private String createToken() {
        byte[] raw = new byte[32];
        secureRandom.nextBytes(raw);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }
}
