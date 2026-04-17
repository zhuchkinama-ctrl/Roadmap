# GRACE Skill: Генерация тестов

## Назначение

Этот скилл обеспечивает создание трёхуровневой системы тестирования для проверки контрактов, логов и интеграционных сценариев в соответствии с методологией GRACE.

---

## Ключевые принципы

1. **TDD** — тесты пишутся ДО кода
2. **3 уровня тестирования** — детерминированные, траекторные, интеграционные
3. **Проверка контрактов** — тесты верифицируют постусловия
4. **Проверка логов** — тесты верифицируют log-маркеры
5. **E2E сценарии** — тесты проверяют сквозной поток

---

## Уровни тестирования

### Level 1: Детерминированные тесты

**Назначение**: Проверка постусловий контрактов.

**Что проверяем**:
- Возвращаемые значения
- Состояние объектов
- Исключения
- Побочные эффекты

#### Backend (JUnit 5)

```java
@Test
void testLoginSuccess() {
    // Arrange
    String email = "user@example.com";
    String password = "password123";
    
    // Act
    AuthResponse response = authService.login(email, password);
    
    // Assert
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());
    assertNotNull(response.getUser());
    assertEquals(email, response.getUser().getEmail());
}

@Test
void testLoginWithInvalidCredentials() {
    // Arrange
    String email = "user@example.com";
    String password = "wrongpassword";
    
    // Act & Assert
    assertThrows(AuthenticationException.class, () -> {
        authService.login(email, password);
    });
}
```

#### Frontend (Jasmine)

```typescript
it('should login successfully', (done) => {
  // Arrange
  const email = 'user@example.com';
  const password = 'password123';
  
  // Act
  authService.login(email, password).subscribe(response => {
    // Assert
    expect(response.accessToken).toBeDefined();
    expect(response.refreshToken).toBeDefined();
    expect(response.user.email).toBe(email);
    done();
  });
});

it('should throw error with invalid credentials', (done) => {
  // Arrange
  const email = 'user@example.com';
  const password = 'wrongpassword';
  
  // Act & Assert
  authService.login(email, password).subscribe({
    next: () => fail('Should have thrown error'),
    error: (error) => {
      expect(error.status).toBe(401);
      done();
    }
  });
});
```

---

### Level 2: Тесты траектории

**Назначение**: Проверка log-маркеров и траектории выполнения.

**Что проверяем**:
- ENTRY логи
- EXIT логи
- BRANCH/DECISION логи
- ERROR логи
- STATE_CHANGE логи

#### Backend (JUnit 5 + Logback)

```java
@Test
void testAuthLoginMarkers() {
    // Capture logs
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    Logger logger = (Logger) LoggerFactory.getLogger(AuthService.class);
    logger.addAppender(appender);
    
    // Execute
    authService.login("user@example.com", "password");
    
    // Verify markers
    List<ILoggingEvent> logs = appender.list;
    assertTrue(logs.stream().anyMatch(l -> l.getMessage().contains("AUTH_LOGIN ENTRY")));
    assertTrue(logs.stream().anyMatch(l -> l.getMessage().contains("AUTH_LOGIN EXIT")));
    
    // Verify decision logs
    assertTrue(logs.stream().anyMatch(l -> l.getMessage().contains("DECISION")));
    
    // Clean up
    logger.detachAppender(appender);
}

@Test
void testAuthLoginErrorMarkers() {
    // Capture logs
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    Logger logger = (Logger) LoggerFactory.getLogger(AuthService.class);
    logger.addAppender(appender);
    
    // Execute with invalid credentials
    try {
        authService.login("user@example.com", "wrongpassword");
        fail("Should have thrown exception");
    } catch (AuthenticationException e) {
        // Expected
    }
    
    // Verify error markers
    List<ILoggingEvent> logs = appender.list;
    assertTrue(logs.stream().anyMatch(l -> l.getMessage().contains("AUTH_LOGIN ENTRY")));
    assertTrue(logs.stream().anyMatch(l -> l.getMessage().contains("AUTH_LOGIN EXIT")));
    assertTrue(logs.stream().anyMatch(l -> l.getMessage().contains("ERROR")));
    
    // Clean up
    logger.detachAppender(appender);
}
```

#### Frontend (Jasmine + spyOn)

```typescript
it('should log ENTRY and EXIT markers', (done) => {
  // Arrange
  spyOn(logLine, 'logLine').and.callThrough();
  
  // Act
  authService.login('user@example.com', 'password123').subscribe(() => {
    // Assert
    expect(logLine).toHaveBeenCalledWith(
      'auth', 'DEBUG', 'login', 'AUTH_LOGIN', 'ENTRY', jasmine.any(Object)
    );
    expect(logLine).toHaveBeenCalledWith(
      'auth', 'DEBUG', 'login', 'AUTH_LOGIN', 'EXIT', jasmine.any(Object)
    );
    done();
  });
});

it('should log ERROR markers on failure', (done) => {
  // Arrange
  spyOn(logLine, 'logLine').and.callThrough();
  spyOn(http, 'post').and.returnValue(throwError(() => new Error('Network error')));
  
  // Act
  authService.login('user@example.com', 'password123').subscribe({
    next: () => fail('Should have thrown error'),
    error: () => {
      // Assert
      expect(logLine).toHaveBeenCalledWith(
        'auth', 'ERROR', 'login', 'AUTH_LOGIN', 'ERROR', jasmine.any(Object)
      );
      expect(logLine).toHaveBeenCalledWith(
        'auth', 'DEBUG', 'login', 'AUTH_LOGIN', 'EXIT', jasmine.any(Object)
      );
      done();
    }
  });
});
```

---

### Level 3: Интеграционные тесты

**Назначение**: Проверка E2E сценариев и интеграции компонентов.

**Что проверяем**:
- Сквозной поток данных
- Интеграцию между компонентами
- API эндпоинты
- UI сценарии

#### Backend (Spring Boot Test)

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testE2E_LoginAndAccessProtectedEndpoint() throws Exception {
        // Step 1: Login
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                Map.of("email", "user@example.com", "password", "password")
            )))
            .andExpect(status().isOk())
            .andReturn();
        
        String accessToken = JsonPath.read(
            loginResult.getResponse().getContentAsString(), 
            "$.accessToken"
        );
        
        // Step 2: Access protected endpoint
        mockMvc.perform(get("/api/tracks")
            .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }
    
    @Test
    void testE2E_RegisterAndLogin() throws Exception {
        // Step 1: Register
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                Map.of(
                    "username", "newuser",
                    "email", "newuser@example.com",
                    "password", "password123"
                )
            )))
            .andExpect(status().isOk());
        
        // Step 2: Login
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                Map.of("email", "newuser@example.com", "password", "password123")
            )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists());
    }
}
```

#### Frontend (Cypress)

```typescript
describe('E2E Login', () => {
  beforeEach(() => {
    cy.visit('/login');
  });
  
  it('should login and access dashboard', () => {
    // Step 1: Fill login form
    cy.get('[data-cy="email-input"]').type('user@example.com');
    cy.get('[data-cy="password-input"]').type('password123');
    
    // Step 2: Submit form
    cy.get('[data-cy="login-button"]').click();
    
    // Step 3: Verify redirect to dashboard
    cy.url().should('include', '/dashboard');
    
    // Step 4: Verify dashboard content
    cy.get('[data-cy="tracks-table"]').should('be.visible');
    cy.get('[data-cy="user-menu"]').should('contain', 'user@example.com');
  });
  
  it('should show error with invalid credentials', () => {
    // Step 1: Fill login form with invalid credentials
    cy.get('[data-cy="email-input"]').type('user@example.com');
    cy.get('[data-cy="password-input"]').type('wrongpassword');
    
    // Step 2: Submit form
    cy.get('[data-cy="login-button"]').click();
    
    // Step 3: Verify error message
    cy.get('[data-cy="error-message"]').should('be.visible');
    cy.get('[data-cy="error-message"]').should('contain', 'Invalid credentials');
    
    // Step 4: Verify still on login page
    cy.url().should('include', '/login');
  });
});
```

---

## TDD Workflow

### Шаг 1: Написать тест (Red)

```java
@Test
void testCreateTrackSuccess() {
    // Arrange
    CreateTrackRequest request = new CreateTrackRequest();
    request.setTitle("Test Track");
    request.setDescription("Test Description");
    
    // Act
    TrackDto result = trackService.createTrack(request, userId);
    
    // Assert
    assertNotNull(result.getId());
    assertEquals("Test Track", result.getTitle());
    assertEquals("Test Description", result.getDescription());
    assertEquals(userId, result.getOwnerId());
}
```

### Шаг 2: Запустить тест (Red)

```bash
mvn test
# Test should fail
```

### Шаг 3: Написать минимальный код (Green)

```java
public TrackDto createTrack(CreateTrackRequest request, Long userId) {
    log.info("CREATE_TRACK ENTRY - title: {}", request.getTitle());
    
    Track track = new Track();
    track.setTitle(request.getTitle());
    track.setDescription(request.getDescription());
    track.setOwner(userRepository.findById(userId).orElseThrow());
    track = trackRepository.save(track);
    
    log.info("CREATE_TRACK EXIT - trackId: {}", track.getId());
    return mapToTrackDto(track);
}
```

### Шаг 4: Запустить тест (Green)

```bash
mvn test
# Test should pass
```

### Шаг 5: Рефакторинг (если нужно)

```java
// Рефакторинг с сохранением контракта
private TrackDto mapToTrackDto(Track track) {
    TrackDto dto = new TrackDto();
    dto.setId(track.getId());
    dto.setTitle(track.getTitle());
    dto.setDescription(track.getDescription());
    dto.setOwnerId(track.getOwner().getId());
    return dto;
}
```

---

## Шаблоны тестов

### Шаблон для детерминированного теста

```java
@Test
void test[MethodName][Scenario]() {
    // Arrange
    // Подготовка тестовых данных
    
    // Act
    // Выполнение тестируемого метода
    
    // Assert
    // Проверка результатов
}
```

### Шаблон для теста траектории

```java
@Test
void test[MethodName]Markers() {
    // Arrange
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    Logger logger = (Logger) LoggerFactory.getLogger([ClassName].class);
    logger.addAppender(appender);
    
    // Act
    [methodName]([parameters]);
    
    // Assert
    List<ILoggingEvent> logs = appender.list;
    assertTrue(logs.stream().anyMatch(l -> l.getMessage().contains("[ANCHOR] ENTRY")));
    assertTrue(logs.stream().anyMatch(l -> l.getMessage().contains("[ANCHOR] EXIT")));
    
    // Clean up
    logger.detachAppender(appender);
}
```

### Шаблон для интеграционного теста

```java
@SpringBootTest
@AutoConfigureMockMvc
class [FeatureName]IntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testE2E_[ScenarioName]() throws Exception {
        // Step 1: [First action]
        MvcResult result1 = mockMvc.perform([request1])
            .andExpect(status().isOk())
            .andReturn();
        
        // Step 2: [Second action]
        mockMvc.perform([request2])
            .andExpect(status().isOk());
    }
}
```

---

## Чеклист тестирования

### Перед написанием кода

- [ ] Написан тест для новой функции
- [ ] Тест проверяет все постусловия контракта
- [ ] Тест проверяет все ветвления
- [ ] Тест проверяет обработку ошибок

### После написания кода

- [ ] Все тесты проходят
- [ ] Написаны тесты траектории для критичных функций
- [ ] Написаны интеграционные тесты для E2E сценариев
- [ ] Покрытие кода тестами ≥ 80%

### Перед коммитом

- [ ] Все тесты проходят локально
- [ ] Все тесты проходят в CI/CD
- [ ] Нет flaky тестов
- [ ] Тесты документируют ожидаемое поведение

---

## Специфика TrackHub

### Backend (Java 17 + Spring Boot)

- **Фреймворк**: JUnit 5 + Spring Test
- **Моки**: Mockito
- **Логирование**: Logback с ListAppender для тестов
- **Интеграционные тесты**: @SpringBootTest + @AutoConfigureMockMvc

### Frontend (Angular 17 + TypeScript)

- **Фреймворк**: Jasmine + Karma
- **Моки**: jasmine.createSpy, TestBed
- **E2E тесты**: Cypress
- **HTTP моки**: HttpClientTestingModule

---

## Связанные документы

- [TDD Rules](../rules/TDD.md) — правила TDD
- [Semantic Markup Rules](../rules/semantic-code-markup.md) — правила разметки
- [AI Logging Rules](../rules/ai-logging.md) — правила логирования

---

*Создано: 2026-04-17*
*GRACE Skill: Test Generation*
