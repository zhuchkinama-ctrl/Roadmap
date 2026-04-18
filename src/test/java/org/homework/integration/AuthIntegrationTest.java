package org.homework.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.homework.dto.request.AuthRequest;
import org.homework.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// === CHUNK: AUTH_INTEGRATION_TEST [TEST] ===
// Описание: Интеграционные тесты для аутентификации (E2E сценарии).
// Dependencies: Spring Boot Test, MockMvc

// [START_AUTH_INTEGRATION_TEST]
/*
 * ANCHOR: AUTH_INTEGRATION_TEST
 * PURPOSE: Интеграционные тесты для аутентификации (E2E сценарии).
 *
 * @PreConditions:
 * - Spring Boot контекст инициализирован
 * - MockMvc доступен
 *
 * @PostConditions:
 * - все тесты проверяют сквозной поток аутентификации
 *
 * @Invariants:
 * - тесты используют реальные компоненты (не моки)
 *
 * @SideEffects:
 * - HTTP запросы к API
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку статусов ответов
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные E2E сценарии
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // [START_AUTH_INTEGRATION_TEST_REGISTER_AND_LOGIN]
    /*
     * ANCHOR: AUTH_INTEGRATION_TEST_REGISTER_AND_LOGIN
     * PURPOSE: E2E сценарий регистрации и входа.
     *
     * @PreConditions:
     * - пользователь не существует
     *
     * @PostConditions:
     * - пользователь регистрируется
     * - пользователь входит в систему
     * - возвращаются валидные токены
     *
     * @Invariants:
     * - токены не null
     *
     * @SideEffects:
     * - создание пользователя в БД
     * - генерация JWT токенов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку токенов
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки
     */
    @Test
    void registerAndLogin_E2E() throws Exception {
        // Arrange
        String timestamp = String.valueOf(System.currentTimeMillis());
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser" + timestamp);
        registerRequest.setEmail("test" + timestamp + "@example.com");
        registerRequest.setPassword("Password123!");

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(registerRequest.getUsername());
        authRequest.setPassword(registerRequest.getPassword());

        // Step 1: Register
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value(registerRequest.getUsername()))
                .andExpect(jsonPath("$.email").value(registerRequest.getEmail()))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true));

        // Step 2: Login
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
    // [END_AUTH_INTEGRATION_TEST_REGISTER_AND_LOGIN]

    // [START_AUTH_INTEGRATION_TEST_LOGIN_WITH_INVALID_CREDENTIALS]
    /*
     * ANCHOR: AUTH_INTEGRATION_TEST_LOGIN_WITH_INVALID_CREDENTIALS
     * PURPOSE: E2E сценарий входа с неверными данными.
     *
     * @PreConditions:
     * - пользователь существует
     * - неверный пароль
     *
     * @PostConditions:
     * - возвращается 401 Unauthorized
     *
     * @Invariants:
     * - пользователь не может войти с неверными данными
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки
     */
    @Test
    void login_WithInvalidCredentials_E2E() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuserinvalid" + System.currentTimeMillis());
        registerRequest.setEmail("testinvalid" + System.currentTimeMillis() + "@example.com");
        registerRequest.setPassword("Password123!");

        // First register a user
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Then try to login with wrong password
        AuthRequest invalidRequest = new AuthRequest();
        invalidRequest.setUsername(registerRequest.getUsername());
        invalidRequest.setPassword("WrongPass123!");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }
    // [END_AUTH_INTEGRATION_TEST_LOGIN_WITH_INVALID_CREDENTIALS]

    // [START_AUTH_INTEGRATION_TEST_REGISTER_WITH_DUPLICATE_USERNAME]
    /*
     * ANCHOR: AUTH_INTEGRATION_TEST_REGISTER_WITH_DUPLICATE_USERNAME
     * PURPOSE: E2E сценарий регистрации с дубликатом username.
     *
     * @PreConditions:
     * - пользователь уже существует
     *
     * @PostConditions:
     * - возвращается 409 Conflict
     *
     * @Invariants:
     * - пользователь не может быть создан дважды
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку уникальности username
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки
     */
    @Test
    void register_WithDuplicateUsername_E2E() throws Exception {
        // Arrange
        String uniqueUsername = "testuserdup" + System.currentTimeMillis();
        String uniqueEmail1 = "testdup1" + System.currentTimeMillis() + "@example.com";
        String uniqueEmail2 = "testdup2" + System.currentTimeMillis() + "@example.com";

        RegisterRequest firstRequest = new RegisterRequest();
        firstRequest.setUsername(uniqueUsername);
        firstRequest.setEmail(uniqueEmail1);
        firstRequest.setPassword("Password123!");

        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setUsername(uniqueUsername);
        duplicateRequest.setEmail(uniqueEmail2);
        duplicateRequest.setPassword("Password123!");

        // Step 1: Register first user
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // Step 2: Try to register duplicate user
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }
    // [END_AUTH_INTEGRATION_TEST_REGISTER_WITH_DUPLICATE_USERNAME]

    // [START_AUTH_INTEGRATION_TEST_REGISTER_WITH_DUPLICATE_EMAIL]
    /*
     * ANCHOR: AUTH_INTEGRATION_TEST_REGISTER_WITH_DUPLICATE_EMAIL
     * PURPOSE: E2E сценарий регистрации с дубликатом email.
     *
     * @PreConditions:
     * - пользователь уже существует с этим email
     *
     * @PostConditions:
     * - возвращается 409 Conflict
     *
     * @Invariants:
     * - пользователь не может быть создан дважды
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку уникальности email
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки
     */
    @Test
    void register_WithDuplicateEmail_E2E() throws Exception {
        // Arrange
        String uniqueUsername1 = "testuserdupemail1" + System.currentTimeMillis();
        String uniqueUsername2 = "testuserdupemail2" + System.currentTimeMillis();
        String uniqueEmail = "testdupemail" + System.currentTimeMillis() + "@example.com";

        RegisterRequest firstRequest = new RegisterRequest();
        firstRequest.setUsername(uniqueUsername1);
        firstRequest.setEmail(uniqueEmail);
        firstRequest.setPassword("Password123!");

        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setUsername(uniqueUsername2);
        duplicateRequest.setEmail(uniqueEmail);
        duplicateRequest.setPassword("Password123!");

        // Step 1: Register first user
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // Step 2: Try to register duplicate user
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }
    // [END_AUTH_INTEGRATION_TEST_REGISTER_WITH_DUPLICATE_EMAIL]

    // [START_AUTH_INTEGRATION_TEST_REGISTER_WITH_INVALID_PASSWORD]
    /*
     * ANCHOR: AUTH_INTEGRATION_TEST_REGISTER_WITH_INVALID_PASSWORD
     * PURPOSE: E2E сценарий регистрации с неверным паролем.
     *
     * @PreConditions:
     * - пароль не соответствует требованиям безопасности
     *
     * @PostConditions:
     * - возвращается 400 Bad Request
     *
     * @Invariants:
     * - пользователь не может быть создан с неверным паролем
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать валидацию пароля
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки
     */
    @Test
    void register_WithInvalidPassword_E2E() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setUsername("testuser_weak");
        invalidRequest.setEmail("test_weak@example.com");
        invalidRequest.setPassword("weak"); // Invalid - too short

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    // [END_AUTH_INTEGRATION_TEST_REGISTER_WITH_INVALID_PASSWORD]

    // [START_AUTH_INTEGRATION_TEST_LOGIN_WITH_INVALID_USERNAME]
    /*
     * ANCHOR: AUTH_INTEGRATION_TEST_LOGIN_WITH_INVALID_USERNAME
     * PURPOSE: E2E сценарий входа с несуществующим пользователем.
     *
     * @PreConditions:
     * - пользователь не существует
     *
     * @PostConditions:
     * - возвращается 401 Unauthorized
     *
     * @Invariants:
     * - несуществующий пользователь не может войти
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования пользователя
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки
     */
    @Test
    void login_WithInvalidUsername_E2E() throws Exception {
        // Arrange
        AuthRequest invalidRequest = new AuthRequest();
        invalidRequest.setUsername("nonexistentuser_" + System.currentTimeMillis());
        invalidRequest.setPassword("Password123!");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }
    // [END_AUTH_INTEGRATION_TEST_LOGIN_WITH_INVALID_USERNAME]
}
// [END_AUTH_INTEGRATION_TEST]
// === END_CHUNK: AUTH_INTEGRATION_TEST ===
