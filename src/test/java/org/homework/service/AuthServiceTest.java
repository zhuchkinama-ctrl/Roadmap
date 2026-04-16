package org.homework.service;

import org.homework.dto.request.AuthRequest;
import org.homework.dto.request.RegisterRequest;
import org.homework.dto.response.AuthResponse;
import org.homework.dto.response.UserDto;
import org.homework.exception.UserAlreadyExistsException;
import org.homework.model.User;
import org.homework.repository.UserRepository;
import org.homework.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// === CHUNK: AUTH_SERVICE_TEST [TEST] ===
// Описание: Юнит-тесты для AuthService.
// Dependencies: JUnit 5, Mockito, Spring Security

// [START_AUTH_SERVICE_TEST]
/*
 * ANCHOR: AUTH_SERVICE_TEST
 * PURPOSE: Юнит-тесты для AuthService.
 *
 * @PreConditions:
 * - AuthService инициализирован с моками зависимостей
 *
 * @PostConditions:
 * - все тесты проверяют контракты методов AuthService
 *
 * @Invariants:
 * - пароли всегда хешируются перед сохранением
 * - новый пользователь всегда имеет роль USER
 * - токены генерируются только для аутентифицированных пользователей
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку уникальности username/email
 * - нельзя убрать проверку хеширования пароля
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тестовые кейсы
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123!");

        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("Password123!");

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .role("USER")
                .enabled(true)
                .build();
    }

    // [START_AUTH_SERVICE_TEST_REGISTER_SUCCESS]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_REGISTER_SUCCESS
     * PURPOSE: Проверка успешной регистрации нового пользователя.
     *
     * @PreConditions:
     * - username и email уникальны
     *
     * @PostConditions:
     * - возвращается UserDto с id нового пользователя
     *
     * @Invariants:
     * - пароль хешируется перед сохранением
     * - новый пользователь имеет роль USER
     *
     * @SideEffects:
     * - пользователь сохраняется в БД
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку уникальности username/email
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные проверки полей
     */
    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = authService.register(registerRequest);

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(1L, result.getId(), "Id должен соответствовать сохранённому пользователю");
        assertEquals("testuser", result.getUsername(), "Username должен соответствовать");
        assertEquals("test@example.com", result.getEmail(), "Email должен соответствовать");
        assertEquals("USER", result.getRole(), "Роль должна быть USER");
        assertTrue(result.getEnabled(), "Пользователь должен быть включён");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
    }
    // [END_AUTH_SERVICE_TEST_REGISTER_SUCCESS]

    // [START_AUTH_SERVICE_TEST_REGISTER_DUPLICATE_USERNAME]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_REGISTER_DUPLICATE_USERNAME
     * PURPOSE: Проверка регистрации с существующим username.
     *
     * @PreConditions:
     * - username уже существует
     *
     * @PostConditions:
     * - выбрасывается UserAlreadyExistsException
     *
     * @Invariants:
     * - пользователь не сохраняется в БД
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку дубликата username
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void register_DuplicateUsername() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest),
                "Должно выбрасываться UserAlreadyExistsException для дубликата username");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    // [END_AUTH_SERVICE_TEST_REGISTER_DUPLICATE_USERNAME]

    // [START_AUTH_SERVICE_TEST_REGISTER_DUPLICATE_EMAIL]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_REGISTER_DUPLICATE_EMAIL
     * PURPOSE: Проверка регистрации с существующим email.
     *
     * @PreConditions:
     * - email уже существует
     *
     * @PostConditions:
     * - выбрасывается UserAlreadyExistsException
     *
     * @Invariants:
     * - пользователь не сохраняется в БД
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку дубликата email
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void register_DuplicateEmail() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest),
                "Должно выбрасываться UserAlreadyExistsException для дубликата email");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
    // [END_AUTH_SERVICE_TEST_REGISTER_DUPLICATE_EMAIL]

    // [START_AUTH_SERVICE_TEST_REGISTER_PASSWORD_HASHED]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_REGISTER_PASSWORD_HASHED
     * PURPOSE: Проверка хеширования пароля при регистрации.
     *
     * @PreConditions:
     * - пароль передан в открытом виде
     *
     * @PostConditions:
     * - пароль хешируется перед сохранением
     *
     * @Invariants:
     * - пароль никогда не хранится в открытом виде
     *
     * @SideEffects:
     * - пароль хешируется
     *
     * @ForbiddenChanges:
     * - нельзя убрать хеширование пароля
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку алгоритма хеширования
     */
    @Test
    void register_PasswordHashed() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.register(registerRequest);

        // Assert
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(argThat(user -> "hashedPassword".equals(user.getPasswordHash())));
    }
    // [END_AUTH_SERVICE_TEST_REGISTER_PASSWORD_HASHED]

    // [START_AUTH_SERVICE_TEST_REGISTER_DEFAULT_ROLE]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_REGISTER_DEFAULT_ROLE
     * PURPOSE: Проверка роли по умолчанию при регистрации.
     *
     * @PreConditions:
     * - новый пользователь создаётся
     *
     * @PostConditions:
     * - новый пользователь имеет роль USER
     *
     * @Invariants:
     * - новый пользователь всегда имеет роль USER
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя изменить роль по умолчанию без согласования
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для других ролей
     */
    @Test
    void register_DefaultRole() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = authService.register(registerRequest);

        // Assert
        assertEquals("USER", result.getRole(), "Роль по умолчанию должна быть USER");
    }
    // [END_AUTH_SERVICE_TEST_REGISTER_DEFAULT_ROLE]

    // [START_AUTH_SERVICE_TEST_REGISTER_DATABASE_ERROR]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_REGISTER_DATABASE_ERROR
     * PURPOSE: Проверка обработки ошибки при сохранении в БД.
     *
     * @PreConditions:
     * - ошибка при сохранении в БД
     *
     * @PostConditions:
     * - выбрасывается RuntimeException
     *
     * @Invariants:
     * - ошибка обрабатывается корректно
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать обработку ошибки
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void register_DatabaseError() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest),
                "Должно выбрасываться RuntimeException при ошибке БД");

        verify(userRepository).save(any(User.class));
    }
    // [END_AUTH_SERVICE_TEST_REGISTER_DATABASE_ERROR]

    // [START_AUTH_SERVICE_TEST_LOGIN_SUCCESS]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_LOGIN_SUCCESS
     * PURPOSE: Проверка успешной аутентификации.
     *
     * @PreConditions:
     * - пользователь существует
     * - пароль верный
     *
     * @PostConditions:
     * - возвращается AuthResponse с токенами
     *
     * @Invariants:
     * - токены генерируются только для аутентифицированных пользователей
     *
     * @SideEffects:
     * - генерация JWT токенов
     *
     * @ForbiddenChanges:
     * - нельзя убрать аутентификацию через AuthenticationManager
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку содержимого токенов
     */
    @Test
    void login_Success() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken("testuser", 1L)).thenReturn("testToken");

        // Act
        AuthResponse result = authService.login(authRequest);

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals("testToken", result.getToken(), "Токен должен соответствовать");
        assertEquals("testToken", result.getRefreshToken(), "RefreshToken должен соответствовать");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtTokenProvider, times(2)).generateToken("testuser", 1L);
    }
    // [END_AUTH_SERVICE_TEST_LOGIN_SUCCESS]

    // [START_AUTH_SERVICE_TEST_LOGIN_AUTHENTICATION_FAILED]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_LOGIN_AUTHENTICATION_FAILED
     * PURPOSE: Проверка неудачной аутентификации.
     *
     * @PreConditions:
     * - неверный пароль
     *
     * @PostConditions:
     * - выбрасывается RuntimeException
     *
     * @Invariants:
     * - неверный пароль не даёт доступ
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void login_AuthenticationFailed() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.login(authRequest),
                "Должно выбрасываться RuntimeException при неудачной аутентификации");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }
    // [END_AUTH_SERVICE_TEST_LOGIN_AUTHENTICATION_FAILED]

    // [START_AUTH_SERVICE_TEST_LOGIN_USER_NOT_FOUND]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_LOGIN_USER_NOT_FOUND
     * PURPOSE: Проверка аутентификации с несуществующим пользователем.
     *
     * @PreConditions:
     * - пользователь не найден в БД
     *
     * @PostConditions:
     * - выбрасывается RuntimeException
     *
     * @Invariants:
     * - несуществующий пользователь не может аутентифицироваться
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования пользователя
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void login_UserNotFound() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.login(authRequest),
                "Должно выбрасываться RuntimeException при отсутствии пользователя");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong());
    }
    // [END_AUTH_SERVICE_TEST_LOGIN_USER_NOT_FOUND]

    // [START_AUTH_SERVICE_TEST_LOGIN_TOKENS_GENERATED]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_LOGIN_TOKENS_GENERATED
     * PURPOSE: Проверка генерации токенов при аутентификации.
     *
     * @PreConditions:
     * - успешная аутентификация
     *
     * @PostConditions:
     * - accessToken и refreshToken не null
     *
     * @Invariants:
     * - токены генерируются для аутентифицированного пользователя
     *
     * @SideEffects:
     * - генерация JWT токенов
     *
     * @ForbiddenChanges:
     * - нельзя убрать генерацию токенов
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку содержимого токенов
     */
    @Test
    void login_TokensGenerated() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken("testuser", 1L)).thenReturn("testToken");

        // Act
        AuthResponse result = authService.login(authRequest);

        // Assert
        assertNotNull(result.getToken(), "AccessToken не должен быть null");
        assertNotNull(result.getRefreshToken(), "RefreshToken не должен быть null");

        verify(jwtTokenProvider, times(2)).generateToken("testuser", 1L);
    }
    // [END_AUTH_SERVICE_TEST_LOGIN_TOKENS_GENERATED]

    // [START_AUTH_SERVICE_TEST_GET_CURRENT_USER_SUCCESS]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_GET_CURRENT_USER_SUCCESS
     * PURPOSE: Проверка получения существующего пользователя.
     *
     * @PreConditions:
     * - пользователь существует
     *
     * @PostConditions:
     * - возвращается UserDto с данными пользователя
     *
     * @Invariants:
     * - пароль не возвращается в ответе
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования пользователя
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку дополнительных полей
     */
    @Test
    void getCurrentUser_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDto result = authService.getCurrentUser("testuser");

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(1L, result.getId(), "Id должен соответствовать");
        assertEquals("testuser", result.getUsername(), "Username должен соответствовать");
        assertEquals("test@example.com", result.getEmail(), "Email должен соответствовать");
        assertEquals("USER", result.getRole(), "Роль должна соответствовать");
        assertTrue(result.getEnabled(), "Пользователь должен быть включён");

        verify(userRepository).findByUsername("testuser");
    }
    // [END_AUTH_SERVICE_TEST_GET_CURRENT_USER_SUCCESS]

    // [START_AUTH_SERVICE_TEST_GET_CURRENT_USER_NOT_FOUND]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_GET_CURRENT_USER_NOT_FOUND
     * PURPOSE: Проверка получения несуществующего пользователя.
     *
     * @PreConditions:
     * - пользователь не существует
     *
     * @PostConditions:
     * - выбрасывается RuntimeException
     *
     * @Invariants:
     * - несуществующий пользователь не может быть получен
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования пользователя
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void getCurrentUser_NotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.getCurrentUser("nonexistent"),
                "Должно выбрасываться RuntimeException при отсутствии пользователя");

        verify(userRepository).findByUsername("nonexistent");
    }
    // [END_AUTH_SERVICE_TEST_GET_CURRENT_USER_NOT_FOUND]

    // [START_AUTH_SERVICE_TEST_GET_CURRENT_USER_NO_PASSWORD]
    /*
     * ANCHOR: AUTH_SERVICE_TEST_GET_CURRENT_USER_NO_PASSWORD
     * PURPOSE: Проверка отсутствия пароля в UserDto.
     *
     * @PreConditions:
     * - пользователь существует
     *
     * @PostConditions:
     * - пароль не включён в UserDto
     *
     * @Invariants:
     * - пароль никогда не возвращается в ответе
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя включать пароль в DTO
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку других чувствительных полей
     */
    @Test
    void getCurrentUser_NoPassword() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDto result = authService.getCurrentUser("testuser");

        // Assert
        // UserDto не имеет поля password, поэтому проверяем, что все остальные поля присутствуют
        assertNotNull(result.getId(), "Id должен быть присутствовать");
        assertNotNull(result.getUsername(), "Username должен быть присутствовать");
        assertNotNull(result.getEmail(), "Email должен быть присутствовать");
        assertNotNull(result.getRole(), "Role должен быть присутствовать");
        assertNotNull(result.getEnabled(), "Enabled должен быть присутствовать");
    }
    // [END_AUTH_SERVICE_TEST_GET_CURRENT_USER_NO_PASSWORD]
}
// [END_AUTH_SERVICE_TEST]
// === END_CHUNK: AUTH_SERVICE_TEST ===
