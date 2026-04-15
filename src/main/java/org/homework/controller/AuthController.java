// === CHUNK: AUTH_CONTROLLER [API] ===
// Описание: REST-контроллер для аутентификации и регистрации пользователей.
// Dependencies: Spring Web, Validation, AuthService, DTOs

package org.homework.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.homework.dto.request.AuthRequest;
import org.homework.dto.request.RegisterRequest;
import org.homework.dto.response.AuthResponse;
import org.homework.dto.response.UserDto;
import org.homework.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// [START_AUTH_CONTROLLER]
/*
 * ANCHOR: AUTH_CONTROLLER
 * PURPOSE: REST-контроллер для аутентификации и регистрации пользователей.
 *
 * @PreConditions:
 * - Spring контекст инициализирован
 * - AuthService доступен через DI
 *
 * @PostConditions:
 * - эндпоинты /register и /login доступны по /api/v1/auth
 *
 * @Invariants:
 * - все методы возвращают ResponseEntity
 *
 * @SideEffects:
 * - HTTP запросы к сервису аутентификации
 * - запись в лог
 *
 * @ForbiddenChanges:
 * - нельзя изменить базовый путь /api/v1/auth без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные эндпоинты (например, /logout, /refresh)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // [START_AUTH_CONTROLLER_REGISTER]
    /*
     * ANCHOR: AUTH_CONTROLLER_REGISTER
     * PURPOSE: Регистрация нового пользователя.
     *
     * @PreConditions:
     * - request прошёл валидацию (@Valid)
     * - username уникален
     * - email уникален
     *
     * @PostConditions:
     * - при успехе: возвращается 200 OK с UserDto
     * - при ошибке: исключение обрабатывается GlobalExceptionHandler
     *
     * @Invariants:
     * - пароль никогда не возвращается в ответе
     *
     * @SideEffects:
     * - создаётся запись пользователя в БД
     * - пароль хешируется
     *
     * @ForbiddenChanges:
     * - нельзя убрать валидацию @Valid
     * - нельзя возвращать пароль в ответе
     *
     * @AllowedRefactorZone:
     * - можно изменить формат логирования
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        log.info("AUTH_CONTROLLER_REGISTER ENTRY - username: {}, email: {}", request.getUsername(), request.getEmail());
        log.debug("Request validation passed for username: {}", request.getUsername());

        try {
            UserDto userDto = authService.register(request);
            log.info("AUTH_CONTROLLER_REGISTER STATE_CHANGE - user created - username: {}, userId: {}", request.getUsername(), userDto.getId());
            log.info("AUTH_CONTROLLER_REGISTER EXIT - success - username: {}", request.getUsername());
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            log.error("AUTH_CONTROLLER_REGISTER ERROR - username: {}, error: {}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }
    // [END_AUTH_CONTROLLER_REGISTER]

    // [START_AUTH_CONTROLLER_LOGIN]
    /*
     * ANCHOR: AUTH_CONTROLLER_LOGIN
     * PURPOSE: Аутентификация пользователя.
     *
     * @PreConditions:
     * - request прошёл валидацию (@Valid)
     * - пользователь существует
     * - пароль верный
     *
     * @PostConditions:
     * - при успехе: возвращается 200 OK с AuthResponse (accessToken, refreshToken)
     * - при ошибке: исключение обрабатывается GlobalExceptionHandler
     *
     * @Invariants:
     * - токены генерируются только для аутентифицированных пользователей
     *
     * @SideEffects:
     * - генерация JWT токенов
     *
     * @ForbiddenChanges:
     * - нельзя убрать валидацию @Valid
     * - нельзя возвращать пароль в ответе
     *
     * @AllowedRefactorZone:
     * - можно изменить формат логирования
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("AUTH_CONTROLLER_LOGIN ENTRY - username: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        log.info("AUTH_CONTROLLER_LOGIN EXIT - success - username: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }
    // [END_AUTH_CONTROLLER_LOGIN]
}
// [END_AUTH_CONTROLLER]
// === END_CHUNK: AUTH_CONTROLLER ===
