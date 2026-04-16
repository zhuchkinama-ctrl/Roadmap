package org.homework.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// === CHUNK: JWT_TOKEN_PROVIDER [SECURITY] ===
// Описание: Провайдер JWT токенов для аутентификации.
// Dependencies: JJWT, Spring

// [START_JWT_TOKEN_PROVIDER]
/*
 * ANCHOR: JWT_TOKEN_PROVIDER
 * PURPOSE: Провайдер JWT токенов для аутентификации.
 *
 * @PreConditions:
 * - jwt.secret настроен в application.properties
 *
 * @PostConditions:
 * - токены генерируются и валидируются с использованием секретного ключа
 *
 * @Invariants:
 * - секретный ключ никогда не логируется
 * - токен содержит username в subject
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать валидацию токена
 * - нельзя убрать подпись токена
 *
 * @AllowedRefactorZone:
 * - можно изменить срок действия токена
 * - можно добавить дополнительные claims в токен
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:ChangeThisSecretInProduction}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        log.debug("JWT_TOKEN_PROVIDER ENTRY - generateToken - username: {}", username);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
        
        log.debug("JWT_TOKEN_PROVIDER EXIT - generateToken - success - username: {}", username);
        return token;
    }

    public String getUsernameFromToken(String token) {
        log.debug("JWT_TOKEN_PROVIDER ENTRY - getUsernameFromToken");
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        log.debug("JWT_TOKEN_PROVIDER EXIT - getUsernameFromToken - success - username: {}", username);
        return username;
    }

    public boolean validateToken(String token) {
        log.debug("JWT_TOKEN_PROVIDER ENTRY - validateToken");
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            log.debug("JWT_TOKEN_PROVIDER EXIT - validateToken - success");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT_TOKEN_PROVIDER ERROR - validateToken - failed: {}", e.getMessage());
            return false;
        }
    }
}
// [END_JWT_TOKEN_PROVIDER]
// === END_CHUNK: JWT_TOKEN_PROVIDER ===
