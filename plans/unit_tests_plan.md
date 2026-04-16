# План написания юнит-тестов для TrackHub

## Обзор

Документ описывает план написания юнит-тестов для существующего Java кода проекта TrackHub.

## Цели

- Обеспечить покрытие бизнес-логики тестами
- Проверить контракты сервисов
- Гарантировать корректность работы с моками репозиториев
- Соблюдать принципы TDD (тесты до кода для новых функций)

## Технологический стек тестирования

- **JUnit 5** - фреймворк для тестирования
- **Mockito** - мокирование зависимостей
- **Spring Boot Test** - интеграционные тесты
- **AssertJ** - fluent assertions (опционально)

## Структура тестов

```
src/test/java/org/homework/
├── service/
│   ├── AuthServiceTest.java
│   ├── TrackServiceTest.java
│   ├── PermissionServiceTest.java
│   └── JwtTokenProviderTest.java
└── security/
    └── JwtTokenProviderTest.java
```

---

## 1. AuthServiceTest

### Тестируемые методы

#### 1.1 `register(RegisterRequest request)`

**Контракт:**
- При успехе: возвращается UserDto с id нового пользователя
- При дубликате username: выбрасывается UserAlreadyExistsException
- При дубликате email: выбрасывается UserAlreadyExistsException
- Пароль всегда хешируется перед сохранением
- Новый пользователь всегда имеет роль USER

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `register_Success` | Регистрация нового пользователя с уникальными данными | UserDto с id, username, email, role=USER |
| 2 | `register_DuplicateUsername` | Регистрация с существующим username | UserAlreadyExistsException |
| 3 | `register_DuplicateEmail` | Регистрация с существующим email | UserAlreadyExistsException |
| 4 | `register_PasswordHashed` | Проверка хеширования пароля | Пароль в БД хеширован |
| 5 | `register_DefaultRole` | Проверка роли по умолчанию | Роль = USER |
| 6 | `register_DatabaseError` | Ошибка при сохранении в БД | RuntimeException |

**Моки:**
- `UserRepository` - мок для existsByUsername, existsByEmail, save
- `PasswordEncoder` - мок для encode
- `JwtTokenProvider` - не используется в register
- `AuthenticationManager` - не используется в register

#### 1.2 `login(AuthRequest request)`

**Контракт:**
- При успехе: возвращается AuthResponse с accessToken и refreshToken
- При ошибке: выбрасывается исключение аутентификации
- Токены генерируются только для аутентифицированных пользователей

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `login_Success` | Успешная аутентификация | AuthResponse с токенами |
| 2 | `login_AuthenticationFailed` | Неверный пароль | RuntimeException |
| 3 | `login_UserNotFound` | Пользователь не найден | RuntimeException |
| 4 | `login_TokensGenerated` | Проверка генерации токенов | accessToken и refreshToken не null |

**Моки:**
- `AuthenticationManager` - мок для authenticate
- `UserRepository` - мок для findByUsername
- `JwtTokenProvider` - мок для generateToken

#### 1.3 `getCurrentUser(String username)`

**Контракт:**
- При успехе: возвращается UserDto
- При отсутствии: выбрасывается RuntimeException
- Пароль никогда не возвращается в ответе

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `getCurrentUser_Success` | Получение существующего пользователя | UserDto с данными |
| 2 | `getCurrentUser_NotFound` | Пользователь не найден | RuntimeException |
| 3 | `getCurrentUser_NoPassword` | Проверка отсутствия пароля в DTO | Пароль не включен в DTO |

**Моки:**
- `UserRepository` - мок для findByUsername

---

## 2. TrackServiceTest

### Тестируемые методы

#### 2.1 `getUserTracks(Long userId, Pageable pageable)`

**Контракт:**
- Возвращает Page<TrackDto> с треками пользователя
- Включает собственные треки и доступные треки

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `getUserTracks_Success` | Получение треков пользователя | Page с треками |
| 2 | `getUserTracks_Empty` | Нет треков у пользователя | Пустой Page |
| 3 | `getUserTracks_WithAccessible` | Треки с доступом | Page включает доступные треки |

**Моки:**
- `TrackRepository` - мок для findByOwnerId, findAccessibleTracks
- `PermissionService` - мок для getUserRole

#### 2.2 `getTrackById(Long trackId, Long userId)`

**Контракт:**
- При успехе: возвращается TrackDto
- При отсутствии трека: ResourceNotFoundException
- При отсутствии прав: AccessDeniedException

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `getTrackById_Success` | Получение трека с правами | TrackDto |
| 2 | `getTrackById_NotFound` | Трек не найден | ResourceNotFoundException |
| 3 | `getTrackById_AccessDenied` | Нет прав доступа | AccessDeniedException |

**Моки:**
- `TrackRepository` - мок для findByIdWithOwner
- `PermissionService` - мок для hasPermission

#### 2.3 `createTrack(CreateTrackRequest request, Long userId)`

**Контракт:**
- При успехе: возвращается TrackDto с id нового трека
- При отсутствии пользователя: ResourceNotFoundException

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `createTrack_Success` | Создание нового трека | TrackDto с id |
| 2 | `createTrack_UserNotFound` | Пользователь не найден | ResourceNotFoundException |
| 3 | `createTrack_TimestampsSet` | Проверка установки createdAt/updatedAt | Временные метки установлены |

**Моки:**
- `UserRepository` - мок для findById
- `TrackRepository` - мок для save

#### 2.4 `updateTrack(Long trackId, UpdateTrackRequest request, Long userId)`

**Контракт:**
- При успехе: возвращается обновленный TrackDto
- При отсутствии трека: ResourceNotFoundException
- При отсутствии прав: AccessDeniedException
- updatedAt обновляется

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `updateTrack_Success` | Обновление трека с правами | Обновленный TrackDto |
| 2 | `updateTrack_NotFound` | Трек не найден | ResourceNotFoundException |
| 3 | `updateTrack_AccessDenied` | Нет прав EDIT | AccessDeniedException |
| 4 | `updateTrack_UpdatedAt` | Проверка обновления updatedAt | updatedAt обновлен |

**Моки:**
- `TrackRepository` - мок для findById, save
- `PermissionService` - мок для hasPermission

#### 2.5 `deleteTrack(Long trackId, Long userId)`

**Контракт:**
- При успехе: трек удален
- При отсутствии трека: ResourceNotFoundException
- При отсутствии прав: AccessDeniedException
- Только владелец может удалить

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `deleteTrack_Success` | Удаление трека владельцем | Трек удален |
| 2 | `deleteTrack_NotFound` | Трек не найден | ResourceNotFoundException |
| 3 | `deleteTrack_AccessDenied` | Не владелец | AccessDeniedException |

**Моки:**
- `TrackRepository` - мок для findById, delete
- `PermissionService` - не используется (проверка владельца напрямую)

---

## 3. PermissionServiceTest

### Тестируемые методы

#### 3.1 `hasPermission(Long trackId, Long userId, String requiredPermission)`

**Контракт:**
- Владелец имеет все права
- VIEW доступен для VIEW и EDIT
- EDIT доступен только для EDIT
- При отсутствии прав: false

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `hasPermission_Owner` | Владелец трека | true для любого права |
| 2 | `hasPermission_ViewPermission` | Пользователь с правом VIEW | true для VIEW, false для EDIT |
| 3 | `hasPermission_EditPermission` | Пользователь с правом EDIT | true для VIEW и EDIT |
| 4 | `hasPermission_NoPermission` | Нет прав | false |
| 5 | `hasPermission_InvalidPermission` | Неверное право | false |

**Моки:**
- `TrackRepository` - мок для isOwner
- `TrackPermissionRepository` - мок для findByTrackIdAndUserId

#### 3.2 `getUserRole(Long trackId, Long userId)`

**Контракт:**
- Владелец: "OWNER"
- С правом: тип права (VIEW/EDIT)
- Без прав: null

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `getUserRole_Owner` | Владелец трека | "OWNER" |
| 2 | `getUserRole_ViewPermission` | Пользователь с правом VIEW | "VIEW" |
| 3 | `getUserRole_EditPermission` | Пользователь с правом EDIT | "EDIT" |
| 4 | `getUserRole_NoPermission` | Нет прав | null |

**Моки:**
- `TrackRepository` - мок для isOwner
- `TrackPermissionRepository` - мок для findByTrackIdAndUserId

---

## 4. JwtTokenProviderTest

### Тестируемые методы

#### 4.1 `generateToken(String username, Long userId)`

**Контракт:**
- Генерирует валидный JWT токен
- Токен содержит username в subject
- Токен содержит userId в claims
- Токен имеет срок действия

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `generateToken_Success` | Генерация токена | Валидный токен |
| 2 | `generateToken_ContainsUsername` | Проверка username в subject | Username в subject |
| 3 | `generateToken_ContainsUserId` | Проверка userId в claims | UserId в claims |
| 4 | `generateToken_Expiration` | Проверка срока действия | Токен истекает через jwtExpirationMs |

**Моки:**
- Нет (использует ReflectionTestUtils для установки jwtSecret и jwtExpirationMs)

#### 4.2 `getUsernameFromToken(String token)`

**Контракт:**
- Возвращает username из токена
- При неверном токене: выбрасывает исключение

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `getUsernameFromToken_Success` | Получение username из валидного токена | Username |
| 2 | `getUsernameFromToken_InvalidToken` | Неверный токен | JwtException |

**Моки:**
- Нет

#### 4.3 `getUserIdFromToken(String token)`

**Контракт:**
- Возвращает userId из токена
- При неверном токене: выбрасывает исключение

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `getUserIdFromToken_Success` | Получение userId из валидного токена | UserId |
| 2 | `getUserIdFromToken_InvalidToken` | Неверный токен | JwtException |

**Моки:**
- Нет

#### 4.4 `validateToken(String token)`

**Контракт:**
- При валидном токене: true
- При неверном токене: false

**Тестовые кейсы:**

| # | Название | Описание | Ожидаемый результат |
|---|----------|----------|---------------------|
| 1 | `validateToken_Valid` | Валидный токен | true |
| 2 | `validateToken_Invalid` | Неверный токен | false |
| 3 | `validateToken_Expired` | Истекший токен | false |
| 4 | `validateToken_WrongSignature` | Неверная подпись | false |

**Моки:**
- Нет

---

## 5. Общие принципы тестирования

### 5.1 Использование Mockito

```java
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
    
    @Test
    void register_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        
        User savedUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .role("USER")
            .enabled(true)
            .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Act
        UserDto result = authService.register(request);
        
        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("USER", result.getRole());
        assertTrue(result.getEnabled());
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
}
```

### 5.2 Тестирование исключений

```java
@Test
void register_DuplicateUsername() {
    // Arrange
    RegisterRequest request = new RegisterRequest("existinguser", "test@example.com", "password123");
    when(userRepository.existsByUsername("existinguser")).thenReturn(true);
    
    // Act & Assert
    assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    
    verify(userRepository).existsByUsername("existinguser");
    verify(userRepository, never()).save(any(User.class));
}
```

### 5.3 Тестирование с ReflectionTestUtils

```java
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    
    private JwtTokenProvider jwtTokenProvider;
    
    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "testSecretKeyForTesting12345678901234567890");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L);
    }
    
    @Test
    void generateToken_Success() {
        // Act
        String token = jwtTokenProvider.generateToken("testuser", 1L);
        
        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token));
        assertEquals(1L, jwtTokenProvider.getUserIdFromToken(token));
    }
}
```

---

## 6. Покрытие кода

### Целевые показатели

| Компонент | Минимальное покрытие | Целевое покрытие |
|-----------|---------------------|------------------|
| AuthService | 80% | 90% |
| TrackService | 80% | 90% |
| PermissionService | 80% | 90% |
| JwtTokenProvider | 90% | 95% |

### Инструменты для измерения покрытия

- JaCoCo (включен в spring-boot-starter-test)
- Генерация отчёта: `mvn clean test jacoco:report`

---

## 7. Порядок реализации

1. **AuthServiceTest** - базовый сервис аутентификации
2. **JwtTokenProviderTest** - независимый компонент без зависимостей
3. **PermissionServiceTest** - сервис проверки прав
4. **TrackServiceTest** - основной бизнес-сервис

---

## 8. Зависимости для тестирования

Все необходимые зависимости уже включены в `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Этот dependency включает:
- JUnit 5
- Mockito
- Spring Boot Test
- AssertJ

---

## 9. Запуск тестов

```bash
# Запуск всех тестов
mvn test

# Запуск конкретного тестового класса
mvn test -Dtest=AuthServiceTest

# Запуск с генерацией отчёта покрытия
mvn clean test jacoco:report
```

---

## 10. Чеклист завершения

- [ ] Все тестовые классы созданы
- [ ] Все тестовые кейсы реализованы
- [ ] Все тесты проходят успешно
- [ ] Покрытие кода соответствует целевым показателям
- [ ] Отчёт JaCoCo сгенерирован
- [ ] Документация обновлена

---

*Дата создания: 2026-04-16*
*Статус: План готов к реализации*
