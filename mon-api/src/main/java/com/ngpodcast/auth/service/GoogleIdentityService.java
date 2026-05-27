package com.ngpodcast.auth.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GoogleIdentityService {
    private final RestClient restClient = RestClient.create();

    @Value("${google.client-id:}")
    private String clientId;

    public GoogleProfile verify(String idToken) {
        if (clientId == null || clientId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Google OAuth n'est pas encore configure côté serveur.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> body = restClient.get()
                .uri("https://oauth2.googleapis.com/tokeninfo?id_token={token}", idToken)
                .retrieve()
                .body(Map.class);

        if (body == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google invalide.");
        }

        String audience = string(body.get("aud"));
        if (!clientId.equals(audience)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client Google invalide.");
        }

        String email = string(body.get("email"));
        String subject = string(body.get("sub"));
        boolean emailVerified = Boolean.parseBoolean(string(body.get("email_verified")));
        if (email.isBlank() || subject.isBlank() || !emailVerified) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email Google non verifie.");
        }

        return new GoogleProfile(
                subject,
                email,
                string(body.get("given_name")),
                string(body.get("family_name")),
                string(body.get("name"))
        );
    }

    private static String string(Object value) {
        return value == null ? "" : value.toString();
    }

    public record GoogleProfile(
            String subject,
            String email,
            String givenName,
            String familyName,
            String displayName
    ) {}
}
