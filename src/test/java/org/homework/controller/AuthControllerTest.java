package org.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.homework.dto.request.AuthRequest;
import org.homework.dto.request.RegisterRequest;
import org.homework.dto.response.AuthResponse;
import org.homework.dto.response.UserDto;
import org.homework.exception.GlobalExceptionHandler;
import org.homework.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// === CHUNK: AUTH_CONTROLLER_TEST [TEST] ===
// Описание: Юнит-тесты для AuthController.
// Dependencies: JUnit 5, Spring Test, Mockito

// [START_AUTH_CONTROLLER_TEST]
/*
 * ANCHOR: AUTH_CONTROLLER_TEST
 * PURPOSE: Юнит-тесты для AuthController.
 *
 * @PreConditions:
 * - AuthController инициализирован с моками зависимостей
 *
 * @PostConditions:
 * - все тесты проверяют контракты методов AuthController
 *
 * @Invariants:
 * - контроллер не содержит бизнес-логики
 * - все запросы проходят через валидацию DTO
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать валидацию @Valid
 * - нельзя убрать обработку исключений
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тестовые кейсы
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123!");

        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("Password123!");
    }

    // [START_AUTH_CONTROLLER_TEST_REGISTER_SUCCESS]
    /*
     * ANCHOR: AUTH_CONTROLLER_TEST_REGISTER_SUCCESS
     * PURPOSE: Проверка успешной регистрации через контроллер.
     *
     * @PreConditions:
     * - request валиден
     * - пользователь не существует
     *
     * @PostConditions:
     * - возвращается 200 OK с UserDto
     *
     * @Invariants:
     * - пароль не возвращается в ответе
     *
     * @SideEffects:
     * - вызывается authService.register()
     *
     * @ForbiddenChanges:
     * - нельзя убрать валидацию @Valid
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки полей
     */
    @Test
    void register_Success() throws Exception {
        // Arrange
        UserDto expectedUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .enabled(true)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(expectedUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(authService).register(any(RegisterRequest.class));
    }
    // [END_AUTH_CONTROLLER_TEST_REGISTER_SUCCESS]

    // [START_AUTH_CONTROLLER_TEST_REGISTER_VALIDATION_ERROR]
    /*
     * ANCHOR: AUTH_CONTROLLER_TEST_REGISTER_VALIDATION_ERROR
     * PURPOSE: Проверка обработки ошибки валидации при регистрации.
     *
     * @PreConditions:
     * - request не проходит валидацию (пустой username)
     *
     * @PostConditions:
     * - возвращается 400 Bad Request
     *
     * @Invariants:
     * - контроллер не вызывает сервис при ошибке валидации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать валидацию @Valid
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки
     */
    @Test
    void register_ValidationError() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setUsername(""); // Invalid
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("Password123!");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }
    // [END_AUTH_CONTROLLER_TEST_REGISTER_VALIDATION_ERROR]

    // [START_AUTH_CONTROLLER_TEST_REGISTER_SERVICE_ERROR]
    /*
     * ANCHOR: AUTH_CONTROLLER_TEST_REGISTER_SERVICE_ERROR
     * PURPOSE: Проверка обработки ошибки сервиса при регистрации.
     *
     * @PreConditions:
     * - сервис выбрасывает исключение
     *
     * @PostConditions:
     * - возвращается 500 Internal Server Error
     *
     * @Invariants:
     * - ошибка проксируется через контроллер
     *
     * @SideEffects:
     * - вызывается authService.register()
     *
     * @ForbiddenChanges:
     * - нельзя убрать обработку исключений
     *
     * @AllowedRefactorZone:
     * - можно добавить кастомный обработчик ошибок
     */
    @Test
    void register_ServiceError() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError());

        verify(authService).register(any(RegisterRequest.class));
    }
    // [END_AUTH_CONTROLLER_TEST_REGISTER_SERVICE_ERROR]

    // [START_AUTH_CONTROLLER_TEST_LOGIN_SUCCESS]
    /*
     * ANCHOR: AUTH_CONTROLLER_TEST_LOGIN_SUCCESS
     * PURPOSE: Проверка успешного входа через контроллер.
     *
     * @PreConditions:
     * - request валиден
     * - пользователь существует
     * - пароль верный
     *
     * @PostConditions:
     * - возвращается 200 OK с AuthResponse (accessToken, refreshToken)
     *
     * @Invariants:
     * - токены не null
     *
     * @SideEffects:
     * - вызывается authService.login()
     *
     * @ForbiddenChanges:
     * - нельзя убрать валидацию @Valid
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки полей
     */
    @Test
    void login_Success() throws Exception {
        // Arrange
        AuthResponse expectedResponse = AuthResponse.builder()
                .token("accessToken123")
                .refreshToken("refreshToken456")
                .build();

        when(authService.login(any(AuthRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("accessToken123"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken456"));

        verify(authService).login(any(AuthRequest.class));
    }
    // [END_AUTH_CONTROLLER_TEST_LOGIN_SUCCESS]

    // [START_AUTH_CONTROLLER_TEST_LOGIN_VALIDATION_ERROR]
    /*
     * ANCHOR: AUTH_CONTROLLER_TEST_LOGIN_VALIDATION_ERROR
     * PURPOSE: Проверка обработки ошибки валидации при входе.
     *
     * @PreConditions:
     * - request не проходит валидацию (пустой password)
     *
     * @PostConditions:
     * - возвращается 400 Bad Request
     *
     * @Invariants:
     * - контроллер не вызывает сервис при ошибке валидации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать валидацию @Valid
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки
     */
    @Test
    void login_ValidationError() throws Exception {
        // Arrange
        AuthRequest invalidRequest = new AuthRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setPassword(""); // Invalid

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(AuthRequest.class));
    }
    // [END_AUTH_CONTROLLER_TEST_LOGIN_VALIDATION_ERROR]

    // [START_AUTH_CONTROLLER_TEST_LOGIN_SERVICE_ERROR]
    /*
     * ANCHOR: AUTH_CONTROLLER_TEST_LOGIN_SERVICE_ERROR
     * PURPOSE: Проверка обработки ошибки сервиса при входе.
     *
     * @PreConditions:
     * - сервис выбрасывает исключение аутентификации
     *
     * @PostConditions:
     * - возвращается 401 Unauthorized
     *
     * @Invariants:
     * - ошибка проксируется через контроллер
     *
     * @SideEffects:
     * - вызывается authService.login()
     *
     * @ForbiddenChanges:
     * - нельзя убрать обработку исключений
     *
     * @AllowedRefactorZone:
     * - можно добавить кастомный обработчик ошибок
     */
    @Test
    void login_ServiceError() throws Exception {
        // Arrange
        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new AuthenticationException("Authentication failed") {});

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());

        verify(authService).login(any(AuthRequest.class));
    }
    // [END_AUTH_CONTROLLER_TEST_LOGIN_SERVICE_ERROR]
}
// [END_AUTH_CONTROLLER_TEST]
// === END_CHUNK: AUTH_CONTROLLER_TEST ===
