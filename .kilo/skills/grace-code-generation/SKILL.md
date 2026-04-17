# GRACE Skill: Генерация кода

## Назначение

Этот скилл обеспечивает генерацию кода в соответствии с методологией GRACE, включая семантическую разметку, AI-friendly логирование и соблюдение контрактов.

---

## Ключевые принципы

1. **Контракт-первый** — код генерируется на основе контракта
2. **Семантическая разметка** — каждый блок имеет ANCHOR и контракт
3. **AI-friendly логирование** — ENTRY/EXIT/BRANCH/DECISION/ERROR точки
4. **TDD** — тесты пишутся ДО кода
5. **Соответствие спецификации** — код соответствует Feature Spec

---

## Процесс генерации кода

### Шаг 1: Анализ Feature Specification

Прочитать Feature Specification из `plans/features/FEAT-XXX.md`:

```markdown
# FEAT-001: User Authentication

## Requirements
- User can register with email and password
- User can login with email and password
- System issues JWT tokens

## API Endpoints
- POST /api/auth/register
- POST /api/auth/login

## Contracts
- AUTH_REGISTER
- AUTH_LOGIN
```

### Шаг 2: Создание контрактов

Для каждого метода создать контракт:

```java
// [START_AUTH_REGISTER]
/*
 * ANCHOR: AUTH_REGISTER
 * PURPOSE: Регистрация нового пользователя.
 * 
 * @PreConditions:
 * - email: валидный email формат
 * - password: непустая строка, min 8 символов
 * - username: непустая строка, min 3 символа
 * - пользователь с таким email не существует
 * 
 * @PostConditions:
 * - при успехе: возвращается AuthResponse с access_token и refresh_token
 * - при ошибке валидации: выбрасывается ValidationException
 * - при дубликате email: выбрасывается UserAlreadyExistsException
 * 
 * @Invariants:
 * - пароль никогда не возвращается в ответе
 * - пароль хешируется перед сохранением
 * - токены всегда валидны при успешной регистрации
 * 
 * @SideEffects:
 * - создаёт запись пользователя в БД
 * - создаёт refresh_token в БД
 * - логирует регистрацию
 * 
 * @ForbiddenChanges:
 * - нельзя убрать валидацию email
 * - нельзя убрать хеширование пароля
 * - нельзя убрать логирование
 */
// [END_AUTH_REGISTER]
```

### Шаг 3: Генерация кода с логированием

```java
// [START_AUTH_REGISTER]
/*
 * ANCHOR: AUTH_REGISTER
 * PURPOSE: Регистрация нового пользователя.
 * ...
 */
@Slf4j
@Service
public class AuthService {
    
    public AuthResponse register(RegisterRequest request) {
        log.info("AUTH_REGISTER ENTRY - email: {}, username: {}", 
            request.getEmail(), request.getUsername());
        
        // Validate email
        if (!isValidEmail(request.getEmail())) {
            log.warn("AUTH_REGISTER DECISION - reject_invalid_email - email: {}", 
                request.getEmail());
            log.info("AUTH_REGISTER EXIT - rejected - reason: invalid_email");
            throw new ValidationException("Invalid email format");
        }
        
        // Validate password
        if (request.getPassword().length() < 8) {
            log.warn("AUTH_REGISTER DECISION - reject_short_password - length: {}", 
                request.getPassword().length());
            log.info("AUTH_REGISTER EXIT - rejected - reason: short_password");
            throw new ValidationException("Password must be at least 8 characters");
        }
        
        // Check if user exists
        boolean userExists = userRepository.existsByEmail(request.getEmail());
        log.debug("AUTH_REGISTER CHECK - user_exists - result: {}", userExists);
        
        if (userExists) {
            log.warn("AUTH_REGISTER DECISION - reject_duplicate_email - email: {}", 
                request.getEmail());
            log.info("AUTH_REGISTER EXIT - rejected - reason: duplicate_email");
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        
        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("AUTH_REGISTER CHECK - password_hashed - result: true");
        
        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(hashedPassword);
        user.setRole(Role.USER);
        user.setEnabled(true);
        user = userRepository.save(user);
        
        log.info("AUTH_REGISTER STATE_CHANGE - entity: user - action: created - userId: {}", 
            user.getId());
        
        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        log.info("AUTH_REGISTER STATE_CHANGE - entity: tokens - action: generated - userId: {}", 
            user.getId());
        
        log.info("AUTH_REGISTER EXIT - success - userId: {}", user.getId());
        
        return new AuthResponse(accessToken, refreshToken, mapToUserDto(user));
    }
}
// [END_AUTH_REGISTER]
```

### Шаг 4: Генерация тестов

```java
@Test
void testRegisterSuccess() {
    // Arrange
    RegisterRequest request = new RegisterRequest();
    request.setEmail("user@example.com");
    request.setUsername("testuser");
    request.setPassword("password123");
    
    // Act
    AuthResponse response = authService.register(request);
    
    // Assert
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());
    assertNotNull(response.getUser());
    assertEquals("user@example.com", response.getUser().getEmail());
}

@Test
void testRegisterWithInvalidEmail() {
    // Arrange
    RegisterRequest request = new RegisterRequest();
    request.setEmail("invalid-email");
    request.setUsername("testuser");
    request.setPassword("password123");
    
    // Act & Assert
    assertThrows(ValidationException.class, () -> {
        authService.register(request);
    });
}
```

---

## Шаблоны генерации кода

### Backend (Java 17 + Spring Boot)

#### Сервисный метод

```java
// [START_[ANCHOR_ID]]
/*
 * ANCHOR: [ANCHOR_ID]
 * PURPOSE: [Одна фраза — зачем существует этот метод]
 * 
 * @PreConditions:
 * - [условие 1]
 * - [условие 2]
 * 
 * @PostConditions:
 * - [гарантия 1 при успехе]
 * - [гарантия 2 при ошибке]
 * 
 * @Invariants:
 * - [инвариант 1]
 * - [инвариант 2]
 * 
 * @SideEffects:
 * - [побочный эффект 1]
 * 
 * @ForbiddenChanges:
 * - [что запрещено менять]
 */
@Slf4j
@Service
public class [ServiceName] {
    
    public [ReturnType] [methodName]([ParameterType] [paramName]) {
        log.info("[ANCHOR_ID] ENTRY - [paramName]: {}", [paramName]);
        
        // Validate preconditions
        if ([preconditionCheck]) {
            log.warn("[ANCHOR_ID] DECISION - reject_[reason] - [paramName]: {}", [paramName]);
            log.info("[ANCHOR_ID] EXIT - rejected - reason: [reason]");
            throw new [ExceptionType]("[Error message]");
        }
        
        // Execute business logic
        [ReturnType] result = [businessLogic];
        
        log.info("[ANCHOR_ID] STATE_CHANGE - entity: [entity] - action: [action]");
        
        log.info("[ANCHOR_ID] EXIT - success - result: {}", result);
        return result;
    }
}
// [END_[ANCHOR_ID]]
```

#### Контроллер

```java
// [START_[ANCHOR_ID]]
/*
 * ANCHOR: [ANCHOR_ID]
 * PURPOSE: [Одна фраза — зачем существует этот эндпоинт]
 * 
 * @PreConditions:
 * - [условие 1]
 * - [условие 2]
 * 
 * @PostConditions:
 * - [гарантия 1 при успехе]
 * - [гарантия 2 при ошибке]
 * 
 * @Invariants:
 * - [инвариант 1]
 * 
 * @SideEffects:
 * - [побочный эффект 1]
 * 
 * @ForbiddenChanges:
 * - [что запрещено менять]
 */
@Slf4j
@RestController
@RequestMapping("/api/[resource]")
public class [ControllerName] {
    
    @PostMapping
    public ResponseEntity<[ResponseType]> [methodName](
            @Valid @RequestBody [RequestType] request,
            Authentication authentication) {
        log.info("[ANCHOR_ID] ENTRY - request: {}", request);
        
        Long userId = getUserIdFromAuthentication(authentication);
        [ResponseType] result = [serviceName].[methodName](request, userId);
        
        log.info("[ANCHOR_ID] EXIT - success - resultId: {}", result.getId());
        return ResponseEntity.ok(result);
    }
}
// [END_[ANCHOR_ID]]
```

### Frontend (Angular 17 + TypeScript)

#### Сервис

```typescript
// [START_[ANCHOR_ID]]
/*
 * ANCHOR: [ANCHOR_ID]
 * PURPOSE: [Одна фраза — зачем существует этот метод]
 * 
 * @PreConditions:
 * - [условие 1]
 * - [условие 2]
 * 
 * @PostConditions:
 * - [гарантия 1 при успехе]
 * - [гарантия 2 при ошибке]
 * 
 * @Invariants:
 * - [инвариант 1]
 * 
 * @SideEffects:
 * - [побочный эффект 1]
 * 
 * @ForbiddenChanges:
 * - [что запрещено менять]
 */
import { logLine } from '../core/lib/log';

@Injectable({ providedIn: 'root' })
export class [ServiceName] {
  
  [methodName]([paramType]: [paramName]): Observable<[ResponseType]> {
    logLine('[module]', 'DEBUG', '[methodName]', '[ANCHOR_ID]', 'ENTRY', { [paramName] });
    
    return this.http.post<[ResponseType]>('/api/[endpoint]', { [paramName] }).pipe(
      tap(response => {
        logLine('[module]', 'DEBUG', '[methodName]', '[ANCHOR_ID]', 'CHECK', {
          check: 'response_received',
          result: response !== undefined
        });
        
        logLine('[module]', 'INFO', '[methodName]', '[ANCHOR_ID]', 'STATE_CHANGE', {
          entity: '[entity]',
          action: '[action]'
        });
        
        logLine('[module]', 'DEBUG', '[methodName]', '[ANCHOR_ID]', 'EXIT', { result: 'success' });
      }),
      catchError(error => {
        logLine('[module]', 'ERROR', '[methodName]', '[ANCHOR_ID]', 'ERROR', {
          reason: error.message,
          status: error.status
        });
        logLine('[module]', 'DEBUG', '[methodName]', '[ANCHOR_ID]', 'EXIT', { result: 'rejected' });
        return throwError(() => error);
      })
    );
  }
}
// [END_[ANCHOR_ID]]
```

#### Компонент

```typescript
// [START_[ANCHOR_ID]]
/*
 * ANCHOR: [ANCHOR_ID]
 * PURPOSE: [Одна фраза — зачем существует этот компонент]
 * 
 * @PreConditions:
 * - [условие 1]
 * 
 * @PostConditions:
 * - [гарантия 1]
 * 
 * @Invariants:
 * - [инвариант 1]
 * 
 * @SideEffects:
 * - [побочный эффект 1]
 * 
 * @ForbiddenChanges:
 * - [что запрещено менять]
 */
import { logLine } from '../../core/lib/log';

@Component({
  selector: 'app-[component-name]',
  templateUrl: './[component-name].component.html',
  styleUrls: ['./[component-name].component.scss']
})
export class [ComponentName]Component implements OnInit {
  
  ngOnInit(): void {
    logLine('[module]', 'DEBUG', 'ngOnInit', '[ANCHOR_ID]', 'ENTRY', {});
    
    this.loadData().subscribe(() => {
      logLine('[module]', 'DEBUG', 'ngOnInit', '[ANCHOR_ID]', 'EXIT', { result: 'success' });
    });
  }
  
  loadData(): Observable<void> {
    logLine('[module]', 'DEBUG', 'loadData', '[ANCHOR_ID]', 'ENTRY', {});
    
    return this.service.getData().pipe(
      tap(data => {
        logLine('[module]', 'INFO', 'loadData', '[ANCHOR_ID]', 'STATE_CHANGE', {
          entity: 'data',
          action: 'loaded'
        });
      }),
      tap(() => {
        logLine('[module]', 'DEBUG', 'loadData', '[ANCHOR_ID]', 'EXIT', { result: 'success' });
      })
    );
  }
}
// [END_[ANCHOR_ID]]
```

---

## Чеклист генерации кода

### Перед генерацией

- [ ] Прочитана Feature Specification
- [ ] Созданы контракты для всех методов
- [ ] Написаны тесты для всех методов
- [ ] Проверено соответствие проекту (project_context.md)

### При генерации

- [ ] Каждый метод имеет ANCHOR и контракт
- [ ] Каждый метод имеет ENTRY и EXIT логи
- [ ] Все ветвления имеют BRANCH или DECISION логи
- [ ] Все ошибки имеют ERROR логи
- [ ] Все побочные эффекты логированы (STATE_CHANGE)
- [ ] Предусловия проверены
- [ ] Постусловия обеспечены
- [ ] Инварианты сохранены
- [ ] Запреты соблюдены

### После генерации

- [ ] Все тесты проходят
- [ ] Код соответствует контракту
- [ ] Логи соответствуют ai-logging.md
- [ ] Код соответствует стилю проекта
- [ ] Нет дублирования кода

---

## Специфика TrackHub

### Backend (Java 17 + Spring Boot)

- **Логирование**: SLF4J + Logback с `@Slf4j`
- **Валидация**: Bean Validation (`@Valid`, `@Size`, `@Pattern`, `@NotBlank`)
- **Безопасность**: Spring Security + JWT, `@PreAuthorize` на контроллерах
- **DTO**: Отдельные классы для request и response
- **Маппинг**: Отдельные методы для маппинга сущностей в DTO

### Frontend (Angular 17 + TypeScript)

- **Логирование**: `logLine` из `core/lib/log.ts`
- **HTTP**: Angular HttpClient с `AuthInterceptor`
- **Формы**: Reactive Forms с валидацией
- **DTO**: Интерфейсы TypeScript для request и response
- **Сервисы**: Injectable сервисы с Observable

---

## Связанные документы

- [Semantic Markup Rules](../rules/semantic-code-markup.md) — правила разметки
- [AI Logging Rules](../rules/ai-logging.md) — правила логирования
- [Project Context](../rules/project_context.md) — контекст проекта
- [Примеры](../examples/) — примеры кода

---

*Создано: 2026-04-17*
*GRACE Skill: Code Generation*
