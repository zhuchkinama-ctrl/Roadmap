// === CHUNK: SECURITY_CONFIG [SECURITY] ===
// Описание: Конфигурация Spring Security с JWT аутентификацией.
// Dependencies: Spring Security, Lombok

package org.homework.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// [START_SECURITY_CONFIG]
/*
 * ANCHOR: SECURITY_CONFIG
 * PURPOSE: Конфигурация Spring Security с JWT аутентификацией.
 *
 * @PreConditions:
 * - JwtAuthenticationFilter внедрён через DI
 * - CustomUserDetailsService внедрён через DI
 *
 * @PostConditions:
 * - SecurityFilterChain настроен с JWT фильтром
 * - публичные эндпоинты (/api/v1/auth/**, /error) доступны без аутентификации
 * - все остальные запросы требуют аутентификации
 * - сессии stateless
 *
 * @Invariants:
 * - CSRF отключён (токен-базированная аутентификация)
 * - CORS настроен
 * - JWT фильтр добавляется перед UsernamePasswordAuthenticationFilter
 *
 * @SideEffects:
 * - настраивает цепочку фильтров безопасности
 * - пишет логи в SLF4J
 *
 * @ForbiddenChanges:
 * - нельзя включить CSRF без согласования
 * - нельзя убрать JWT фильтр
 * - нельзя сделать сессии stateful
 *
 * @AllowedRefactorZone:
 * - можно изменить список публичных эндпоинтов
 * - можно добавить дополнительные правила авторизации
 * - можно изменить алгоритм хеширования паролей
 */
@Slf4j
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring SecurityFilterChain");

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configure(http))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("SecurityFilterChain configured successfully");
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
// [END_SECURITY_CONFIG]
// === END_CHUNK: SECURITY_CONFIG ===