// === CHUNK: AUTH_SERVICE [SERVICE] ===
// Описание: Сервис аутентификации и регистрации пользователей.
// Dependencies: Spring Security, JWT, UserRepository, PasswordEncoder

package org.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.homework.dto.request.AuthRequest;
import org.homework.dto.request.RegisterRequest;
import org.homework.dto.response.AuthResponse;
import org.homework.dto.response.UserDto;
import org.homework.exception.UserAlreadyExistsException;
import org.homework.model.User;
import org.homework.repository.UserRepository;
import org.homework.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// [START_AUTH_SERVICE]
/*
 * ANCHOR: AUTH_SERVICE
 * PURPOSE: Сервис аутентификации и регистрации пользователей.
 *
 * @PreConditions:
 * - Spring контекст инициализирован
 * - все зависимости доступны через DI
 *
 * @PostConditions:
 * - сервис готов к обработке запросов аутентификации и регистрации
 *
 * @Invariants:
 * - пароли никогда не хранятся в открытом виде
 *
 * @SideEffects:
 * - запись в БД при регистрации
 * - генерация JWT токенов при логине
 *
 * @ForbiddenChanges:
 * - нельзя убрать хеширование паролей
 * - нельзя убрать валидацию уникальности username/email
 *
 * @AllowedRefactorZone:
 * - можно изменить алгоритм хеширования паролей
 * - можно изменить логику генерации токенов
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // [START_AUTH_SERVICE_REGISTER]
    /*
     * ANCHOR: AUTH_SERVICE_REGISTER
     * PURPOSE: Регистрация нового пользователя.
     *
     * @PreConditions:
     * - request валиден
     * - username уникален
     * - email уникален
     *
     * @PostConditions:
     * - при успехе: возвращается UserDto с id нового пользователя
     * - при дубликате username: выбрасывается UserAlreadyExistsException
     * - при дубликате email: выбрасывается UserAlreadyExistsException
     *
     * @Invariants:
     * - пароль всегда хешируется перед сохранением
     * - новый пользователь всегда имеет роль USER
     *
     * @SideEffects:
     * - создаётся запись пользователя в БД
     * - пароль хешируется
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку уникальности username
     * - нельзя убрать проверку уникальности email
     * - нельзя убрать хеширование пароля
     *
     * @AllowedRefactorZone:
     * - можно изменить роль по умолчанию
     * - можно добавить дополнительные поля валидации
     */
    @Transactional
    public UserDto register(RegisterRequest request) {
        log.info("AUTH_SERVICE_REGISTER ENTRY - username: {}, email: {}", request.getUsername(), request.getEmail());
        log.debug("AUTH_SERVICE_REGISTER CHECK - password length: {}", request.getPassword() != null ? request.getPassword().length() : 0);

        // Проверка существования пользователя по username
        boolean usernameExists = userRepository.existsByUsername(request.getUsername());
        log.debug("AUTH_SERVICE_REGISTER CHECK - username {} exists: {}", request.getUsername(), usernameExists);
        
        if (usernameExists) {
            log.warn("AUTH_SERVICE_REGISTER DECISION - reject duplicate username: {}", request.getUsername());
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        // Проверка существования пользователя по email
        boolean emailExists = userRepository.existsByEmail(request.getEmail());
        log.debug("AUTH_SERVICE_REGISTER CHECK - email {} exists: {}", request.getEmail(), emailExists);
        
        if (emailExists) {
            log.warn("AUTH_SERVICE_REGISTER DECISION - reject duplicate email: {}", request.getEmail());
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        // Создание нового пользователя
        log.info("AUTH_SERVICE_REGISTER STATE_CHANGE - creating user - username: {}", request.getUsername());
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .enabled(true)
                .build();

        try {
            user = userRepository.save(user);
            log.info("AUTH_SERVICE_REGISTER STATE_CHANGE - user created - id: {}, username: {}", user.getId(), user.getUsername());
        } catch (Exception e) {
            log.error("AUTH_SERVICE_REGISTER ERROR - failed to save user: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось сохранить пользователя в базе данных", e);
        }

        log.info("AUTH_SERVICE_REGISTER EXIT - success - username: {}", request.getUsername());
        return mapToUserDto(user);
    }
    // [END_AUTH_SERVICE_REGISTER]

    // [START_AUTH_SERVICE_LOGIN]
    /*
     * ANCHOR: AUTH_SERVICE_LOGIN
     * PURPOSE: Аутентификация пользователя.
     *
     * @PreConditions:
     * - request валиден
     * - пользователь существует
     * - пароль верный
     *
     * @PostConditions:
     * - при успехе: возвращается AuthResponse с accessToken и refreshToken
     * - при ошибке: выбрасывается исключение аутентификации
     *
     * @Invariants:
     * - токены генерируются только для аутентифицированных пользователей
     *
     * @SideEffects:
     * - генерация JWT токенов
     *
     * @ForbiddenChanges:
     * - нельзя убрать аутентификацию через AuthenticationManager
     * - нельзя убрать генерацию токенов
     *
     * @AllowedRefactorZone:
     * - можно изменить срок действия токенов
     * - можно добавить дополнительные данные в токен
     */
    public AuthResponse login(AuthRequest request) {
        log.info("AUTH_SERVICE_LOGIN ENTRY - username: {}", request.getUsername());

        // Аутентификация пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Явная проверка аутентификации
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("AUTH_SERVICE_LOGIN DECISION - authentication failed - username: {}", request.getUsername());
            throw new RuntimeException("Аутентификация не удалась");
        }

        // Получение пользователя из базы данных
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Генерация JWT токенов
        String token = jwtTokenProvider.generateToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateToken(user.getUsername());

        log.info("AUTH_SERVICE_LOGIN STATE_CHANGE - tokens generated - username: {}", request.getUsername());
        log.info("AUTH_SERVICE_LOGIN EXIT - success - username: {}", request.getUsername());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }
    // [END_AUTH_SERVICE_LOGIN]

    // [START_AUTH_SERVICE_GET_CURRENT_USER]
    /*
     * ANCHOR: AUTH_SERVICE_GET_CURRENT_USER
     * PURPOSE: Получение текущего пользователя по username.
     *
     * @PreConditions:
     * - username валиден
     * - пользователь существует
     *
     * @PostConditions:
     * - при успехе: возвращается UserDto
     * - при отсутствии: выбрасывается RuntimeException
     *
     * @Invariants:
     * - пароль никогда не возвращается в ответе
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя возвращать пароль в ответе
     *
     * @AllowedRefactorZone:
     * - можно добавить кэширование
     */
    public UserDto getCurrentUser(String username) {
        log.info("AUTH_SERVICE_GET_CURRENT_USER ENTRY - username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        log.info("AUTH_SERVICE_GET_CURRENT_USER EXIT - success - username: {}", username);
        return mapToUserDto(user);
    }
    // [END_AUTH_SERVICE_GET_CURRENT_USER]

    // [START_AUTH_SERVICE_MAP_TO_USER_DTO]
    /*
     * ANCHOR: AUTH_SERVICE_MAP_TO_USER_DTO
     * PURPOSE: Маппинг сущности User в UserDto.
     *
     * @PreConditions:
     * - user не null
     *
     * @PostConditions:
     * - возвращается UserDto с данными пользователя
     *
     * @Invariants:
     * - пароль никогда не включается в DTO
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя включать пароль в DTO
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные поля в DTO
     */
    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
    // [END_AUTH_SERVICE_MAP_TO_USER_DTO]
}
// [END_AUTH_SERVICE]
// === END_CHUNK: AUTH_SERVICE ===