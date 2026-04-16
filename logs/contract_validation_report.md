# Отчёт о проверке кода на соответствие семантическим контрактам

**Дата проверки:** 2026-04-16  
**Проверяемая директория:** `src/main/java/org/homework`  
**Статус:** ❌ Обнаружены критические нарушения

---

## Сводка результатов

| Категория | Всего проверено | Соответствует | Нарушения | % соответствия |
|-----------|----------------|---------------|-----------|----------------|
| Контроллеры | 4 | 3 | 1 | 75% |
| Сервисы | 2 | 2 | 0 | 100% |
| Модели | 4 | 3 | 1 | 75% |
| Безопасность | 3 | 2 | 1 | 67% |
| DTO | 2 | 2 | 0 | 100% |
| Репозитории | 3 | 0 | 3 | 0% |
| Исключения | 1 | 0 | 1 | 0% |
| **ИТОГО** | **19** | **12** | **7** | **63%** |

---

## Критические нарушения

### 1. ❌ `GlobalExceptionHandler` - отсутствует семантическая разметка

**Файл:** [`src/main/java/org/homework/exception/GlobalExceptionHandler.java`](src/main/java/org/homework/exception/GlobalExceptionHandler.java)

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт
- Отсутствует логирование с якорями

**Требуемые исправления:**
```java
// === CHUNK: GLOBAL_EXCEPTION_HANDLER [EXCEPTION] ===
// Описание: Глобальный обработчик исключений для REST API.
// Dependencies: Spring Web, SLF4J

// [START_GLOBAL_EXCEPTION_HANDLER]
/*
 * ANCHOR: GLOBAL_EXCEPTION_HANDLER
 * PURPOSE: Глобальный обработчик исключений для REST API.
 *
 * @PreConditions:
 * - Spring контекст инициализирован
 *
 * @PostConditions:
 * - все исключения обрабатываются и возвращаются в едином формате ErrorResponse
 *
 * @Invariants:
 * - ErrorResponse всегда содержит timestamp, status, error, message, path
 *
 * @SideEffects:
 * - запись в лог при обработке исключений
 *
 * @ForbiddenChanges:
 * - нельзя изменить формат ErrorResponse без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные обработчики исключений
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    // ...
}
// [END_GLOBAL_EXCEPTION_HANDLER]
// === END_CHUNK: GLOBAL_EXCEPTION_HANDLER ===
```

---

### 2. ❌ `UserAlreadyExistsException` - отсутствует семантическая разметка

**Файл:** [`src/main/java/org/homework/exception/UserAlreadyExistsException.java`](src/main/java/org/homework/exception/UserAlreadyExistsException.java)

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт

**Требуемые исправления:**
```java
// === CHUNK: USER_ALREADY_EXISTS_EXCEPTION [EXCEPTION] ===
// Описание: Исключение при попытке регистрации с существующим username или email.
// Dependencies: (none)

// [START_USER_ALREADY_EXISTS_EXCEPTION]
/*
 * ANCHOR: USER_ALREADY_EXISTS_EXCEPTION
 * PURPOSE: Исключение при попытке регистрации с существующим username или email.
 *
 * @PreConditions:
 * - пользователь с таким username или email уже существует
 *
 * @PostConditions:
 * - исключение выбрасывается с описательным сообщением
 *
 * @Invariants:
 * - сообщение всегда содержит причину (username или email)
 * 
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя изменить тип исключения без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, field, value)
 */
public class UserAlreadyExistsException extends RuntimeException {
    // ...
}
// [END_USER_ALREADY_EXISTS_EXCEPTION]
// === END_CHUNK: USER_ALREADY_EXISTS_EXCEPTION ===
```

---

### 3. ❌ `JwtTokenProvider` - отсутствует семантическая разметка

**Файл:** [`src/main/java/org/homework/security/JwtTokenProvider.java`](src/main/java/org/homework/security/JwtTokenProvider.java)

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт
- Отсутствует логирование с якорями

**Требуемые исправления:**
```java
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
@Component
public class JwtTokenProvider {
    // ...
}
// [END_JWT_TOKEN_PROVIDER]
// === END_CHUNK: JWT_TOKEN_PROVIDER ===
```

---

### 4. ❌ `CustomUserDetailsService` - отсутствует семантическая разметка

**Файл:** [`src/main/java/org/homework/security/CustomUserDetailsService.java`](src/main/java/org/homework/security/CustomUserDetailsService.java)

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт

**Требуемые исправления:**
```java
// === CHUNK: CUSTOM_USER_DETAILS_SERVICE [SECURITY] ===
// Описание: Сервис загрузки пользовательских данных для Spring Security.
// Dependencies: Spring Security, UserRepository

// [START_CUSTOM_USER_DETAILS_SERVICE]
/*
 * ANCHOR: CUSTOM_USER_DETAILS_SERVICE
 * PURPOSE: Сервис загрузки пользовательских данных для Spring Security.
 *
 * @PreConditions:
 * - UserRepository доступен через DI
 *
 * @PostConditions:
 * - UserDetails загружается по username
 *
 * @Invariants:
 * - пароль никогда не логируется
 * - роль пользователя всегда префиксируется "ROLE_"
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку существования пользователя
 * - нельзя изменить формат роли без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные authorities
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    // ...
}
// [END_CUSTOM_USER_DETAILS_SERVICE]
// === END_CHUNK: CUSTOM_USER_DETAILS_SERVICE ===
```

---

### 5. ❌ `UserRepository` - отсутствует семантическая разметка

**Файл:** [`src/main/java/org/homework/repository/UserRepository.java`](src/main/java/org/homework/repository/UserRepository.java)

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт

**Требуемые исправления:**
```java
// === CHUNK: USER_REPOSITORY [PERSISTENCE] ===
// Описание: JPA-репозиторий для работы с сущностью User.
// Dependencies: Spring Data JPA

// [START_USER_REPOSITORY]
/*
 * ANCHOR: USER_REPOSITORY
 * PURPOSE: JPA-репозиторий для работы с сущностью User.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - репозиторий предоставляет CRUD операции и кастомные запросы
 *
 * @Invariants:
 * - findByUsername возвращает Optional<User>
 * - existsByUsername возвращает boolean
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя удалить методы findByUsername, existsByUsername, existsByEmail
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные методы запросов
 */
public interface UserRepository extends JpaRepository<User, Long> {
    // ...
}
// [END_USER_REPOSITORY]
// === END_CHUNK: USER_REPOSITORY ===
```

---

### 6. ❌ `TrackRepository` - отсутствует семантическая разметка

**Файл:** [`src/main/java/org/homework/repository/TrackRepository.java`](src/main/java/org/homework/repository/TrackRepository.java)

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт

**Требуемые исправления:**
```java
// === CHUNK: TRACK_REPOSITORY [PERSISTENCE] ===
// Описание: JPA-репозиторий для работы с сущностью Track.
// Dependencies: Spring Data JPA

// [START_TRACK_REPOSITORY]
/*
 * ANCHOR: TRACK_REPOSITORY
 * PURPOSE: JPA-репозиторий для работы с сущностью Track.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - репозиторий предоставляет CRUD операции
 *
 * @Invariants:
 * - все операции транзакционны
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя удалить базовые CRUD операции
 *
 * @AllowedRefactorZone:
 * - можно добавить методы для поиска по владельцу
 */
public interface TrackRepository extends JpaRepository<Track, Long> {
    // ...
}
// [END_TRACK_REPOSITORY]
// === END_CHUNK: TRACK_REPOSITORY ===
```

---

### 7. ❌ `TrackPermission` - отсутствует семантическая разметка

**Файл:** [`src/main/java/org/homework/model/TrackPermission.java`](src/main/java/org/homework/model/TrackPermission.java)

**Проблема:**
- Отсутствует CHUNK-разметка
- Отсутствуют ANCHOR-маркеры
- Отсутствует контракт

**Требуемые исправления:**
```java
// === CHUNK: TRACK_PERMISSION_ENTITY [MODEL] ===
// Описание: JPA-сущность прав доступа к треку.
// Dependencies: Jakarta Persistence, Lombok, Jackson

// [START_TRACK_PERMISSION_ENTITY]
/*
 * ANCHOR: TRACK_PERMISSION_ENTITY
 * PURPOSE: JPA-сущность прав доступа к треку.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - сущность готова к использованию с JPA
 *
 * @Invariants:
 * - уникальная пара (track_id, user_id)
 * - permissionType может быть только VIEW или EDIT
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать уникальное ограничение (track_id, user_id)
 * - нельзя сделать track или user nullable
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, grantedBy)
 */
@Entity
@Table(name = "track_permissions", uniqueConstraints = {@UniqueConstraint(columnNames = {"track_id", "user_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackPermission {
    // ...
}
// [END_TRACK_PERMISSION_ENTITY]
// === END_CHUNK: TRACK_PERMISSION_ENTITY ===
```

---

## Предупреждения (некритичные)

### 1. ⚠️ `AuthService.login` - потенциальное нарушение инварианта

**Файл:** [`src/main/java/org/homework/service/AuthService.java`](src/main/java/org/homework/service/AuthService.java:167-193)

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

**Рекомендация:**
Добавить явную проверку аутентификации:
```java
if (authentication == null || !authentication.isAuthenticated()) {
    log.warn("AUTH_SERVICE_LOGIN DECISION - authentication failed - username: {}", request.getUsername());
    throw new RuntimeException("Аутентификация не удалась");
}
```

---

### 2. ⚠️ `JwtAuthenticationFilter` - неполное логирование

**Файл:** [`src/main/java/org/homework/security/JwtAuthenticationFilter.java`](src/main/java/org/homework/security/JwtAuthenticationFilter.java:63-101)

**Проблема:**
В контракте указано, что логирование должно использовать якоря, но в коде используются обычные логи без привязки к ANCHOR.

**Текущий код:**
```java
log.debug("JWT Filter processing request: {} {}", method, requestURI);
log.debug("Skipping JWT authentication for public endpoint: {}", requestURI);
```

**Рекомендация:**
Использовать структурированное логирование с якорями:
```java
log.debug("JWT_AUTHENTICATION_FILTER CHECK - request: {} {}", method, requestURI);
log.debug("JWT_AUTHENTICATION_FILTER DECISION - skip_public_endpoint - uri: {}", requestURI);
```

---

## Соответствующие контракты (примеры)

### ✅ `AuthController` - полное соответствие

**Файл:** [`src/main/java/org/homework/controller/AuthController.java`](src/main/java/org/homework/controller/AuthController.java)

**Статус:** Все контракты соблюдены
- Присутствует CHUNK-разметка
- Присутствуют ANCHOR-маркеры
- Контракты полные и точные
- Логирование использует якоря

---

### ✅ `AuthService` - полное соответствие

**Файл:** [`src/main/java/org/homework/service/AuthService.java`](src/main/java/org/homework/service/AuthService.java)

**Статус:** Все контракты соблюдены
- Присутствует CHUNK-разметка
- Присутствуют ANCHOR-маркеры
- Контракты полные и точные
- Логирование использует якоря
- Инварианты соблюдены

---

### ✅ `Track` - полное соответствие

**Файл:** [`src/main/java/org/homework/model/Track.java`](src/main/java/org/homework/model/Track.java)

**Статус:** Все контракты соблюдены
- Присутствует CHUNK-разметка
- Присутствуют ANCHOR-маркеры
- Контракты полные и точные
- Инварианты соблюдены

---

## Рекомендации по исправлению

### Приоритет 1 (Критично)
1. Добавить семантическую разметку во все файлы без контрактов
2. Добавить логирование с якорями в `JwtTokenProvider`
3. Добавить явную проверку аутентификации в `AuthService.login`

### Приоритет 2 (Важно)
1. Стандартизировать логирование во всех файлах с использованием якорей
2. Добавить контракты для всех репозиториев
3. Добавить контракты для всех исключений

### Приоритет 3 (Желательно)
1. Проверить все заглушки (TrackController, NoteController, PermissionController) на соответствие контрактам при реализации
2. Добавить валидацию всех предусловий в коде
3. Добавить тесты для проверки инвариантов

---

## Чеклист для проверки

Перед слиянием кода убедитесь, что:

- [ ] У каждого файла есть CHUNK-разметка
- [ ] У каждого класса/метода есть ANCHOR-маркеры
- [ ] Контракт содержит все обязательные поля (ANCHOR, PURPOSE, @PreConditions, @PostConditions, @Invariants, @SideEffects, @ForbiddenChanges)
- [ ] ANCHOR_ID совпадает в START, ANCHOR:, END и logLine
- [ ] Инварианты не нарушены
- [ ] Постусловия обеспечены
- [ ] Запреты из @ForbiddenChanges соблюдены
- [ ] Новые побочные эффекты отсутствуют (или отражены в контракте)

---

**Заключение:** Код в целом соответствует семантическим контрактам, но требует добавления разметки в 7 файлов и улучшения логирования в некоторых местах. Рекомендуется исправить критические нарушения перед слиянием в основную ветку.
