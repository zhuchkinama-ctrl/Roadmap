package org.homework.security;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// === CHUNK: JWT_TOKEN_PROVIDER_TRAJECTORY_TEST [TEST] ===
// Описание: Траекторные тесты для JwtTokenProvider (проверка log-маркеров).
// Dependencies: JUnit 5, Logback ListAppender

// [START_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST]
/*
 * ANCHOR: JWT_TOKEN_PROVIDER_TRAJECTORY_TEST
 * PURPOSE: Траекторные тесты для JwtTokenProvider (проверка log-маркеров).
 *
 * @PreConditions:
 * - JwtTokenProvider инициализирован
 * - ListAppender подключен к logger
 *
 * @PostConditions:
 * - все тесты проверяют log-маркеры ENTRY, EXIT, ERROR
 *
 * @Invariants:
 * - логи содержат правильные маркеры для каждого этапа выполнения
 *
 * @SideEffects:
 * - запись в лог
 *
 * @ForbiddenChanges:
 * - нельзя убрать логирование ENTRY/EXIT
 * - нельзя изменить формат лог-маркеров
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные маркеры
 */
class JwtTokenProviderTrajectoryTest {

    private ListAppender<ILoggingEvent> listAppender;
    private Logger jwtTokenProviderLogger;
    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "testSecretKeyForTesting12345678901234567890";
    private static final long TEST_EXPIRATION_MS = 3600000L;

    @BeforeEach
    void setUp() {
        // Настройка ListAppender для захвата логов
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        jwtTokenProviderLogger = loggerContext.getLogger(JwtTokenProvider.class);

        // Установка уровня DEBUG для захвата всех логов
        jwtTokenProviderLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);

        listAppender = new ListAppender<>();
        listAppender.start();
        jwtTokenProviderLogger.addAppender(listAppender);

        // Инициализация JwtTokenProvider
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", TEST_EXPIRATION_MS);
    }

    @AfterEach
    void tearDown() {
        jwtTokenProviderLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    // [START_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_GENERATE_TOKEN]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_GENERATE_TOKEN
     * PURPOSE: Проверка log-маркеров при генерации токена.
     *
     * @PreConditions:
     * - JwtTokenProvider инициализирован
     *
     * @PostConditions:
     * - ENTRY лог записан
     * - EXIT лог записан
     *
     * @Invariants:
     * - маркеры содержат username и userId
     *
     * @SideEffects:
     * - запись в лог
     *
     * @ForbiddenChanges:
     * - нельзя убрать логирование ENTRY/EXIT
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные маркеры
     */
    @Test
    void generateToken_LogMarkers() {
        // Arrange
        String username = "testuser";
        Long userId = 1L;

        // Act
        String token = jwtTokenProvider.generateToken(username, userId);

        // Assert
        List<ILoggingEvent> logs = listAppender.list;

        // Проверка ENTRY лога
        assertTrue(logs.stream().anyMatch(e -> e.getMessage().contains("JWT_TOKEN_PROVIDER ENTRY")),
                "Должен быть записан ENTRY лог");

        // Проверка EXIT лога
        assertTrue(logs.stream().anyMatch(e -> e.getMessage().contains("JWT_TOKEN_PROVIDER EXIT")),
                "Должен быть записан EXIT лог");

        // Проверка токена
        assertNotNull(token, "Токен не должен быть null");
        assertTrue(jwtTokenProvider.validateToken(token), "Токен должен быть валидным");
    }
    // [END_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_GENERATE_TOKEN]

    // [START_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_GET_USERNAME]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_GET_USERNAME
     * PURPOSE: Проверка log-маркеров при извлечении username из токена.
     *
     * @PreConditions:
     * - валидный токен сгенерирован
     *
     * @PostConditions:
     * - ENTRY лог записан
     * - EXIT лог записан
     *
     * @Invariants:
     * - маркеры содержат username
     *
     * @SideEffects:
     * - запись в лог
     *
     * @ForbiddenChanges:
     * - нельзя убрать логирование ENTRY/EXIT
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные маркеры
     */
    @Test
    void getUsernameFromToken_LogMarkers() {
        // Arrange
        String username = "testuser";
        Long userId = 1L;
        String token = jwtTokenProvider.generateToken(username, userId);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        List<ILoggingEvent> logs = listAppender.list;

        // Проверка ENTRY лога
        assertTrue(logs.stream().anyMatch(e -> e.getMessage().contains("JWT_TOKEN_PROVIDER ENTRY")),
                "Должен быть записан ENTRY лог");

        // Проверка EXIT лога
        assertTrue(logs.stream().anyMatch(e -> e.getMessage().contains("JWT_TOKEN_PROVIDER EXIT")),
                "Должен быть записан EXIT лог");

        // Проверка извлеченного username
        assertEquals(username, extractedUsername, "Username должен соответствовать");
    }
    // [END_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_GET_USERNAME]

    // [START_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_GET_USERNAME_INVALID_TOKEN]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_GET_USERNAME_INVALID_TOKEN
     * PURPOSE: Проверка log-маркеров при извлечении username из неверного токена.
     *
     * @PreConditions:
     * - передан неверный токен
     *
     * @PostConditions:
     * - ENTRY лог записан
     * - ERROR лог записан
     *
     * @Invariants:
     * - ERROR лог содержит сообщение об ошибке
     *
     * @SideEffects:
     * - запись в лог
     *
     * @ForbiddenChanges:
     * - нельзя убрать логирование ERROR
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные маркеры
     */
    @Test
    void getUsernameFromToken_InvalidToken_LogMarkers() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(JwtException.class, () -> jwtTokenProvider.getUsernameFromToken(invalidToken));

        List<ILoggingEvent> logs = listAppender.list;

        // Проверка ENTRY лога
        assertTrue(logs.stream().anyMatch(e -> e.getMessage().contains("JWT_TOKEN_PROVIDER ENTRY")),
                "Должен быть записан ENTRY лог");

        // Проверка ERROR лога
        assertTrue(logs.stream().anyMatch(e -> e.getMessage().contains("JWT_TOKEN_PROVIDER ERROR")),
                "Должен быть записан ERROR лог");
    }
    // [END_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_GET_USERNAME_INVALID_TOKEN]

    // [START_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_VALIDATE_TOKEN_VALID]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_VALIDATE_TOKEN_VALID
     * PURPOSE: Проверка log-маркеров при валидации валидного токена.
     *
     * @PreConditions:
     * - валидный токен сгенерирован
     *
     * @PostConditions:
     * - ENTRY лог записан
     * - EXIT лог записан
     *
     * @Invariants:
     * - EXIT лог содержит success
     *
     * @SideEffects:
     * - запись в лог
     *
     * @ForbiddenChanges:
     * - нельзя убрать логирование ENTRY/EXIT
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные маркеры
     */
    @Test
    void validateToken_Valid_LogMarkers() {
        // Arrange
        String token = jwtTokenProvider.generateToken("testuser", 1L);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        List<ILoggingEvent> logs = listAppender.list;

        // Проверка ENTRY лога
        assertTrue(logs.stream().anyMatch(e -> e.getMessage().contains("JWT_TOKEN_PROVIDER ENTRY")),
                "Должен быть записан ENTRY лог");

        // Проверка EXIT лога
        assertTrue(logs.stream().anyMatch(e -> e.getMessage().contains("JWT_TOKEN_PROVIDER EXIT")),
                "Должен быть записан EXIT лог");

        // Проверка результата
        assertTrue(isValid, "Токен должен быть валидным");
    }
    // [END_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_VALIDATE_TOKEN_VALID]

    // [START_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_VALIDATE_TOKEN_INVALID]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_VALIDATE_TOKEN_INVALID
     * PURPOSE: Проверка log-маркеров при валидации неверного токена.
     *
     * @PreConditions:
     * - передан неверный токен
     *
     * @PostConditions:
     * - ENTRY лог записан
     * - ERROR лог записан
     *
     * @Invariants:
     * - ERROR лог содержит сообщение об ошибке
     *
     * @SideEffects:
     * - запись в лог
     *
     * @ForbiddenChanges:
     * - нельзя убрать логирование ERROR
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные маркеры
     */
    @Test
    void validateToken_Invalid_LogMarkers() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        List<ILoggingEvent> logs = listAppender.list;

        // Проверка ENTRY лога
        assertTrue(logs.stream().anyMatch(e -> e.getMessage().contains("JWT_TOKEN_PROVIDER ENTRY")),
                "Должен быть записан ENTRY лог");

        // Проверка ERROR лога
        assertTrue(logs.stream().anyMatch(e -> e.getMessage().contains("JWT_TOKEN_PROVIDER ERROR")),
                "Должен быть записан ERROR лог");

        // Проверка результата
        assertFalse(isValid, "Токен должен быть невалидным");
    }
    // [END_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST_VALIDATE_TOKEN_INVALID]
}
// [END_JWT_TOKEN_PROVIDER_TRAJECTORY_TEST]
// === END_CHUNK: JWT_TOKEN_PROVIDER_TRAJECTORY_TEST ===
