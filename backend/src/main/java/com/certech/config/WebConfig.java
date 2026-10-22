package com.certech.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Value("${app.contact.rate-limit.max-requests:5}")
    private int contactMaxRequests;

    @Value("${app.contact.rate-limit.window-seconds:60}")
    private long contactWindowSeconds;

    @Bean
    ContactRateLimitInterceptor contactRateLimitInterceptor() {
        return new ContactRateLimitInterceptor(contactMaxRequests, contactWindowSeconds);
    }

    @Bean
    ApiSecurityHeadersInterceptor apiSecurityHeadersInterceptor() {
        return new ApiSecurityHeadersInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiSecurityHeadersInterceptor()).addPathPatterns("/api/**");
        registry.addInterceptor(contactRateLimitInterceptor()).addPathPatterns("/api/contacto");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim).filter(value -> !value.isBlank()).toArray(String[]::new);
        if (origins.length == 0 || Arrays.stream(origins).anyMatch("*"::equals)) {
            throw new IllegalStateException("Configure explicit CORS origins; wildcard origins are not allowed.");
        }
        registry.addMapping("/api/**").allowedOrigins(origins)
                .allowedMethods("POST", "OPTIONS").allowedHeaders("Content-Type", "Accept")
                .maxAge(3600);
    }
}
