# План исправления замечаний из отчёта о проверке контрактов

**Дата:** 2026-04-16  
**Статус:** Требуется исправление 7 критических нарушений и 2 предупреждений

---

## Обзор

Отчёт о проверке выявил **7 критических нарушений** (отсутствие семантической разметки) и **2 предупреждения** (потенциальные нарушения инвариантов и неполное логирование).

**Текущее соответствие:** 63% (12 из 19 файлов)

---

## Критические нарушения (Приоритет 1)

### 1. GlobalExceptionHandler - отсутствие семантической разметки

**Файл:** `src/main/java/org/homework/exception/GlobalExceptionHandler.java`

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт
- Отсутствует логирование с якорями

**Требуемые исправления:**
1. Добавить CHUNK-разметку: `// === CHUNK: GLOBAL_EXCEPTION_HANDLER [EXCEPTION] ===`
2. Добавить ANCHOR-маркеры: `[START_GLOBAL_EXCEPTION_HANDLER]` и `[END_GLOBAL_EXCEPTION_HANDLER]`
3. Добавить контракт с полями:
   - `ANCHOR: GLOBAL_EXCEPTION_HANDLER`
   - `PURPOSE: Глобальный обработчик исключений для REST API`
   - `@PreConditions: Spring контекст инициализирован`
   - `@PostConditions: все исключения обрабатываются и возвращаются в едином формате ErrorResponse`
   - `@Invariants: ErrorResponse всегда содержит timestamp, status, error, message, path`
   - `@SideEffects: запись в лог при обработке исключений`
   - `@ForbiddenChanges: нельзя изменить формат ErrorResponse без согласования`
   - `@AllowedRefactorZone: можно добавить дополнительные обработчики исключений`
4. Добавить логирование с якорями в методы-обработчики

---

### 2. UserAlreadyExistsException - отсутствие семантической разметки

**Файл:** `src/main/java/org/homework/exception/UserAlreadyExistsException.java`

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт

**Требуемые исправления:**
1. Добавить CHUNK-разметку: `// === CHUNK: USER_ALREADY_EXISTS_EXCEPTION [EXCEPTION] ===`
2. Добавить ANCHOR-маркеры: `[START_USER_ALREADY_EXISTS_EXCEPTION]` и `[END_USER_ALREADY_EXISTS_EXCEPTION]`
3. Добавить контракт с полями:
   - `ANCHOR: USER_ALREADY_EXISTS_EXCEPTION`
   - `PURPOSE: Исключение при попытке регистрации с существующим username или email`
   - `@PreConditions: пользователь с таким username или email уже существует`
   - `@PostConditions: исключение выбрасывается с описательным сообщением`
   - `@Invariants: сообщение всегда содержит причину (username или email)`
   - `@SideEffects: нет побочных эффектов`
   - `@ForbiddenChanges: нельзя изменить тип исключения без согласования`
   - `@AllowedRefactorZone: можно добавить дополнительные поля (например, field, value)`

---

### 3. JwtTokenProvider - отсутствие семантической разметки и логирования

**Файл:** `src/main/java/org/homework/security/JwtTokenProvider.java`

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт
- Отсутствует логирование с якорями

**Требуемые исправления:**
1. Добавить CHUNK-разметку: `// === CHUNK: JWT_TOKEN_PROVIDER [SECURITY] ===`
2. Добавить ANCHOR-маркеры: `[START_JWT_TOKEN_PROVIDER]` и `[END_JWT_TOKEN_PROVIDER]`
3. Добавить контракт с полями:
   - `ANCHOR: JWT_TOKEN_PROVIDER`
   - `PURPOSE: Провайдер JWT токенов для аутентификации`
   - `@PreConditions: jwt.secret настроен в application.properties`
   - `@PostConditions: токены генерируются и валидируются с использованием секретного ключа`
   - `@Invariants: секретный ключ никогда не логируется, токен содержит username в subject`
   - `@SideEffects: нет побочных эффектов`
   - `@ForbiddenChanges: нельзя убрать валидацию токена, нельзя убрать подпись токена`
   - `@AllowedRefactorZone: можно изменить срок действия токена, можно добавить дополнительные claims в токен`
4. Добавить логирование с якорями в методы:
   - `generateToken()` - ENTRY/EXIT логи
   - `getUsernameFromToken()` - ENTRY/EXIT логи
   - `validateToken()` - ENTRY/EXIT/ERROR логи

---

### 4. CustomUserDetailsService - отсутствие семантической разметки

**Файл:** `src/main/java/org/homework/security/CustomUserDetailsService.java`

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт

**Требуемые исправления:**
1. Добавить CHUNK-разметку: `// === CHUNK: CUSTOM_USER_DETAILS_SERVICE [SECURITY] ===`
2. Добавить ANCHOR-маркеры: `[START_CUSTOM_USER_DETAILS_SERVICE]` и `[END_CUSTOM_USER_DETAILS_SERVICE]`
3. Добавить контракт с полями:
   - `ANCHOR: CUSTOM_USER_DETAILS_SERVICE`
   - `PURPOSE: Сервис загрузки пользовательских данных для Spring Security`
   - `@PreConditions: UserRepository доступен через DI`
   - `@PostConditions: UserDetails загружается по username`
   - `@Invariants: пароль никогда не логируется, роль пользователя всегда префиксируется "ROLE_"`
   - `@SideEffects: нет побочных эффектов`
   - `@ForbiddenChanges: нельзя убрать проверку существования пользователя, нельзя изменить формат роли без согласования`
   - `@AllowedRefactorZone: можно добавить дополнительные authorities`
4. Обновить существующее логирование для использования якорей

---

### 5. UserRepository - отсутствие семантической разметки

**Файл:** `src/main/java/org/homework/repository/UserRepository.java`

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт

**Требуемые исправления:**
1. Добавить CHUNK-разметку: `// === CHUNK: USER_REPOSITORY [PERSISTENCE] ===`
2. Добавить ANCHOR-маркеры: `[START_USER_REPOSITORY]` и `[END_USER_REPOSITORY]`
3. Добавить контракт с полями:
   - `ANCHOR: USER_REPOSITORY`
   - `PURPOSE: JPA-репозиторий для работы с сущностью User`
   - `@PreConditions: нет нетривиальных предусловий`
   - `@PostConditions: репозиторий предоставляет CRUD операции и кастомные запросы`
   - `@Invariants: findByUsername возвращает Optional<User>, existsByUsername возвращает boolean`
   - `@SideEffects: нет побочных эффектов`
   - `@ForbiddenChanges: нельзя удалить методы findByUsername, existsByUsername, existsByEmail`
   - `@AllowedRefactorZone: можно добавить дополнительные методы запросов`

---

### 6. TrackRepository - отсутствие семантической разметки

**Файл:** `src/main/java/org/homework/repository/TrackRepository.java`

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт

**Требуемые исправления:**
1. Добавить CHUNK-разметку: `// === CHUNK: TRACK_REPOSITORY [PERSISTENCE] ===`
2. Добавить ANCHOR-маркеры: `[START_TRACK_REPOSITORY]` и `[END_TRACK_REPOSITORY]`
3. Добавить контракт с полями:
   - `ANCHOR: TRACK_REPOSITORY`
   - `PURPOSE: JPA-репозиторий для работы с сущностью Track`
   - `@PreConditions: нет нетривиальных предусловий`
   - `@PostConditions: репозиторий предоставляет CRUD операции`
   - `@Invariants: все операции транзакционны`
   - `@SideEffects: нет побочных эффектов`
   - `@ForbiddenChanges: нельзя удалить базовые CRUD операции`
   - `@AllowedRefactorZone: можно добавить методы для поиска по владельцу`

---

### 7. TrackPermission - отсутствие семантической разметки

**Файл:** `src/main/java/org/homework/model/TrackPermission.java`

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт

**Требуемые исправления:**
1. Добавить CHUNK-разметку: `// === CHUNK: TRACK_PERMISSION_ENTITY [MODEL] ===`
2. Добавить ANCHOR-маркеры: `[START_TRACK_PERMISSION_ENTITY]` и `[END_TRACK_PERMISSION_ENTITY]`
3. Добавить контракт с полями:
   - `ANCHOR: TRACK_PERMISSION_ENTITY`
   - `PURPOSE: JPA-сущность прав доступа к треку`
   - `@PreConditions: нет нетривиальных предусловий`
   - `@PostConditions: сущность готова к использованию с JPA`
   - `@Invariants: уникальная пара (track_id, user_id), permissionType может быть только VIEW или EDIT`
   - `@SideEffects: нет побочных эффектов`
   - `@ForbiddenChanges: нельзя убрать уникальное ограничение (track_id, user_id), нельзя сделать track или user nullable`
   - `@AllowedRefactorZone: можно добавить дополнительные поля (например, grantedBy)`

---

## Предупреждения (Приоритет 2)

### 1. AuthService.login - потенциальное нарушение инварианта

**Файл:** `src/main/java/org/homework/service/AuthService.java:167-193`

**Проблема:**
В контракте указано: "токены генерируются только для аутентифицированных пользователей", но в коде нет явной проверки, что пользователь аутентифицирован перед генерацией токенов.

**Текущий код:**
```java
public AuthResponse login(AuthRequest request) {
    log.info("AUTH_SERVICE_LOGIN ENTRY - username: {}", request.getUsername());

    // Аутентификация пользователя
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
            )
    );

    // Получение пользователя из базы данных
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

    // Генерация JWT токенов
    String token = jwtTokenProvider.generateToken(user.getUsername());
    String refreshToken = jwtTokenProvider.generateToken(user.getUsername());
    // ...
}
```

**Требуемые исправления:**
Добавить явную проверку аутентификации после вызова `authenticationManager.authenticate()`:
```java
if (authentication == null || !authentication.isAuthenticated()) {
    log.warn("AUTH_SERVICE_LOGIN DECISION - authentication failed - username: {}", request.getUsername());
    throw new RuntimeException("Аутентификация не удалась");
}
```

---

### 2. JwtAuthenticationFilter - неполное логирование

**Файл:** `src/main/java/org/homework/security/JwtAuthenticationFilter.java:63-101`

**Проблема:**
В контракте указано, что логирование должно использовать якоря, но в коде используются обычные логи без привязки к ANCHOR.

**Текущий код:**
```java
log.debug("JWT Filter processing request: {} {}", method, requestURI);
log.debug("Skipping JWT authentication for public endpoint: {}", requestURI);
```

**Требуемые исправления:**
Использовать структурированное логирование с якорями:
```java
log.debug("JWT_AUTHENTICATION_FILTER CHECK - request: {} {}", method, requestURI);
log.debug("JWT_AUTHENTICATION_FILTER DECISION - skip_public_endpoint - uri: {}", requestURI);
```

---

## Порядок выполнения

### Этап 1: Критические нарушения (Приоритет 1)
1. ✅ Исправить `GlobalExceptionHandler`
2. ✅ Исправить `UserAlreadyExistsException`
3. ✅ Исправить `JwtTokenProvider`
4. ✅ Исправить `CustomUserDetailsService`
5. ✅ Исправить `UserRepository`
6. ✅ Исправить `TrackRepository`
7. ✅ Исправить `TrackPermission`

### Этап 2: Предупреждения (Приоритет 2)
8. ✅ Исправить `AuthService.login` - добавить явную проверку аутентификации
9. ✅ Исправить `JwtAuthenticationFilter` - добавить логирование с якорями

---

## Критерии завершения

- [ ] Все 7 критических нарушений исправлены
- [ ] Все 2 предупреждения исправлены
- [ ] Все файлы имеют CHUNK-разметку
- [ ] Все файлы имеют ANCHOR-маркеры
- [ ] Все файлы имеют полные контракты
- [ ] Логирование использует якори везде, где требуется
- [ ] Инварианты соблюдены

---

## Ожидаемый результат

После выполнения всех исправлений:
- **Соответствие контрактам:** 100% (19 из 19 файлов)
- **Критические нарушения:** 0
- **Предупреждения:** 0
- **Все файлы имеют семантическую разметку**

---

## Дополнительные рекомендации

1. **Проверить заглушки:** TrackController, NoteController, PermissionController на соответствие контрактам при реализации
2. **Добавить валидацию:** всех предусловий в коде
3. **Добавить тесты:** для проверки инвариантов
4. **Обновить отчёт:** после завершения всех исправлений обновить `logs/contract_validation_report.md`
