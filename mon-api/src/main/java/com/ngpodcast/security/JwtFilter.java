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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
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
     * Réponses JSON 401 lorsque le JWT est « valide » mais incohérent (sujet vide, utilisateur supprimé).
     * <p>
     * Jeton vide / expiré / signature invalide avec header {@code Bearer} :
     * <ul>
     *   <li>{@code GET} / {@code HEAD} : contexte effacé, requête <strong>anonyme</strong> —
     *       les lectures {@code permitAll} (listes publiques) fonctionnent malgré un vieux jeton dans le navigateur ;
     *       les routes réservées (ex. {@code /mine}) renvoient 403.</li>
     *   <li>mutation ({@code POST}, {@code PUT}, …) : 401 pour forcer une reconnexion côté SPA.</li>
     * </ul>
     */
    private void unauthorized(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("detail", detail)));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/auth/")) {
            return true;
        }
        return isPublicCatalogRead(request);
    }

    /** GET/HEAD catalogue public (listes + détail id) : ne pas appliquer JWT (évite 401/403 avec vieux jeton). */
    private static boolean isPublicCatalogRead(HttpServletRequest request) {
        String method = request.getMethod();
        if (!HttpMethod.GET.matches(method) && !HttpMethod.HEAD.matches(method)) {
            return false;
        }
        String path = request.getRequestURI();
        if (path == null || path.contains("/mine")) {
            return false;
        }
        for (String base : new String[] {"/api/podcasts", "/api/writings", "/api/storytellings"}) {
            if (path.equals(base) || path.equals(base + "/") || path.startsWith(base + "/")) {
                return true;
            }
        }
        return false;
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
            SecurityContextHolder.clearContext();
            String method = request.getMethod();
            boolean safeRead =
                    HttpMethod.GET.matches(method)
                            || HttpMethod.HEAD.matches(method)
                            || HttpMethod.OPTIONS.matches(method);
            if (!safeRead) {
                unauthorized(
                        response,
                        "Jeton JWT invalide ou expire. Deconnectez-vous et reconnectez-vous.");
                return;
            }
            chain.doFilter(request, response);
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
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        chain.doFilter(request, response);
    }
}
