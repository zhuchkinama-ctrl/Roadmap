# Feature Specification: FEAT-001

## Status
planning

## Overview
Аутентификация и авторизация пользователей. Реализация регистрации, входа, JWT токенов, refresh токенов и защиты API endpoints.

## Requirements

### Functional Requirements
- FR-001: Пользователь может зарегистрироваться с username (3-50 символов, только латиница, без пробелов) и password (минимум 8 символов, 1 цифра, 1 заглавная буква, 1 специальный символ)
- FR-002: Пользователь может войти в систему с username и password
- FR-003: Система выдаёт access token (JWT) и refresh token при успешной аутентификации
- FR-004: Access token имеет ограниченное время жизни (например, 15 минут)
- FR-005: Refresh token имеет более длительное время жизни (например, 7 дней)
- FR-006: Пользователь может обновить access token с помощью refresh token
- FR-007: Пользователь может выйти из системы (отзыв refresh token)
- FR-008: Все запросы, кроме регистрации/входа/обновления токена, требуют валидный JWT

### Non-Functional Requirements
- NFR-001: Время ответа API для аутентификации ≤ 300 мс для 95% запросов
- NFR-002: Система должна поддерживать до 100 одновременных пользователей
- NFR-003: Пароли хешируются с помощью BCrypt
- NFR-004: JWT токены хранятся в HTTP-only cookies или localStorage
- NFR-005: Защита от CSRF атак (отключена для API, токен-базированная аутентификация)

## Architecture

### Backend Components
- AuthController (ANCHOR: AUTH_CONTROLLER)
- AuthService (ANCHOR: AUTH_SERVICE)
- UserRepository (ANCHOR: USER_REPOSITORY)
- User (ANCHOR: USER_MODEL)
- AuthRequest (ANCHOR: AUTH_REQUEST)
- RegisterRequest (ANCHOR: REGISTER_REQUEST)
- AuthResponse (ANCHOR: AUTH_RESPONSE)
- UserDto (ANCHOR: USER_DTO)
- JwtTokenProvider (ANCHOR: JWT_TOKEN_PROVIDER)
- JwtAuthenticationFilter (ANCHOR: JWT_AUTH_FILTER)
- SecurityConfig (ANCHOR: SECURITY_CONFIG)
- CustomUserDetailsService (ANCHOR: USER_DETAILS_SERVICE)

### Frontend Components
- LoginComponent (ANCHOR: LOGIN_COMPONENT)
- RegisterComponent (ANCHOR: REGISTER_COMPONENT)
- AuthService (ANCHOR: AUTH_SERVICE_FRONTEND)
- AuthGuard (ANCHOR: AUTH_GUARD)
- AuthInterceptor (ANCHOR: AUTH_INTERCEPTOR)
- UserModel (ANCHOR: USER_MODEL_FRONTEND)

### Data Flow
```
Frontend (Login/Register) → AuthInterceptor → Backend (AuthController)
→ AuthService → UserRepository → PostgreSQL
→ JWT Token → AuthInterceptor → Frontend
```

## Contracts

### Backend Component 1 (ANCHOR: AUTH_CONTROLLER)
```
[START_AUTH_CONTROLLER]
/*
 * ANCHOR: AUTH_CONTROLLER
 * PURPOSE: REST-эндпоинты для аутентификации и авторизации
 *
 * @PreConditions:
 * - Spring MVC контекст инициализирован
 * - AuthService внедрён через DI
 * - JWT токен валиден для защищённых endpoints
 *
 * @PostConditions:
 * - при успехе: возвращается AuthResponse с токенами
 * - при ошибке: выбрасывается исключение и возвращается ErrorResponseDto
 *
 * @Invariants:
 * - контроллер не содержит бизнес-логики
 * - все запросы проходят через валидацию DTO
 * - токены не логируются в открытом виде
 *
 * @SideEffects:
 * - вызывает методы AuthService
 * - возвращает HTTP ответы с правильными статусами
 *
 * @ForbiddenChanges:
 * - нельзя убрать валидацию DTO (@Valid)
 * - нельзя убрать обработку исключений
 * - нельзя возвращать пароли в ответах
 *
 * @AllowedRefactorZone:
 * - можно изменить формат логирования
 * - можно добавить дополнительные проверки валидации
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("AUTH_CONTROLLER ENTRY - register request received");
        AuthResponse response = authService.register(request);
        log.info("AUTH_CONTROLLER EXIT - registration successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("AUTH_CONTROLLER ENTRY - login request received");
        AuthResponse response = authService.login(request);
        log.info("AUTH_CONTROLLER EXIT - login successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        log.info("AUTH_CONTROLLER ENTRY - refresh request received");
        String refreshToken = request.get("refreshToken");
        AuthResponse response = authService.refresh(refreshToken);
        log.info("AUTH_CONTROLLER EXIT - refresh successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> request, Authentication authentication) {
        log.info("AUTH_CONTROLLER ENTRY - logout request received");
        String refreshToken = request.get("refreshToken");
        authService.logout(refreshToken);
        log.info("AUTH_CONTROLLER EXIT - logout successful");
        return ResponseEntity.ok().build();
    }
}
// [END_AUTH_CONTROLLER]
```

### Backend Component 2 (ANCHOR: AUTH_SERVICE)
```
[START_AUTH_SERVICE]
/*
 * ANCHOR: AUTH_SERVICE
 * PURPOSE: Управление аутентификацией и авторизацией пользователей
 *
 * @PreConditions:
 * - username: непустая строка после trim, min 3 символа
 * - password: непустая строка, min 8 символов
 * - пользователь с таким username не существует (для регистрации)
 *
 * @PostConditions:
 * - при успехе регистрации: возвращается AuthResponse с access_token и refresh_token
 * - при ошибке валидации: выбрасывается ValidationException
 * - при ошибке дубликата: выбрасывается UserAlreadyExistsException
 *
 * @Invariants:
 * - пароль никогда не хранится в открытом виде
 * - access_token имеет ограниченное время жизни
 * - refresh_token имеет более длительное время жизни
 *
 * @SideEffects:
 * - создаёт запись пользователя в БД
 * - генерирует JWT токены
 * - пишет лог регистрации в audit log
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку длины пароля (требование безопасности)
 * - нельзя убрать хеширование пароля (безопасность)
 * - нельзя возвращать пароль в ответе (безопасность)
 *
 * @AllowedRefactorZone:
 * - можно менять алгоритм хеширования пароля
 * - можно вынести валидацию в отдельную функцию
 * - можно изменить формат логирования
 */
@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        log.info("AUTH_SERVICE ENTRY - register request received");
        // реализация
        log.info("AUTH_SERVICE EXIT - registration successful");
        return new AuthResponse();
    }

    public AuthResponse login(AuthRequest request) {
        log.info("AUTH_SERVICE ENTRY - login request received");
        // реализация
        log.info("AUTH_SERVICE EXIT - login successful");
        return new AuthResponse();
    }

    public AuthResponse refresh(String refreshToken) {
        log.info("AUTH_SERVICE ENTRY - refresh request received");
        // реализация
        log.info("AUTH_SERVICE EXIT - refresh successful");
        return new AuthResponse();
    }

    public void logout(String refreshToken) {
        log.info("AUTH_SERVICE ENTRY - logout request received");
        // реализация
        log.info("AUTH_SERVICE EXIT - logout successful");
    }
}
// [END_AUTH_SERVICE]
```

### Backend Component 3 (ANCHOR: USER_REPOSITORY)
```
[START_USER_REPOSITORY]
/*
 * ANCHOR: USER_REPOSITORY
 * PURPOSE: Доступ к сущности User в PostgreSQL
 *
 * @PreConditions:
 * - Spring Data JPA контекст инициализирован
 * - DataSource доступен
 *
 * @PostConditions:
 * - при успехе: возвращается User или Optional<User>
 * - при ошибке: выбрасывается DataAccessException
 *
 * @Invariants:
 * - все запросы к БД проходят через этот репозиторий
 * - username уникален
 *
 * @SideEffects:
 * - чтение/запись в таблицу users
 *
 * @ForbiddenChanges:
 * - нельзя убрать уникальность username
 * - нельзя убрать методы findByUsername и save
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные методы поиска
 * - можно изменить стратегию кэширования
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
// [END_USER_REPOSITORY]
```

### Backend Component 4 (ANCHOR: USER_MODEL)
```
[START_USER_MODEL]
/*
 * ANCHOR: USER_MODEL
 * PURPOSE: Сущность пользователя для JPA
 *
 * @PreConditions:
 * - Hibernate контекст инициализирован
 * - таблица users существует
 *
 * @PostConditions:
 * - при сохранении: создаётся запись в БД
 * - при загрузке: возвращается сущность User
 *
 * @Invariants:
 * - username уникален
 * - password хранится в зашифрованном виде
 * - enabled может быть true/false
 *
 * @SideEffects:
 * - создание/обновление записи в таблице users
 *
 * @ForbiddenChanges:
 * - нельзя убрать уникальность username
 * - нельзя убрать хеширование пароля
 * - нельзя изменить структуру таблицы без миграции
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля
 * - можно изменить стратегию кэширования
 */
@Entity
@Table(name = "users", schema = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // getters and setters
}
// [END_USER_MODEL]
```

### Backend Component 5 (ANCHOR: AUTH_REQUEST)
```
[START_AUTH_REQUEST]
/*
 * ANCHOR: AUTH_REQUEST
 * PURPOSE: DTO для запроса входа в систему
 *
 * @PreConditions:
 * - JSON десериализация успешна
 * - поля не null
 *
 * @PostConditions:
 * - при успехе: возвращается AuthRequest с валидными данными
 * - при ошибке: выбрасывается ValidationException
 *
 * @Invariants:
 * - username непустая строка
 * - password непустая строка
 *
 * @SideEffects:
 * - None
 *
 * @ForbiddenChanges:
 * - нельзя убрать валидацию полей
 * - нельзя изменить структуру без обновления контроллера
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля валидации
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
// [END_AUTH_REQUEST]
```

### Backend Component 6 (ANCHOR: REGISTER_REQUEST)
```
[START_REGISTER_REQUEST]
/*
 * ANCHOR: REGISTER_REQUEST
 * PURPOSE: DTO для запроса регистрации пользователя
 *
 * @PreConditions:
 * - JSON десериализация успешна
 * - поля не null
 *
 * @PostConditions:
 * - при успехе: возвращается RegisterRequest с валидными данными
 * - при ошибке: выбрасывается ValidationException
 *
 * @Invariants:
 * - username: 3-50 символов, только латиница, без пробелов
 * - password: min 8 символов, 1 цифра, 1 заглавная буква, 1 специальный символ
 *
 * @SideEffects:
 * - None
 *
 * @ForbiddenChanges:
 * - нельзя убрать валидацию полей
 * - нельзя изменить требования к паролю без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (email, имя и т.д.)
 * - можно изменить стратегию валидации
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]*$", message = "Username must start with a letter and contain only letters and numbers")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]*$", message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character")
    private String password;
}
// [END_REGISTER_REQUEST]
```

### Backend Component 7 (ANCHOR: AUTH_RESPONSE)
```
[START_AUTH_RESPONSE]
/*
 * ANCHOR: AUTH_RESPONSE
 * PURPOSE: DTO для ответа аутентификации
 *
 * @PreConditions:
 * - JWT токены сгенерированы
 *
 * @PostConditions:
 * - возвращается AuthResponse с access_token и refresh_token
 *
 * @Invariants:
 * - access_token не null
 * - refresh_token не null
 *
 * @SideEffects:
 * - None
 *
 * @ForbiddenChanges:
 * - нельзя убрать поля token и refreshToken
 * - нельзя возвращать пароль в ответе
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (userId, username и т.д.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserDto user;
}
// [END_AUTH_RESPONSE]
```

### Backend Component 8 (ANCHOR: USER_DTO)
```
[START_USER_DTO]
/*
 * ANCHOR: USER_DTO
 * PURPOSE: DTO для передачи данных пользователя
 *
 * @PreConditions:
 * - User сущность загружена из БД
 *
 * @PostConditions:
 * - возвращается UserDto с данными пользователя
 *
 * @Invariants:
 * - id не null
 * - username не null
 *
 * @SideEffects:
 * - None
 *
 * @ForbiddenChanges:
 * - нельзя убрать обязательные поля
 * - нельзя возвращать пароль в DTO
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (email, role и т.д.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String role;
    private Boolean enabled;
    private LocalDateTime createdAt;
}
// [END_USER_DTO]
```

### Backend Component 9 (ANCHOR: JWT_TOKEN_PROVIDER)
```
[START_JWT_TOKEN_PROVIDER]
/*
 * ANCHOR: JWT_TOKEN_PROVIDER
 * PURPOSE: Генерация и валидация JWT токенов
 *
 * @PreConditions:
 * - Secret key настроен
 * - Spring контекст инициализирован
 *
 * @PostConditions:
 * - при успехе: возвращается JWT токен
 * - при ошибке: выбрасывается JwtException
 *
 * @Invariants:
 * - access token имеет ограниченное время жизни (15 минут)
 * - refresh token имеет более длительное время жизни (7 дней)
 * - токены подписываются секретным ключом
 *
 * @SideEffects:
 * - None
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку подписи токена
 * - нельзя изменить алгоритм хеширования без миграции
 *
 * @AllowedRefactorZone:
 * - можно изменить время жизни токенов
 * - можно изменить алгоритм шифрования
 */
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public String generateAccessToken(UserDetails userDetails) {
        log.info("JWT_TOKEN_PROVIDER ENTRY - generateAccessToken");
        // реализация
        log.info("JWT_TOKEN_PROVIDER EXIT - access token generated");
        return "token";
    }

    public String generateRefreshToken(UserDetails userDetails) {
        log.info("JWT_TOKEN_PROVIDER ENTRY - generateRefreshToken");
        // реализация
        log.info("JWT_TOKEN_PROVIDER EXIT - refresh token generated");
        return "token";
    }

    public String getUsernameFromToken(String token) {
        log.info("JWT_TOKEN_PROVIDER ENTRY - getUsernameFromToken");
        // реализация
        log.info("JWT_TOKEN_PROVIDER EXIT - username extracted");
        return "username";
    }

    public boolean validateToken(String token) {
        log.info("JWT_TOKEN_PROVIDER ENTRY - validateToken");
        // реализация
        log.info("JWT_TOKEN_PROVIDER EXIT - token validated");
        return true;
    }
}
// [END_JWT_TOKEN_PROVIDER]
```

### Backend Component 10 (ANCHOR: JWT_AUTH_FILTER)
```
[START_JWT_AUTH_FILTER]
/*
 * ANCHOR: JWT_AUTH_FILTER
 * PURPOSE: Фильтр для проверки JWT токена в запросах
 *
 * @PreConditions:
 * - Spring Security контекст инициализирован
 * - JwtTokenProvider доступен
 *
 * @PostConditions:
 * - при валидном токене: устанавливается Authentication в SecurityContext
 * - при невалидном токене: возвращается 401 Unauthorized
 *
 * @Invariants:
 * - фильтр применяется ко всем запросам, кроме публичных
 * - токен проверяется до обработки запроса
 *
 * @SideEffects:
 * - устанавливает Authentication в SecurityContext
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку токена
 * - нельзя пропускать публичные endpoints
 *
 * @AllowedRefactorZone:
 * - можно изменить логику проверки токена
 * - можно добавить кэширование
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        log.info("JWT_AUTH_FILTER ENTRY - processing request");
        // реализация
        log.info("JWT_AUTH_FILTER EXIT - request processed");
        filterChain.doFilter(request, response);
    }
}
// [END_JWT_AUTH_FILTER]
```

### Backend Component 11 (ANCHOR: SECURITY_CONFIG)
```
[START_SECURITY_CONFIG]
/*
 * ANCHOR: SECURITY_CONFIG
 * PURPOSE: Конфигурация Spring Security для JWT аутентификации
 *
 * @PreConditions:
 * - Spring Boot контекст инициализирован
 * - JwtAuthenticationFilter и CustomUserDetailsService доступны
 *
 * @PostConditions:
 * - SecurityFilterChain настроен с JWT фильтром
 * - HTTP Basic отключён
 * - CORS включён
 *
 * @Invariants:
 * - все запросы проходят через фильтр аутентификации
 * - публичные endpoints доступны без токена
 *
 * @SideEffects:
 * - настраивает SecurityFilterChain
 *
 * @ForbiddenChanges:
 * - нельзя убрать JWT фильтр
 * - нельзя включить HTTP Basic
 *
 * @AllowedRefactorZone:
 * - можно изменить настройки CORS
 * - можно добавить дополнительные фильтры
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("SECURITY_CONFIG ENTRY - configuring security");
        // реализация
        log.info("SECURITY_CONFIG EXIT - security configured");
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        log.info("SECURITY_CONFIG ENTRY - creating authentication manager");
        // реализация
        log.info("SECURITY_CONFIG EXIT - authentication manager created");
        return http.getSharedObject(AuthenticationManager.class);
    }
}
// [END_SECURITY_CONFIG]
```

### Backend Component 12 (ANCHOR: USER_DETAILS_SERVICE)
```
[START_USER_DETAILS_SERVICE]
/*
 * ANCHOR: USER_DETAILS_SERVICE
 * PURPOSE: Загрузка данных пользователя для Spring Security
 *
 * @PreConditions:
 * - UserRepository доступен
 * - Spring Security контекст инициализирован
 *
 * @PostConditions:
 * - при успехе: возвращается UserDetails
 * - при ошибке: выбрасывается UsernameNotFoundException
 *
 * @Invariants:
 * - пользователь загружается по username
 * - пароль не раскрывается за пределы SecurityContext
 *
 * @SideEffects:
 * - None
 *
 * @ForbiddenChanges:
 * - нельзя убрать загрузку пользователя по username
 * - нельзя возвращать null вместо UserDetails
 *
 * @AllowedRefactorZone:
 * - можно добавить кэширование
 * - можно изменить стратегию загрузки ролей
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("USER_DETAILS_SERVICE ENTRY - loading user by username");
        // реализация
        log.info("USER_DETAILS_SERVICE EXIT - user loaded");
        return new UserPrincipal();
    }
}
// [END_USER_DETAILS_SERVICE]
```

### Frontend Component 1 (ANCHOR: LOGIN_COMPONENT)
```
[START_LOGIN_COMPONENT]
/*
 * ANCHOR: LOGIN_COMPONENT
 * PURPOSE: Компонент формы входа в систему
 *
 * @PreConditions:
 * - Angular контекст инициализирован
 * - AuthService доступен через DI
 *
 * @PostConditions:
 * - при успехе: пользователь авторизован, перенаправление на dashboard
 * - при ошибке: отображается сообщение об ошибке
 *
 * @Invariants:
 * - форма валидируется на клиенте
 * - токены сохраняются в localStorage
 * - пароль не логируется
 *
 * @SideEffects:
 * - вызывает AuthService.login()
 * - сохраняет токены в localStorage
 *
 * @ForbiddenChanges:
 * - нельзя убрать валидацию формы
 * - нельзя удалять токены при ошибке без перенаправления
 *
 * @AllowedRefactorZone:
 * - можно изменить стили компонента
 * - можно добавить дополнительные поля формы
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  errorMessage: string | null = null;
  isLoading = false;

  ngOnInit(): void {
    logLine('auth', 'DEBUG', 'ngOnInit', 'LOGIN_COMPONENT', 'ENTRY', {});
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
    logLine('auth', 'DEBUG', 'ngOnInit', 'LOGIN_COMPONENT', 'EXIT', { formInitialized: true });
  }

  onSubmit(): void {
    logLine('auth', 'DEBUG', 'onSubmit', 'LOGIN_COMPONENT', 'ENTRY', {});
    if (this.loginForm.invalid) {
      logLine('auth', 'WARN', 'onSubmit', 'LOGIN_COMPONENT', 'DECISION', { decision: 'form_invalid' });
      return;
    }
    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        logLine('auth', 'INFO', 'onSubmit', 'LOGIN_COMPONENT', 'STATE_CHANGE', { action: 'login_success' });
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        logLine('auth', 'ERROR', 'onSubmit', 'LOGIN_COMPONENT', 'ERROR', { reason: error.message });
        this.errorMessage = error.message;
      }
    });
    logLine('auth', 'DEBUG', 'onSubmit', 'LOGIN_COMPONENT', 'EXIT', {});
  }
}
// [END_LOGIN_COMPONENT]
```

### Frontend Component 2 (ANCHOR: REGISTER_COMPONENT)
```
[START_REGISTER_COMPONENT]
/*
 * ANCHOR: REGISTER_COMPONENT
 * PURPOSE: Компонент формы регистрации пользователя
 *
 * @PreConditions:
 * - Angular контекст инициализирован
 * - AuthService доступен через DI
 *
 * @PostConditions:
 * - при успехе: пользователь зарегистрирован, перенаправление на login
 * - при ошибке: отображается сообщение об ошибке
 *
 * @Invariants:
 * - форма валидируется на клиенте
 * - токены не сохраняются при регистрации
 * - пароль не логируется
 *
 * @SideEffects:
 * - вызывает AuthService.register()
 * - перенаправление на страницу входа при успехе
 *
 * @ForbiddenChanges:
 * - нельзя убрать валидацию формы
 * - нельзя удалять токены при ошибке без перенаправления
 *
 * @AllowedRefactorZone:
 * - можно изменить стили компонента
 * - можно добавить дополнительные поля формы
 */
@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  errorMessage: string | null = null;
  isLoading = false;

  ngOnInit(): void {
    logLine('auth', 'DEBUG', 'ngOnInit', 'REGISTER_COMPONENT', 'ENTRY', {});
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
    logLine('auth', 'DEBUG', 'ngOnInit', 'REGISTER_COMPONENT', 'EXIT', { formInitialized: true });
  }

  onSubmit(): void {
    logLine('auth', 'DEBUG', 'onSubmit', 'REGISTER_COMPONENT', 'ENTRY', {});
    if (this.registerForm.invalid) {
      logLine('auth', 'WARN', 'onSubmit', 'REGISTER_COMPONENT', 'DECISION', { decision: 'form_invalid' });
      return;
    }
    this.authService.register(this.registerForm.value).subscribe({
      next: (response) => {
        logLine('auth', 'INFO', 'onSubmit', 'REGISTER_COMPONENT', 'STATE_CHANGE', { action: 'register_success' });
        this.router.navigate(['/login']);
      },
      error: (error) => {
        logLine('auth', 'ERROR', 'onSubmit', 'REGISTER_COMPONENT', 'ERROR', { reason: error.message });
        this.errorMessage = error.message;
      }
    });
    logLine('auth', 'DEBUG', 'onSubmit', 'REGISTER_COMPONENT', 'EXIT', {});
  }
}
// [END_REGISTER_COMPONENT]
```

### Frontend Component 3 (ANCHOR: AUTH_SERVICE_FRONTEND)
```
[START_AUTH_SERVICE_FRONTEND]
/*
 * ANCHOR: AUTH_SERVICE_FRONTEND
 * PURPOSE: Управление аутентификацией на фронтенде
 *
 * @PreConditions:
 * - HTTP клиент инициализирован
 * - AuthInterceptor настроен
 *
 * @PostConditions:
 * - при успехе: токены сохранены в localStorage
 * - при ошибке: отображается сообщение об ошибке
 *
 * @Invariants:
 * - токены никогда не логируются в открытом виде
 * - refresh token обновляется автоматически при 401
 *
 * @SideEffects:
 * - сохраняет токены в localStorage
 * - устанавливает заголовок Authorization для последующих запросов
 *
 * @ForbiddenChanges:
 * - нельзя удалять токены при ошибке без перенаправления на login
 * - нельзя хранить токены в небезопасных местах
 *
 * @AllowedRefactorZone:
 * - можно изменить способ хранения токенов (localStorage → sessionStorage)
 * - можно добавить кэширование токенов
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly STORAGE_KEY = 'auth_tokens';
  private readonly currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);

  constructor(private http: HttpClient) {
    logLine('auth', 'DEBUG', 'constructor', 'AUTH_SERVICE_FRONTEND', 'ENTRY', {});
    const stored = localStorage.getItem(this.STORAGE_KEY);
    if (stored) {
      this.currentUserSubject.next(JSON.parse(stored));
    }
    logLine('auth', 'DEBUG', 'constructor', 'AUTH_SERVICE_FRONTEND', 'EXIT', { initialized: true });
  }

  register(username: string, password: string): Observable<AuthResponse> {
    logLine('auth', 'DEBUG', 'register', 'AUTH_SERVICE_FRONTEND', 'ENTRY', { username });
    return this.http.post<AuthResponse>('/api/v1/auth/register', { username, password }).pipe(
      tap(response => {
        logLine('auth', 'INFO', 'register', 'AUTH_SERVICE_FRONTEND', 'STATE_CHANGE', { action: 'register_success' });
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(response));
        this.currentUserSubject.next(response);
      })
    );
  }

  login(username: string, password: string): Observable<AuthResponse> {
    logLine('auth', 'DEBUG', 'login', 'AUTH_SERVICE_FRONTEND', 'ENTRY', { username });
    return this.http.post<AuthResponse>('/api/v1/auth/login', { username, password }).pipe(
      tap(response => {
        logLine('auth', 'INFO', 'login', 'AUTH_SERVICE_FRONTEND', 'STATE_CHANGE', { action: 'login_success' });
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(response));
        this.currentUserSubject.next(response);
      })
    );
  }

  refresh(): Observable<AuthResponse> {
    logLine('auth', 'DEBUG', 'refresh', 'AUTH_SERVICE_FRONTEND', 'ENTRY', {});
    return this.http.post<AuthResponse>('/api/v1/auth/refresh', { refreshToken: this.getToken() });
  }

  logout(): Observable<void> {
    logLine('auth', 'DEBUG', 'logout', 'AUTH_SERVICE_FRONTEND', 'ENTRY', {});
    return this.http.post<void>('/api/v1/auth/logout', { refreshToken: this.getToken() }).pipe(
      tap(() => {
        logLine('auth', 'INFO', 'logout', 'AUTH_SERVICE_FRONTEND', 'STATE_CHANGE', { action: 'logout_success' });
        localStorage.removeItem(this.STORAGE_KEY);
        this.currentUserSubject.next(null);
      })
    );
  }

  isAuthenticated(): boolean {
    logLine('auth', 'DEBUG', 'isAuthenticated', 'AUTH_SERVICE_FRONTEND', 'ENTRY', {});
    const token = this.getToken();
    const result = token !== null;
    logLine('auth', 'DEBUG', 'isAuthenticated', 'AUTH_SERVICE_FRONTEND', 'EXIT', { result });
    return result;
  }

  getToken(): string | null {
    logLine('auth', 'DEBUG', 'getToken', 'AUTH_SERVICE_FRONTEND', 'ENTRY', {});
    const stored = localStorage.getItem(this.STORAGE_KEY);
    const token = stored ? JSON.parse(stored).accessToken : null;
    logLine('auth', 'DEBUG', 'getToken', 'AUTH_SERVICE_FRONTEND', 'EXIT', { hasToken: token !== null });
    return token;
  }
}
// [END_AUTH_SERVICE_FRONTEND]
```

### Frontend Component 4 (ANCHOR: AUTH_GUARD)
```
[START_AUTH_GUARD]
/*
 * ANCHOR: AUTH_GUARD
 * PURPOSE: Guard для защиты роутов от неавторизованных пользователей
 *
 * @PreConditions:
 * - Angular Router и AuthService доступны
 *
 * @PostConditions:
 * - при авторизации: возвращается true, роут активируется
 * - при неавторизации: возвращается false, перенаправление на login
 *
 * @Invariants:
 * - проверяет авторизацию перед активацией роута
 * - сохраняет URL для последующего перенаправления
 *
 * @SideEffects:
 * - None
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку авторизации
 * - нельзя пропускать публичные роуты
 *
 * @AllowedRefactorZone:
 * - можно изменить логику проверки ролей
 * - можно добавить кэширование
 */
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private router: Router, private authService: AuthService) {
    logLine('auth', 'DEBUG', 'constructor', 'AUTH_GUARD', 'ENTRY', {});
    logLine('auth', 'DEBUG', 'constructor', 'AUTH_GUARD', 'EXIT', { initialized: true });
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    logLine('auth', 'DEBUG', 'canActivate', 'AUTH_GUARD', 'ENTRY', { url: state.url });
    if (this.authService.isAuthenticated()) {
      logLine('auth', 'DEBUG', 'canActivate', 'AUTH_GUARD', 'EXIT', { result: true });
      return true;
    }
    logLine('auth', 'WARN', 'canActivate', 'AUTH_GUARD', 'DECISION', { decision: 'redirect_to_login' });
    this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    logLine('auth', 'DEBUG', 'canActivate', 'AUTH_GUARD', 'EXIT', { result: false });
    return false;
  }
}
// [END_AUTH_GUARD]
```

### Frontend Component 5 (ANCHOR: AUTH_INTERCEPTOR)
```
[START_AUTH_INTERCEPTOR]
/*
 * ANCHOR: AUTH_INTERCEPTOR
 * PURPOSE: Interceptor для добавления JWT токена к HTTP запросам
 *
 * @PreConditions:
 * - Angular HttpClient и AuthService доступны
 *
 * @PostConditions:
 * - при успехе: токен добавлен к заголовку Authorization
 * - при 401: запускается обновление токена
 *
 * @Invariants:
 * - токен добавляется ко всем запросам (кроме публичных)
 * - токен не логируется в открытом виде
 *
 * @SideEffects:
 * - добавляет заголовок Authorization
 * - обрабатывает 401 ошибки
 *
 * @ForbiddenChanges:
 * - нельзя убрать добавление токена
 * - нельзя логировать токен
 *
 * @AllowedRefactorZone:
 * - можно изменить логику обновления токена
 * - можно добавить кэширование
 */
@Injectable({ providedIn: 'root' })
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {
    logLine('auth', 'DEBUG', 'constructor', 'AUTH_INTERCEPTOR', 'ENTRY', {});
    logLine('auth', 'DEBUG', 'constructor', 'AUTH_INTERCEPTOR', 'EXIT', { initialized: true });
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    logLine('auth', 'DEBUG', 'intercept', 'AUTH_INTERCEPTOR', 'ENTRY', { url: request.url });
    const token = this.authService.getToken();
    if (token) {
      request = request.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
      logLine('auth', 'DEBUG', 'intercept', 'AUTH_INTERCEPTOR', 'BRANCH', { branch: 'token_added' });
    }
    return next.handle(request).pipe(
      catchError(error => {
        if (error.status === 401) {
          logLine('auth', 'WARN', 'intercept', 'AUTH_INTERCEPTOR', 'DECISION', { decision: 'token_expired' });
          return this.authService.refresh();
        }
        return throwError(() => error);
      })
    );
  }
}
// [END_AUTH_INTERCEPTOR]
```

### Frontend Component 6 (ANCHOR: USER_MODEL_FRONTEND)
```
[START_USER_MODEL_FRONTEND]
/*
 * ANCHOR: USER_MODEL_FRONTEND
 * PURPOSE: TypeScript интерфейс для модели пользователя
 *
 * @PreConditions:
 * - TypeScript компиляция успешна
 *
 * @PostConditions:
 * - возвращается User модель с валидными данными
 *
 * @Invariants:
 * - id: number
 * - username: string
 * - role: 'USER' | 'ADMIN'
 * - enabled: boolean
 *
 * @SideEffects:
 * - None
 *
 * @ForbiddenChanges:
 * - нельзя убрать обязательные поля
 * - нельзя изменить типы полей
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля
 */
export interface User {
  id: number;
  username: string;
  role: 'USER' | 'ADMIN';
  enabled: boolean;
  createdAt: string;
}
// [END_USER_MODEL_FRONTEND]
```

## API

## API

### Endpoints
- POST /api/v1/auth/register — Регистрация пользователя
- POST /api/v1/auth/login — Вход в систему
- POST /api/v1/auth/refresh — Обновление access токена
- POST /api/v1/auth/logout — Выход из системы

### DTO
- RegisterRequest — Запрос регистрации (username, password)
- AuthRequest — Запрос входа (username, password)
- AuthResponse — Ответ аутентификации (token, refreshToken)
- UserDto — Данные пользователя (id, username, role, enabled, createdAt)

## UI

### Components
- LoginComponent — Форма входа с валидацией
- RegisterComponent — Форма регистрации с валидацией

### Services
- AuthService — HTTP запросы к API аутентификации

## Test Plan

### Backend Tests (JUnit 5 + Spring Test)

#### Level 1: Детерминированные тесты
- [ ] Тест 1: Проверка успешной регистрации
- [ ] Тест 2: Проверка валидации username (менее 3 символов)
- [ ] Тест 3: Проверка валидации password (менее 8 символов)
- [ ] Тест 4: Проверка дубликата username
- [ ] Тест 5: Проверка успешного входа
- [ ] Тест 6: Проверка неверного пароля
- [ ] Тест 7: Проверка генерации JWT токена
- [ ] Тест 8: Проверка refresh токена

#### Level 2: Тесты траектории
- [ ] Тест 1: Проверка ENTRY/EXIT логов в register()
- [ ] Тест 2: Проверка BRANCH логов при валидации
- [ ] Тест 3: Проверка DECISION логов при дубликате
- [ ] Тест 4: Проверка STATE_CHANGE логов при создании пользователя
- [ ] Тест 5: Проверка ERROR логов при исключениях

#### Level 3: Интеграционные тесты
- [ ] Тест 1: E2E сценарий регистрации и входа
- [ ] Тест 2: Интеграция с PostgreSQL (создание пользователя)
- [ ] Тест 3: Тестирование REST API (MockMvc)
- [ ] Тест 4: Тестирование Spring Security

### Frontend Tests (Jasmine/Karma)

#### Level 1: Детерминированные тесты
- [ ] Тест 1: Проверка отображения формы входа
- [ ] Тест 2: Проверка валидации формы входа
- [ ] Тест 3: Проверка успешного входа
- [ ] Тест 4: Проверка обработки ошибок входа
- [ ] Тест 5: Проверка отображения формы регистрации
- [ ] Тест 6: Проверка валидации формы регистрации
- [ ] Тест 7: Проверка успешной регистрации
- [ ] Тест 8: Проверка обработки ошибок регистрации

#### Level 2: Тесты траектории
- [ ] Тест 1: Проверка ENTRY/EXIT логов в LoginComponent
- [ ] Тест 2: Проверка BRANCH логов при валидации
- [ ] Тест 3: Проверка DECISION логов при ошибке

#### Level 3: Интеграционные тесты
- [ ] Тест 1: E2E сценарий регистрации и входа (Cypress)
- [ ] Тест 2: Интеграция с backend API

## Dependencies
- FEAT-001 не имеет зависимостей от других фич (базовая фича)
- Зависит от: PostgreSQL (таблица users)
- Зависит от: Spring Security (JWT фильтры)
- Зависит от: Spring Data JPA (UserRepository)

## Acceptance Criteria
- [ ] AC-001: Пользователь может зарегистрироваться с валидными данными
- [ ] AC-002: Пользователь не может зарегистрироваться с невалидными данными (короткий username, слабый password)
- [ ] AC-003: Пользователь не может зарегистрироваться с уже существующим username
- [ ] AC-004: Пользователь может войти в систему с валидными данными
- [ ] AC-005: Пользователь не может войти с неверным паролем
- [ ] AC-006: Access token выдаётся с ограниченным временем жизни
- [ ] AC-007: Refresh token выдаётся с более длительным временем жизни
- [ ] AC-008: Пользователь может обновить access token с помощью refresh token
- [ ] AC-009: Пользователь может выйти из системы (refresh token отзывается)
- [ ] AC-010: Все защищённые endpoints требуют валидный JWT
- [ ] AC-011: Время ответа API для аутентификации ≤ 300 мс
- [ ] AC-012: Пароли хешируются с помощью BCrypt
