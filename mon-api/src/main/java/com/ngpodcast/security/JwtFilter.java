package com.ngpodcast.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngpodcast.user.User;
import com.ngpodcast.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/** Enregistré uniquement via {@link com.ngpodcast.config.SecurityConfig} (+ désactivation servlet auto). */
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public JwtFilter(JwtService jwtService, UserRepository userRepository, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Réponses JSON 401 pour que la SPA puisse déconnecter / rafraîchir la session.
     * Si un header {@code Bearer} est envoyé mais le jeton est vide, invalide ou expiré,
     * on répond toujours 401 (plus de GET anonymes avec un vieux jeton qui finissaient en 403 sur /mine).
     */
    private void unauthorized(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("detail", detail)));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7).trim();
        if (token.isEmpty() || !jwtService.isValid(token)) {
            unauthorized(
                    response,
                    "Jeton JWT invalide ou expire. Deconnectez-vous et reconnectez-vous.");
            return;
        }

        final String email = jwtService.extractEmail(token);

        /*
         * Ne pas exiger authentication == null : un filtre en amont ou un contexte résiduel
         * peut laisser un principal incohérent ; pour un Bearer valide on réassocie toujours
         * l’User issu du JWT (évite des 403 opaques sur POST JSON valides alors que GET /mine marche).
         */
        if (email == null || email.isBlank()) {
            unauthorized(response, "Jeton JWT invalide : sujet vide.");
            return;
        }

        var loaded = userRepository.findByEmail(email);
        if (loaded.isEmpty()) {
            unauthorized(
                    response,
                    "Jeton JWT : utilisateur inconnu ou supprime. Connectez-vous de nouveau.");
            return;
        }
        User user = loaded.get();
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }
}
