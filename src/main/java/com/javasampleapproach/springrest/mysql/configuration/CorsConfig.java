package com.javasampleapproach.springrest.mysql.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplikuje sa na všetky endpointy v tvojej API
                .allowedOrigins("http://localhost:4200", "http://localhost:4201") // Povolené adresy
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS"); // Povolené HTTP metódy

    }
}