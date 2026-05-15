package com.ngpodcast.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${storage.local.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    /* CORS : uniquement dans SecurityConfig (corsConfigurationSource + chaîne de filtres).
       Évite deux registres qui peuvent prêter à confusion ; NetworkError vient en général
       d’une connexion impossible (Spring arrêté / Flyway / port), pas du doublon CORS. */
}