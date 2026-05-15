package com.ngpodcast.config;

import com.ngpodcast.security.JwtFilter;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/login", "/auth/register", "/auth/reset-password", "/auth/reset-password/confirm")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/files/**").permitAll()
                /* « /mine » doit rester avant les motifs /** sinon Spring autoriserait /mine sans JWT */
                .requestMatchers(HttpMethod.GET, "/api/podcasts/mine").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/writings/mine").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/storytellings/mine").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/podcasts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/podcasts/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/podcasts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/writings").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/writings/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/writings/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/storytellings").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/storytellings/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/storytellings/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        /* Sans motif qui matche l’Origin du navigateur (https, autre IP, SSR…), Spring peut répondre 403.
           allowCredentials=false → le motif * est autorisé ; à resserrer en prod si besoin. */
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
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
