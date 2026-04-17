# GRACE Skill: Исправление ошибок

## Назначение

Этот скилл обеспечивает исправление ошибок с сохранением контрактов, семантической разметки и AI-friendly логирования в соответствии с методологией GRACE.

---

## Ключевые принципы

1. **Сохранение контрактов** — контракты не меняются без явной необходимости
2. **Сохранение семантики** — смысл кода не меняется
3. **Сохранение логов** — log-маркеры сохраняются
4. **Минимальные изменения** — только необходимые исправления
5. **Обновление правил** — новые ошибки добавляются в error-patterns.md

---

## Процесс исправления ошибок

### Шаг 1: Анализ ошибки

Прочитать отчёт о верификации или ошибку:

```markdown
# Verification Report

## Issues

### Critical Issues
1. **[HIGH]** testRegisterWithInvalidEmail fails - Wrong exception type
   - **File**: `src/test/java/org/homework/service/AuthServiceTest.java`
   - **Line**: 45
   - **Expected**: ValidationException
   - **Actual**: UserAlreadyExistsException
```

### Шаг 2: Понимание причины

Проанализировать код и понять причину ошибки:

```java
// [START_AUTH_REGISTER]
/*
 * ANCHOR: AUTH_REGISTER
 * PURPOSE: Регистрация нового пользователя.
 * 
 * @PreConditions:
 * - email: валидный email формат
 * - password: непустая строка, min 8 символов
 * - пользователь с таким email не существует
 * 
 * @PostConditions:
 * - при ошибке валидации: выбрасывается ValidationException
 * - при дубликате email: выбрасывается UserAlreadyExistsException
 */
public AuthResponse register(RegisterRequest request) {
    log.info("AUTH_REGISTER ENTRY - email: {}", request.getEmail());
    
    // Validate email
    if (!isValidEmail(request.getEmail())) {
        log.warn("AUTH_REGISTER DECISION - reject_invalid_email");
        throw new ValidationException("Invalid email format");
    }
    
    // Check if user exists
    boolean userExists = userRepository.existsByEmail(request.getEmail());
    if (userExists) {
        log.warn("AUTH_REGISTER DECISION - reject_duplicate_email");
        throw new UserAlreadyExistsException("User already exists");
    }
    
    // ... rest of code
}
// [END_AUTH_REGISTER]
```

**Анализ**: Ошибка в тесте — тест ожидает ValidationException, но код выбрасывает UserAlreadyExistsException. Нужно исправить тест.

### Шаг 3: Исправление ошибки

Исправить ошибку с сохранением контракта и логов:

```java
// [START_AUTH_REGISTER]
/*
 * ANCHOR: AUTH_REGISTER
 * PURPOSE: Регистрация нового пользователя.
 * 
 * @PreConditions:
 * - email: валидный email формат
 * - password: непустая строка, min 8 символов
 * - пользователь с таким email не существует
 * 
 * @PostConditions:
 * - при ошибке валидации: выбрасывается ValidationException
 * - при дубликате email: выбрасывается UserAlreadyExistsException
 */
public AuthResponse register(RegisterRequest request) {
    log.info("AUTH_REGISTER ENTRY - email: {}", request.getEmail());
    
    // Validate email
    if (!isValidEmail(request.getEmail())) {
        log.warn("AUTH_REGISTER DECISION - reject_invalid_email - email: {}", 
            request.getEmail());
        log.info("AUTH_REGISTER EXIT - rejected - reason: invalid_email");
        throw new ValidationException("Invalid email format");
    }
    
    // Check if user exists
    boolean userExists = userRepository.existsByEmail(request.getEmail());
    log.debug("AUTH_REGISTER CHECK - user_exists - result: {}", userExists);
    
    if (userExists) {
        log.warn("AUTH_REGISTER DECISION - reject_duplicate_email - email: {}", 
            request.getEmail());
        log.info("AUTH_REGISTER EXIT - rejected - reason: duplicate_email");
        throw new UserAlreadyExistsException("User already exists");
    }
    
    // ... rest of code
}
// [END_AUTH_REGISTER]
```

### Шаг 4: Обновление теста

Исправить тест с сохранением контракта:

```java
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

@Test
void testRegisterWithDuplicateEmail() {
    // Arrange
    RegisterRequest request = new RegisterRequest();
    request.setEmail("existing@example.com");
    request.setUsername("testuser");
    request.setPassword("password123");
    
    // Create existing user
    User existingUser = new User();
    existingUser.setEmail("existing@example.com");
    userRepository.save(existingUser);
    
    // Act & Assert
    assertThrows(UserAlreadyExistsException.class, () -> {
        authService.register(request);
    });
}
```

### Шаг 5: Верификация исправления

Запустить тесты и убедиться, что ошибка исправлена:

```bash
mvn test
# All tests pass
```

### Шаг 6: Обновление error-patterns.md

Добавить правило для предотвращения повторения ошибки:

```markdown
### RULE_TEST_001: Соответствие ожидаемых исключений

**Проблема**: [2026-04-17] Тест ожидал ValidationException, но код выбрасывал UserAlreadyExistsException.

**Причина**: Тест не соответствовал контракту метода.

**Решение**:
1. Проверить контракт метода (@PostConditions)
2. Убедиться, что тест ожидает правильное исключение
3. Создать отдельные тесты для разных сценариев

**Пример**:
❌ Неправильно:
```java
@Test
void testRegisterWithInvalidEmail() {
    assertThrows(ValidationException.class, () -> {
        authService.register(request);
    });
}
// Но email уже существует, поэтому выбрасывается UserAlreadyExistsException
```

✅ Правильно:
```java
@Test
void testRegisterWithInvalidEmail() {
    // Arrange: email валидный, но не существует
    RegisterRequest request = new RegisterRequest();
    request.setEmail("invalid-email");
    // ...
    
    assertThrows(ValidationException.class, () -> {
        authService.register(request);
    });
}

@Test
void testRegisterWithDuplicateEmail() {
    // Arrange: email уже существует
    RegisterRequest request = new RegisterRequest();
    request.setEmail("existing@example.com");
    // ...
    
    assertThrows(UserAlreadyExistsException.class, () -> {
        authService.register(request);
    });
}
```

**Применимость**: При написании тестов для методов с несколькими сценариями ошибок.

**Дата добавления**: 2026-04-17
**Инцидент**: testRegisterWithInvalidEmail fails
```

---

## Шаблоны исправления ошибок

### Шаблон 1: Исправление логики

```java
// [START_[ANCHOR_ID]]
/*
 * ANCHOR: [ANCHOR_ID]
 * PURPOSE: [Назначение]
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
public [ReturnType] [methodName]([ParameterType] [paramName]) {
    log.info("[ANCHOR_ID] ENTRY - [paramName]: {}", [paramName]);
    
    // Fix: Добавлена проверка предусловия
    if ([preconditionCheck]) {
        log.warn("[ANCHOR_ID] DECISION - reject_[reason] - [paramName]: {}", [paramName]);
        log.info("[ANCHOR_ID] EXIT - rejected - reason: [reason]");
        throw new [ExceptionType]("[Error message]");
    }
    
    // Fix: Исправлена логика
    [ReturnType] result = [fixedLogic];
    
    log.info("[ANCHOR_ID] STATE_CHANGE - entity: [entity] - action: [action]");
    log.info("[ANCHOR_ID] EXIT - success - result: {}", result);
    return result;
}
// [END_[ANCHOR_ID]]
```

### Шаблон 2: Добавление логов

```java
// [START_[ANCHOR_ID]]
/*
 * ANCHOR: [ANCHOR_ID]
 * PURPOSE: [Назначение]
 * ...
 */
public [ReturnType] [methodName]([ParameterType] [paramName]) {
    // Fix: Добавлен ENTRY лог
    log.info("[ANCHOR_ID] ENTRY - [paramName]: {}", [paramName]);
    
    // Fix: Добавлен CHECK лог
    boolean isValid = [check];
    log.debug("[ANCHOR_ID] CHECK - [check_name] - result: {}", isValid);
    
    if (!isValid) {
        // Fix: Добавлен DECISION лог
        log.warn("[ANCHOR_ID] DECISION - reject_[reason]");
        // Fix: Добавлен ERROR лог
        log.error("[ANCHOR_ID] ERROR - reason: [reason]");
        // Fix: Добавлен EXIT лог
        log.info("[ANCHOR_ID] EXIT - rejected - reason: [reason]");
        throw new [ExceptionType]("[Error message]");
    }
    
    [ReturnType] result = [businessLogic];
    
    // Fix: Добавлен STATE_CHANGE лог
    log.info("[ANCHOR_ID] STATE_CHANGE - entity: [entity] - action: [action]");
    
    // Fix: Добавлен EXIT лог
    log.info("[ANCHOR_ID] EXIT - success - result: {}", result);
    return result;
}
// [END_[ANCHOR_ID]]
```

### Шаблон 3: Исправление контракта

```java
// [START_[ANCHOR_ID]]
/*
 * ANCHOR: [ANCHOR_ID]
 * PURPOSE: [Назначение]
 * 
 * @PreConditions:
 * - [условие 1]
 * - Fix: Добавлено условие 2
 * 
 * @PostConditions:
 * - [гарантия 1]
 * - Fix: Добавлена гарантия 2
 * 
 * @Invariants:
 * - [инвариант 1]
 * - Fix: Добавлен инвариант 2
 * 
 * @SideEffects:
 * - [побочный эффект 1]
 * - Fix: Добавлен побочный эффект 2
 * 
 * @ForbiddenChanges:
 * - [что запрещено менять]
 * - Fix: Добавлен запрет 2
 */
public [ReturnType] [methodName]([ParameterType] [paramName]) {
    log.info("[ANCHOR_ID] ENTRY - [paramName]: {}", [paramName]);
    
    // Fix: Реализовано новое условие
    if ([newCondition]) {
        log.warn("[ANCHOR_ID] DECISION - reject_[reason]");
        log.info("[ANCHOR_ID] EXIT - rejected - reason: [reason]");
        throw new [ExceptionType]("[Error message]");
    }
    
    [ReturnType] result = [businessLogic];
    
    // Fix: Реализован новый побочный эффект
    [newSideEffect];
    
    log.info("[ANCHOR_ID] STATE_CHANGE - entity: [entity] - action: [action]");
    log.info("[ANCHOR_ID] EXIT - success - result: {}", result);
    return result;
}
// [END_[ANCHOR_ID]]
```

---

## Чеклист исправления ошибок

### Перед исправлением

- [ ] Прочитан отчёт об ошибке
- [ ] Понята причина ошибки
- [ ] Проверен контракт метода
- [ ] Проверены существующие тесты

### При исправлении

- [ ] Контракт сохранён (или обновлён с обоснованием)
- [ ] Семантика сохранена
- [ ] Логи сохранены (или добавлены)
- [ ] Изменения минимальны
- [ ] Тесты обновлены

### После исправления

- [ ] Все тесты проходят
- [ ] Код соответствует контракту
- [ ] Логи соответствуют ai-logging.md
- [ ] Правило добавлено в error-patterns.md
- [ ] Отчёт об исправлении создан

---

## Шаблон отчёта об исправлении

```markdown
# Fix Report

## Metadata
- **Date**: 2026-04-17T06:30:00Z
- **Feature**: FEAT-001 User Authentication
- **Issue**: testRegisterWithInvalidEmail fails

## Issue Description
Test expected ValidationException, but code threw UserAlreadyExistsException.

## Root Cause
Test did not match the contract of the method. The contract specifies:
- ValidationException for invalid email
- UserAlreadyExistsException for duplicate email

## Fix Applied
1. Updated test to expect correct exception
2. Added separate test for duplicate email scenario
3. Added missing log markers

## Changes Made
- **File**: `src/test/java/org/homework/service/AuthServiceTest.java`
  - Updated `testRegisterWithInvalidEmail` to expect ValidationException
  - Added `testRegisterWithDuplicateEmail` to test duplicate email scenario

- **File**: `src/main/java/org/homework/service/AuthService.java`
  - Added missing CHECK log for user existence check
  - Added missing DECISION log for duplicate email rejection

## Verification
- All tests pass
- Code matches contract
- Log markers are present

## Rule Added
- RULE_TEST_001: Соответствие ожидаемых исключений

## Conclusion
**Status**: FIXED
**Action Required**: None
```

---

## Специфика TrackHub

### Backend (Java 17 + Spring Boot)

- **Логирование**: SLF4J + Logback
- **Исключения**: Кастомные исключения (ValidationException, UserAlreadyExistsException)
- **Контракты**: Javadoc контракты с @PreConditions, @PostConditions, @Invariants, @SideEffects, @ForbiddenChanges

### Frontend (Angular 17 + TypeScript)

- **Логирование**: `logLine` из `core/lib/log.ts`
- **Ошибки**: Error handling в RxJS catchError
- **Контракты**: TypeScript комментарии с контрактом

---

## Связанные документы

- [Error Patterns](../rules/error-patterns.md) — паттерны ошибок
- [Error Driven Learning](../rules/error-driven-learning.md) — обучение на ошибках
- [Semantic Markup Rules](../rules/semantic-code-markup.md) — правила разметки

---

*Создано: 2026-04-17*
*GRACE Skill: Fix*
