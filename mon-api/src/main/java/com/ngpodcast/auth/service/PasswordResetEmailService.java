package com.ngpodcast.auth.service;

import com.ngpodcast.user.User;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.mail.from:no-reply@ng-podcast.local}")
    private String from;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public PasswordResetEmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void sendResetLink(User user, String token) {
        String link = frontendUrl + "/resetpassword/confirm?token=" + token;
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null || mailHost == null || mailHost.isBlank()) {
            System.out.println("[DEV] Password reset link for " + user.getEmail() + ": " + link);
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(user.getEmail());
            msg.setSubject("Reset your ng-podcast password");
            msg.setText("""
                    Hello %s,

                    We received a request to reset your ng-podcast password.

                    Click this link to choose a new password:
                    %s

                    This link expires in 1 hour. If you did not request it, you can ignore this email.
                    """.formatted(user.getPrenom() == null ? "" : user.getPrenom(), link));
            sender.send(msg);
        } catch (RuntimeException ex) {
            System.out.println("[DEV] Could not send password reset email. Link: " + link);
        }
    }
}
