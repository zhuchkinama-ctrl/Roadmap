package org.homework.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

// === CHUNK: JWT_TOKEN_PROVIDER_TEST [TEST] ===
// Описание: Юнит-тесты для JwtTokenProvider.
// Dependencies: JUnit 5, JJWT

// [START_JWT_TOKEN_PROVIDER_TEST]
/*
 * ANCHOR: JWT_TOKEN_PROVIDER_TEST
 * PURPOSE: Юнит-тесты для JwtTokenProvider.
 *
 * @PreConditions:
 * - JwtTokenProvider инициализирован
 * - jwtSecret и jwtExpirationMs настроены через ReflectionTestUtils
 *
 * @PostConditions:
 * - все тесты проверяют контракты методов JwtTokenProvider
 *
 * @Invariants:
 * - секретный ключ используется для генерации и валидации токенов
 * - токен содержит username в subject
 * - токен содержит userId в claims
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку валидности токена
 * - нельзя убрать проверку содержимого токена
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тестовые кейсы
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "testSecretKeyForTesting12345678901234567890";
    private static final long TEST_EXPIRATION_MS = 3600000L; // 1 час

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", TEST_EXPIRATION_MS);
    }

    // [START_JWT_TOKEN_PROVIDER_TEST_GENERATE_TOKEN_SUCCESS]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TEST_GENERATE_TOKEN_SUCCESS
     * PURPOSE: Проверка успешной генерации токена.
     *
     * @PreConditions:
     * - jwtTokenProvider инициализирован
     *
     * @PostConditions:
     * - возвращается непустой токен
     * - токен валиден
     *
     * @Invariants:
     * - токен содержит username в subject
     * - токен содержит userId в claims
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку непустого токена
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки содержимого токена
     */
    @Test
    void generateToken_Success() {
        // Act
        String token = jwtTokenProvider.generateToken("testuser", 1L);

        // Assert
        assertNotNull(token, "Токен не должен быть null");
        assertFalse(token.isEmpty(), "Токен не должен быть пустым");
        assertTrue(jwtTokenProvider.validateToken(token), "Токен должен быть валидным");
    }
    // [END_JWT_TOKEN_PROVIDER_TEST_GENERATE_TOKEN_SUCCESS]

    // [START_JWT_TOKEN_PROVIDER_TEST_GENERATE_TOKEN_CONTAINS_USERNAME]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TEST_GENERATE_TOKEN_CONTAINS_USERNAME
     * PURPOSE: Проверка наличия username в subject токена.
     *
     * @PreConditions:
     * - токен сгенерирован
     *
     * @PostConditions:
     * - username извлекается из subject токена
     *
     * @Invariants:
     * - username соответствует переданному при генерации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку соответствия username
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных username
     */
    @Test
    void generateToken_ContainsUsername() {
        // Arrange
        String expectedUsername = "testuser";

        // Act
        String token = jwtTokenProvider.generateToken(expectedUsername, 1L);
        String actualUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(expectedUsername, actualUsername, "Username в токене должен соответствовать переданному");
    }
    // [END_JWT_TOKEN_PROVIDER_TEST_GENERATE_TOKEN_CONTAINS_USERNAME]

    // [START_JWT_TOKEN_PROVIDER_TEST_GENERATE_TOKEN_CONTAINS_USER_ID]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TEST_GENERATE_TOKEN_CONTAINS_USER_ID
     * PURPOSE: Проверка наличия userId в claims токена.
     *
     * @PreConditions:
     * - токен сгенерирован
     *
     * @PostConditions:
     * - userId извлекается из claims токена
     *
     * @Invariants:
     * - userId соответствует переданному при генерации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку соответствия userId
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных userId
     */
    @Test
    void generateToken_ContainsUserId() {
        // Arrange
        Long expectedUserId = 123L;

        // Act
        String token = jwtTokenProvider.generateToken("testuser", expectedUserId);
        Long actualUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(expectedUserId, actualUserId, "UserId в токене должен соответствовать переданному");
    }
    // [END_JWT_TOKEN_PROVIDER_TEST_GENERATE_TOKEN_CONTAINS_USER_ID]

    // [START_JWT_TOKEN_PROVIDER_TEST_GET_USERNAME_FROM_TOKEN_SUCCESS]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TEST_GET_USERNAME_FROM_TOKEN_SUCCESS
     * PURPOSE: Проверка успешного извлечения username из токена.
     *
     * @PreConditions:
     * - валидный токен сгенерирован
     *
     * @PostConditions:
     * - username успешно извлечён
     *
     * @Invariants:
     * - username соответствует переданному при генерации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку соответствия username
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных username
     */
    @Test
    void getUsernameFromToken_Success() {
        // Arrange
        String expectedUsername = "testuser";
        String token = jwtTokenProvider.generateToken(expectedUsername, 1L);

        // Act
        String actualUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(expectedUsername, actualUsername, "Username должен соответствовать переданному");
    }
    // [END_JWT_TOKEN_PROVIDER_TEST_GET_USERNAME_FROM_TOKEN_SUCCESS]

    // [START_JWT_TOKEN_PROVIDER_TEST_GET_USERNAME_FROM_TOKEN_INVALID_TOKEN]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TEST_GET_USERNAME_FROM_TOKEN_INVALID_TOKEN
     * PURPOSE: Проверка обработки неверного токена при извлечении username.
     *
     * @PreConditions:
     * - передан неверный токен
     *
     * @PostConditions:
     * - выбрасывается JwtException
     *
     * @Invariants:
     * - неверный токен не обрабатывается
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку исключения
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных типов неверных токенов
     */
    @Test
    void getUsernameFromToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(JwtException.class, () -> jwtTokenProvider.getUsernameFromToken(invalidToken),
                "Должно выбрасываться JwtException для неверного токена");
    }
    // [END_JWT_TOKEN_PROVIDER_TEST_GET_USERNAME_FROM_TOKEN_INVALID_TOKEN]

    // [START_JWT_TOKEN_PROVIDER_TEST_GET_USER_ID_FROM_TOKEN_SUCCESS]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TEST_GET_USER_ID_FROM_TOKEN_SUCCESS
     * PURPOSE: Проверка успешного извлечения userId из токена.
     *
     * @PreConditions:
     * - валидный токен сгенерирован
     *
     * @PostConditions:
     * - userId успешно извлечён
     *
     * @Invariants:
     * - userId соответствует переданному при генерации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку соответствия userId
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных userId
     */
    @Test
    void getUserIdFromToken_Success() {
        // Arrange
        Long expectedUserId = 456L;
        String token = jwtTokenProvider.generateToken("testuser", expectedUserId);

        // Act
        Long actualUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(expectedUserId, actualUserId, "UserId должен соответствовать переданному");
    }
    // [END_JWT_TOKEN_PROVIDER_TEST_GET_USER_ID_FROM_TOKEN_SUCCESS]

    // [START_JWT_TOKEN_PROVIDER_TEST_GET_USER_ID_FROM_TOKEN_INVALID_TOKEN]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TEST_GET_USER_ID_FROM_TOKEN_INVALID_TOKEN
     * PURPOSE: Проверка обработки неверного токена при извлечении userId.
     *
     * @PreConditions:
     * - передан неверный токен
     *
     * @PostConditions:
     * - выбрасывается JwtException
     *
     * @Invariants:
     * - неверный токен не обрабатывается
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку исключения
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных типов неверных токенов
     */
    @Test
    void getUserIdFromToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(JwtException.class, () -> jwtTokenProvider.getUserIdFromToken(invalidToken),
                "Должно выбрасываться JwtException для неверного токена");
    }
    // [END_JWT_TOKEN_PROVIDER_TEST_GET_USER_ID_FROM_TOKEN_INVALID_TOKEN]

    // [START_JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_VALID]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_VALID
     * PURPOSE: Проверка валидации валидного токена.
     *
     * @PreConditions:
     * - валидный токен сгенерирован
     *
     * @PostConditions:
     * - возвращается true
     *
     * @Invariants:
     * - валидный токен проходит валидацию
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку валидности
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных валидных токенов
     */
    @Test
    void validateToken_Valid() {
        // Arrange
        String token = jwtTokenProvider.generateToken("testuser", 1L);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid, "Валидный токен должен проходить валидацию");
    }
    // [END_JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_VALID]

    // [START_JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_INVALID]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_INVALID
     * PURPOSE: Проверка валидации неверного токена.
     *
     * @PreConditions:
     * - передан неверный токен
     *
     * @PostConditions:
     * - возвращается false
     *
     * @Invariants:
     * - неверный токен не проходит валидацию
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку невалидности
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных типов неверных токенов
     */
    @Test
    void validateToken_Invalid() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid, "Неверный токен не должен проходить валидацию");
    }
    // [END_JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_INVALID]

    // [START_JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_EXPIRED]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_EXPIRED
     * PURPOSE: Проверка валидации истекшего токена.
     *
     * @PreConditions:
     * - токен сгенерирован с коротким сроком действия
     * - срок действия истёк
     *
     * @PostConditions:
     * - возвращается false
     *
     * @Invariants:
     * - истекший токен не проходит валидацию
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку истечения срока
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных сроков действия
     */
    @Test
    void validateToken_Expired() {
        // Arrange
        long shortExpiration = 100L; // 100 мс
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", shortExpiration);
        String token = jwtTokenProvider.generateToken("testuser", 1L);

        // Ждём истечения срока действия
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertFalse(isValid, "Истекший токен не должен проходить валидацию");

        // Восстанавливаем исходное значение
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", TEST_EXPIRATION_MS);
    }
    // [END_JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_EXPIRED]

    // [START_JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_WRONG_SIGNATURE]
    /*
     * ANCHOR: JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_WRONG_SIGNATURE
     * PURPOSE: Проверка валидации токена с неверной подписью.
     *
     * @PreConditions:
     * - токен сгенерирован с другим секретным ключом
     *
     * @PostConditions:
     * - возвращается false
     *
     * @Invariants:
     * - токен с неверной подписью не проходит валидацию
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку подписи
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных секретных ключей
     */
    @Test
    void validateToken_WrongSignature() {
        // Arrange
        String token = jwtTokenProvider.generateToken("testuser", 1L);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "differentSecretKeyForTesting12345678901234567890");

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertFalse(isValid, "Токен с неверной подписью не должен проходить валидацию");

        // Восстанавливаем исходное значение
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
    }
    // [END_JWT_TOKEN_PROVIDER_TEST_VALIDATE_TOKEN_WRONG_SIGNATURE]
}
// [END_JWT_TOKEN_PROVIDER_TEST]
// === END_CHUNK: JWT_TOKEN_PROVIDER_TEST ===
