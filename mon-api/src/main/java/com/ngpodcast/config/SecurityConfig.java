package com.ngpodcast.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngpodcast.security.JwtFilter;
import com.ngpodcast.security.JwtService;
import com.ngpodcast.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Filtre unique : pas de `@Component`, sinon Boot l’ajoute aussi à la chaîne servlet → JWT / auth incohérents (403 sur POST JSON). */
    @Bean
    public JwtFilter jwtFilter(
            JwtService jwtService,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        return new JwtFilter(jwtService, userRepository, objectMapper);
    }

    /** Empêche l’auto-enregistrement `FilterRegistrationBean` de tout `Filter` `@Bean` (chaîne JWT = SecurityFilterChain seule). */
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterServletDisabled(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> reg = new FilterRegistrationBean<>(jwtFilter);
        reg.setEnabled(false);
        return reg;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http
                /* CORS : voir corsConfigurationSource() — indispensable pour Angular sur :4200. */
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                /* API stateless JWT : pas de token synchronizer / cookie CSRF côté SPA. */
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                /*
                 * Sans session/form-login, Spring peut répondre 403 sur une route
                 * authenticated() appelée anonymement. Pour une SPA, c'est bien un 401 :
                 * "connecte-toi d'abord".
                 */
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentification requise."))
                )
                /*
                 * Ordre des matchers : endpoints publics (auth, fichiers, GET/HEAD listes publiques),
                 * puis GET /mine authentifiés, puis tout le reste de /api/** → JWT obligatoire.
                 */
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    auth.requestMatchers(
                                    "/error",
                                    "/auth/config",
                                    "/auth/login",
                                    "/auth/register",
                                    "/auth/google",
                                    "/auth/resend-verification",
                                    "/auth/verify-email",
                                    "/auth/reset-password",
                                    "/auth/reset-password/confirm")
                            .permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/files/**").permitAll();
                    auth.requestMatchers(HttpMethod.HEAD, "/files/**").permitAll();
                    /* « /mine » avant /** : sinon lecture anonyme autorisée sur /mine */
                    auth.requestMatchers(HttpMethod.GET, "/api/podcasts/mine").authenticated();
                    auth.requestMatchers(HttpMethod.GET, "/api/writings/mine").authenticated();
                    auth.requestMatchers(HttpMethod.GET, "/api/storytellings/mine").authenticated();
                    permitPublicCatalogRead(auth, "podcasts");
                    permitPublicCatalogRead(auth, "writings");
                    permitPublicCatalogRead(auth, "storytellings");
                    auth.anyRequest().authenticated();
                })
                /* JWT avant UsernamePasswordAuthenticationFilter : peupler SecurityContext avant AuthorizationFilter. */
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * GET/HEAD catalogue public (listes + détail par id) : accès anonyme.
     * PathPatterns + Ant : évite les 403 sur slash final ou chemins non couverts par /** seul.
     */
    private static void permitPublicCatalogRead(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
            String segment) {
        String base = "/api/" + segment;
        auth.requestMatchers(HttpMethod.GET, base, base + "/").permitAll();
        auth.requestMatchers(HttpMethod.GET, base + "/*").permitAll();
        auth.requestMatchers(HttpMethod.GET, base + "/**").permitAll();
        auth.requestMatchers(HttpMethod.HEAD, base, base + "/").permitAll();
        auth.requestMatchers(HttpMethod.HEAD, base + "/*").permitAll();
        auth.requestMatchers(HttpMethod.HEAD, base + "/**").permitAll();
        auth.requestMatchers(new AntPathRequestMatcher(base, HttpMethod.GET.name())).permitAll();
        auth.requestMatchers(new AntPathRequestMatcher(base + "/", HttpMethod.GET.name())).permitAll();
        auth.requestMatchers(new AntPathRequestMatcher(base + "/**", HttpMethod.GET.name())).permitAll();
        auth.requestMatchers(new AntPathRequestMatcher(base, HttpMethod.HEAD.name())).permitAll();
        auth.requestMatchers(new AntPathRequestMatcher(base + "/", HttpMethod.HEAD.name())).permitAll();
        auth.requestMatchers(new AntPathRequestMatcher(base + "/**", HttpMethod.HEAD.name())).permitAll();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        /*
         * Origines dev Angular explicites + motifs pour autres ports / IP locale.
         * Autoriser Authorization (preflight Access-Control-Request-Headers).
         * allowCredentials=false → compatibilité avec Origin wildcard "*".
         */
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "http://localhost:*",
                "http://127.0.0.1:*",
                "*"));
        config.setAllowedMethods(List.of("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.addAllowedHeader("Authorization");
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
