package org.homework.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация CORS для разрешения запросов с фронтенда.
 */
@Slf4j
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        log.info("Configuring CORS filter");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Разрешаем запросы с любого источника (для разработки)
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));
        
        // Разрешаем все необходимые методы
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Разрешаем все заголовки
        config.setAllowedHeaders(List.of("*"));
        
        // Разрешаем expose заголовки
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        
        // Максимальное время preflight запроса (1 час)
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        
        log.info("CORS filter configured successfully");
        return new CorsFilter(source);
    }
}
