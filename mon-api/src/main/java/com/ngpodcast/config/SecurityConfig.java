package com.ngpodcast.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngpodcast.security.JwtFilter;
import com.ngpodcast.security.JwtService;
import com.ngpodcast.user.UserRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
                 * Ordre des matchers : endpoints publics (auth, fichiers, GET listes publiques),
                 * puis GET /mine authentifiés, puis tout le reste de /api/** → JWT obligatoire.
                 */
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/login", "/auth/register", "/auth/reset-password", "/auth/reset-password/confirm")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/files/**").permitAll()
                /* « /mine » doit rester avant les motifs /** sinon Spring autoriserait /mine sans JWT */
                .requestMatchers(HttpMethod.GET, "/api/podcasts/mine").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/writings/mine").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/storytellings/mine").authenticated()
                /* Slash final distinct : motifs type /api/writings/** ne couvrent pas /api/writings/ sous PathPatterns,
                   sinon ces GET tombent dans anyRequest → 403 alors que sans slash ils sont bien publics. */
                .requestMatchers(HttpMethod.GET, "/api/podcasts", "/api/podcasts/").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/podcasts/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/podcasts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/writings", "/api/writings/").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/writings/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/writings/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/storytellings", "/api/storytellings/").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/storytellings/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/storytellings/**").permitAll()
                .anyRequest().authenticated()
            )
                /* JWT avant UsernamePasswordAuthenticationFilter : peupler SecurityContext avant AuthorizationFilter. */
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
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
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
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
