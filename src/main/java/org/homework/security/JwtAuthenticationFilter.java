// === CHUNK: JWT_AUTHENTICATION_FILTER [SECURITY] ===
// Описание: JWT фильтр аутентификации для Spring Security.
// Dependencies: Spring Security, Servlet API, Lombok

package org.homework.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

// [START_JWT_AUTHENTICATION_FILTER]
/*
 * ANCHOR: JWT_AUTHENTICATION_FILTER
 * PURPOSE: JWT фильтр аутентификации для Spring Security.
 *
 * @PreConditions:
 * - JwtTokenProvider внедрён через DI
 * - Spring Security контекст доступен
 *
 * @PostConditions:
 * - при валидном токене: пользователь аутентифицирован в SecurityContext
 * - при отсутствии/невалидном токене: запрос продолжается без аутентификации
 * - публичные эндпоинты пропускаются без проверки токена
 *
 * @Invariants:
 * - фильтр выполняется один раз для каждого запроса (OncePerRequestFilter)
 * - публичные эндпоинты (/api/v1/auth/) всегда пропускаются
 * - токен проверяется только если заголовок Authorization начинается с "Bearer "
 *
 * @SideEffects:
 * - устанавливает аутентификацию в SecurityContextHolder при валидном токене
 * - пишет логи в SLF4J
 *
 * @ForbiddenChanges:
 * - нельзя убрать пропуск публичных эндпоинтов
 * - нельзя изменить формат токена (Bearer)
 * - нельзя убрать установку аутентификации в SecurityContext
 *
 * @AllowedRefactorZone:
 * - можно изменить список публичных эндпоинтов
 * - можно добавить дополнительные проверки (например, проверку ролей)
 * - можно изменить уровень логирования
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("JWT Filter processing request: {} {}", method, requestURI);
        
        // Пропускаем публичные эндпоинты без проверки токена
        if (requestURI.startsWith("/api/v1/auth/")) {
            log.debug("Skipping JWT authentication for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        String header = request.getHeader("Authorization");
        log.debug("Authorization header: {}", header != null ? "Bearer ***" : "null");
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            log.debug("Token found, validating...");
            
            if (tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsernameFromToken(token);
                log.debug("Token valid for user: {}", username);
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("Invalid token provided");
            }
        } else {
            log.debug("No Bearer token found in request");
        }
        
        filterChain.doFilter(request, response);
    }
}
// [END_JWT_AUTHENTICATION_FILTER]
// === END_CHUNK: JWT_AUTHENTICATION_FILTER ===