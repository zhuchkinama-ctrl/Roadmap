# План реализации FEAT-001: Аутентификация и авторизация

**Дата создания:** 2026-04-17  
**Feature ID:** FEAT-001  
**Статус:** planning → implementation  
**Версия:** 1.0.0  
**Автор:** GRACE Skill: Code Generation

---

## Обзор

Этот документ содержит детальный план реализации фичи FEAT-001 (Аутентификация и авторизация) в соответствии с методологией GRACE. План включает:

1. **Анализ Feature Specification** — проверка соответствия требованиям GRACE
2. **Декомпозиция на задачи** — разбивка на конкретные шаги реализации
3. **Контракты компонентов** — формальное описание поведения каждого компонента
4. **План тестирования** — многоуровневое тестирование (TDD подход)
5. **Валидация** — проверка соответствия контрактам и требованиям

---

## 1. Анализ Feature Specification

### 1.1 Соответствие требованиям GRACE

| Требование GRACE | Статус | Комментарий |
|------------------|--------|-------------|
| Feature Specification Document | ✅ | `plans/features/FEAT-001-authentication.md` создан |
| Определение компонентов с якорями | ✅ | Все компоненты имеют ANCHOR_ID |
| Контракты для компонентов | ✅ | Контракты присутствуют в Feature Spec |
| План тестирования | ✅ | Тесты описаны по уровням |
| API спецификация | ✅ | Эндпоинты и DTO описаны |
| UI компоненты | ✅ | Компоненты и сервисы описаны |
| Acceptance Criteria | ✅ | 12 критериев приёмки определены |
| Dependencies | ✅ | Зависимости указаны |

**Вывод:** Feature Specification соответствует требованиям GRACE.

### 1.2 Структура компонентов

#### Backend компоненты

| ANCHOR_ID | Файл | Назначение | Статус |
|-----------|------|------------|--------|
| `AUTH_CONTROLLER` | `src/main/java/org/homework/controller/AuthController.java` | REST контроллер | ✅ Реализован |
| `AUTH_SERVICE` | `src/main/java/org/homework/service/AuthService.java` | Бизнес-логика | ✅ Реализован |
| `AUTH_SERVICE_REGISTER` | `AuthService.register()` | Метод регистрации | ✅ Реализован |
| `AUTH_SERVICE_LOGIN` | `AuthService.login()` | Метод входа | ✅ Реализован |
| `USER_REPOSITORY` | `src/main/java/org/homework/repository/UserRepository.java` | JPA репозиторий | ✅ Реализован |
| `USER_MODEL` | `src/main/java/org/homework/model/User.java` | JPA сущность | ✅ Реализован |
| `AUTH_REQUEST` | `src/main/java/org/homework/dto/request/AuthRequest.java` | DTO запроса входа | ✅ Реализован |
| `REGISTER_REQUEST` | `src/main/java/org/homework/dto/request/RegisterRequest.java` | DTO запроса регистрации | ✅ Реализован |
| `AUTH_RESPONSE` | `src/main/java/org/homework/dto/response/AuthResponse.java` | DTO ответа | ✅ Реализован |
| `USER_DTO` | `src/main/java/org/homework/dto/response/UserDto.java` | DTO пользователя | ✅ Реализован |
| `JWT_TOKEN_PROVIDER` | `src/main/java/org/homework/security/JwtTokenProvider.java` | Генерация JWT | ✅ Реализован |
| `JWT_AUTH_FILTER` | `src/main/java/org/homework/security/JwtAuthenticationFilter.java` | JWT фильтр | ✅ Реализован |
| `SECURITY_CONFIG` | `src/main/java/org/homework/security/SecurityConfig.java` | Конфигурация безопасности | ✅ Реализован |
| `USER_DETAILS_SERVICE` | `src/main/java/org/homework/security/CustomUserDetailsService.java` | Загрузка пользователя | ✅ Реализован |

#### Frontend компоненты

| ANCHOR_ID | Файл | Назначение | Статус |
|-----------|------|------------|--------|
| `LOGIN_COMPONENT` | `frontend/src/app/features/auth/login/login.component.ts` | Компонент входа | ✅ Реализован |
| `REGISTER_COMPONENT` | `frontend/src/app/features/auth/register/register.component.ts` | Компонент регистрации | ✅ Реализован |
| `AUTH_SERVICE_FRONTEND` | `frontend/src/app/core/services/auth.service.ts` | Сервис аутентификации | ✅ Реализован |
| `AUTH_GUARD` | `frontend/src/app/core/guards/auth.guard.ts` | Guard маршрутов | ✅ Реализован |
| `AUTH_INTERCEPTOR` | `frontend/src/app/core/interceptors/auth.interceptor.ts` | HTTP перехватчик | ✅ Реализован |
| `USER_MODEL_FRONTEND` | `frontend/src/app/shared/models/user.model.ts` | TypeScript модель | ✅ Реализован |

---

## 2. Декомпозиция на задачи

### 2.1 Приоритет задач

| Приоритет | Описание | Задачи |
|-----------|----------|--------|
| **P0** | Критичные задачи для базовой функциональности | 1-10 |
| **P1** | Важные задачи для полной функциональности | 11-15 |
| **P2** | Желательные задачи для улучшения | 16-20 |

### 2.2 Список задач

#### Backend задачи

| # | Задача | Файл | ANCHOR | Приоритет |
|---|--------|------|--------|-----------|
| 1 | Проверить и улучшить валидацию в `RegisterRequest` | `RegisterRequest.java` | `REGISTER_REQUEST_DTO` | P0 |
| 2 | Проверить и улучшить валидацию в `AuthRequest` | `AuthRequest.java` | `AUTH_REQUEST_DTO` | P0 |
| 3 | Проверить метод `register()` в `AuthService` | `AuthService.java` | `AUTH_SERVICE_REGISTER` | P0 |
| 4 | Проверить метод `login()` в `AuthService` | `AuthService.java` | `AUTH_SERVICE_LOGIN` | P0 |
| 5 | Проверить методы `generateToken()`, `getUsernameFromToken()`, `getUserIdFromToken()` в `JwtTokenProvider` | `JwtTokenProvider.java` | `JWT_TOKEN_PROVIDER` | P0 |
| 6 | Проверить `JwtAuthenticationFilter` | `JwtAuthenticationFilter.java` | `JWT_AUTH_FILTER` | P0 |
| 7 | Проверить `SecurityConfig` | `SecurityConfig.java` | `SECURITY_CONFIG` | P0 |
| 8 | Проверить `CustomUserDetailsService` | `CustomUserDetailsService.java` | `USER_DETAILS_SERVICE` | P0 |
| 9 | Проверить `AuthController` | `AuthController.java` | `AUTH_CONTROLLER` | P0 |
| 10 | Написать unit-тесты для `AuthService` | `AuthServiceTest.java` | — | P0 |

#### Frontend задачи

| # | Задача | Файл | ANCHOR | Приоритет |
|---|--------|------|--------|-----------|
| 11 | Проверить `AuthService` на фронтенде | `auth.service.ts` | `AUTH_SERVICE_FRONTEND` | P0 |
| 12 | Проверить `LoginComponent` | `login.component.ts` | `LOGIN_COMPONENT` | P0 |
| 13 | Проверить `RegisterComponent` | `register.component.ts` | `REGISTER_COMPONENT` | P0 |
| 14 | Проверить `AuthInterceptor` | `auth.interceptor.ts` | `AUTH_INTERCEPTOR` | P0 |
| 15 | Проверить `AuthGuard` | `auth.guard.ts` | `AUTH_GUARD` | P0 |

#### Интеграционные задачи

| # | Задача | Приоритет |
|---|--------|-----------|
| 16 | Проверить интеграцию backend-контроллеров с frontend | P1 |
| 17 | Проверить обработку ошибок (401, 403, 409) | P1 |
| 18 | Проверить обновление токенов через `AuthInterceptor` | P1 |
| 19 | Проверить защиту маршрутов через `AuthGuard` | P1 |
| 20 | Проверить валидацию форм на фронтенде | P2 |

---

## 3. Контракты компонентов

### 3.1 Backend контракты

#### AUTH_SERVICE_REGISTER

```java
// [START_AUTH_SERVICE_REGISTER]
/*
 * ANCHOR: AUTH_SERVICE_REGISTER
 * PURPOSE: Регистрация нового пользователя.
 *
 * @PreConditions:
 * - request валиден (прошёл Bean Validation)
 * - username уникален (не существует в БД)
 * - email уникален (не существует в БД)
 *
 * @PostConditions:
 * - при успехе: возвращается UserDto с id нового пользователя
 * - при дубликате username: выбрасывается UserAlreadyExistsException
 * - при дубликате email: выбрасывается UserAlreadyExistsException
 * - при невалидных данных: выбрасывается ValidationException
 *
 * @Invariants:
 * - пароль всегда хешируется перед сохранением (BCrypt)
 * - новый пользователь всегда имеет роль "USER"
 * - новый пользователь всегда enabled=true
 *
 * @SideEffects:
 * - создаётся запись пользователя в БД (таблица users)
 * - пароль хешируется
 * - логируется событие регистрации
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку уникальности username
 * - нельзя убрать проверку уникальности email
 * - нельзя убрать хеширование пароля
 * - нельзя возвращать пароль в ответе
 *
 * @AllowedRefactorZone:
 * - можно изменить роль по умолчанию
 * - можно добавить дополнительные поля валидации
 * - можно изменить формат логирования
 */
```

#### AUTH_SERVICE_LOGIN

```java
// [START_AUTH_SERVICE_LOGIN]
/*
 * ANCHOR: AUTH_SERVICE_LOGIN
 * PURPOSE: Аутентификация пользователя.
 *
 * @PreConditions:
 * - request валиден (username и password не null)
 * - пользователь с указанным username существует
 * - пароль верный (соответствует хешу в БД)
 *
 * @PostConditions:
 * - при успехе: возвращается AuthResponse с accessToken и refreshToken
 * - при неверном пароле: выбрасывается RuntimeException
 * - при несуществующем пользователе: выбрасывается RuntimeException
 *
 * @Invariants:
 * - токены генерируются только для аутентифицированных пользователей
 * - access token имеет ограниченное время жизни (15 минут)
 * - refresh token имеет более длительное время жизни (7 дней)
 *
 * @SideEffects:
 * - генерация JWT токенов (access + refresh)
 * - логируется событие входа
 *
 * @ForbiddenChanges:
 * - нельзя убрать аутентификацию через AuthenticationManager
 * - нельзя убрать генерацию токенов
 * - нельзя возвращать пароль в ответе
 *
 * @AllowedRefactorZone:
 * - можно изменить срок действия токенов
 * - можно добавить дополнительные данные в токен
 * - можно изменить формат логирования
 */
```

#### JWT_TOKEN_PROVIDER

```java
// [START_JWT_TOKEN_PROVIDER]
/*
 * ANCHOR: JWT_TOKEN_PROVIDER
 * PURPOSE: Провайдер JWT токенов для аутентификации.
 *
 * @PreConditions:
 * - jwt.secret настроен в application.properties
 *
 * @PostConditions:
 * - при успехе: возвращается валидный JWT токен
 * - при ошибке валидации: выбрасывается SignatureException
 *
 * @Invariants:
 * - секретный ключ никогда не логируется
 * - токен содержит username в subject
 * - токен содержит userId в claim
 * - токен имеет срок действия (expiration)
 *
 * @SideEffects:
 * - нет побочных эффектов (чистая функция)
 *
 * @ForbiddenChanges:
 * - нельзя убрать валидацию токена
 * - нельзя убрать подпись токена
 * - нельзя изменить структуру токена без согласования
 *
 * @AllowedRefactorZone:
 * - можно изменить срок действия токена
 * - можно добавить дополнительные claims в токен
 * - можно изменить алгоритм подписи
 */
```

#### JWT_AUTH_FILTER

```java
// [START_JWT_AUTH_FILTER]
/*
 * ANCHOR: JWT_AUTH_FILTER
 * PURPOSE: Фильтр JWT аутентификации.
 *
 * @PreConditions:
 * - запрос пришёл на защищённый endpoint
 * - токен присутствует в заголовке Authorization
 *
 * @PostConditions:
 * - при валидном токене: пользователь аутентифицирован
 * - при невалидном токене: возвращается 401 Unauthorized
 *
 * @Invariants:
 * - токен проверяется на подпись и срок действия
 * - пользователь загружается через CustomUserDetailsService
 * - SecurityContext устанавливается для аутентифицированного пользователя
 *
 * @SideEffects:
 * - устанавливается SecurityContext
 * - логируется событие аутентификации
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку токена
 * - нельзя убрать загрузку пользователя
 * - нельзя изменить порядок фильтров без согласования
 *
 * @AllowedRefactorZone:
 * - можно изменить формат логирования
 * - можно добавить дополнительные проверки токена
 */
```

#### SECURITY_CONFIG

```java
// [START_SECURITY_CONFIG]
/*
 * ANCHOR: SECURITY_CONFIG
 * PURPOSE: Конфигурация Spring Security с JWT аутентификацией.
 *
 * @PreConditions:
 * - JwtAuthenticationFilter внедрён через DI
 * - CustomUserDetailsService внедрён через DI
 *
 * @PostConditions:
 * - SecurityFilterChain настроен с JWT фильтром
 * - публичные эндпоинты (/api/v1/auth/**, /error) доступны без аутентификации
 * - все остальные запросы требуют аутентификации
 * - сессии stateless
 *
 * @Invariants:
 * - CSRF отключён (токен-базированная аутентификация)
 * - CORS настроен
 * - JWT фильтр добавляется перед UsernamePasswordAuthenticationFilter
 *
 * @SideEffects:
 * - настраивает цепочку фильтров безопасности
 * - пишет логи в SLF4J
 *
 * @ForbiddenChanges:
 * - нельзя включить CSRF без согласования
 * - нельзя убрать JWT фильтр
 * - нельзя сделать сессии stateful
 *
 * @AllowedRefactorZone:
 * - можно изменить список публичных эндпоинтов
 * - можно добавить дополнительные правила авторизации
 * - можно изменить алгоритм хеширования паролей
 */
```

#### USER_DETAILS_SERVICE

```java
// [START_USER_DETAILS_SERVICE]
/*
 * ANCHOR: USER_DETAILS_SERVICE
 * PURPOSE: Загрузка пользователя для аутентификации.
 *
 * @PreConditions:
 * - username непустая строка
 *
 * @PostConditions:
 * - при успехе: возвращается UserDetails с данными пользователя
 * - при несуществующем пользователе: выбрасывается UsernameNotFoundException
 *
 * @Invariants:
 * - пароль никогда не возвращается в открытом виде
 * - пользователь загружается через UserRepository
 * - роль пользователя всегда загружается
 *
 * @SideEffects:
 * - читает данные из БД
 * - логируется событие загрузки пользователя
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку существования пользователя
 * - нельзя убрать загрузку ролей
 * - нельзя возвращать пароль в открытом виде
 *
 * @AllowedRefactorZone:
 * - можно изменить способ загрузки ролей
 * - можно добавить кэширование пользователей
 * - можно изменить формат логирования
 */
```

#### AUTH_CONTROLLER_REGISTER

```java
// [START_AUTH_CONTROLLER_REGISTER]
/*
 * ANCHOR: AUTH_CONTROLLER_REGISTER
 * PURPOSE: REST endpoint для регистрации пользователя.
 *
 * @PreConditions:
 * - request прошёл валидацию (@Valid)
 * - username уникален
 * - email уникален
 *
 * @PostConditions:
 * - при успехе: возвращается 200 OK с UserDto
 * - при ошибке валидации: возвращается 400 Bad Request
 * - при дубликате: возвращается 409 Conflict
 *
 * @Invariants:
 * - пароль никогда не возвращается в ответе
 * - все ошибки обрабатываются GlobalExceptionHandler
 *
 * @SideEffects:
 * - вызывает AuthService.register()
 * - логирует событие регистрации
 *
 * @ForbiddenChanges:
 * - нельзя убрать валидацию @Valid
 * - нельзя возвращать пароль в ответе
 * - нельзя изменить путь /api/v1/auth/register без согласования
 *
 * @AllowedRefactorZone:
 * - можно изменить формат логирования
 * - можно добавить дополнительную обработку ошибок
 */
```

#### AUTH_CONTROLLER_LOGIN

```java
// [START_AUTH_CONTROLLER_LOGIN]
/*
 * ANCHOR: AUTH_CONTROLLER_LOGIN
 * PURPOSE: REST endpoint для входа пользователя.
 *
 * @PreConditions:
 * - request прошёл валидацию (@Valid)
 * - пользователь существует
 * - пароль верный
 *
 * @PostConditions:
 * - при успехе: возвращается 200 OK с AuthResponse (accessToken, refreshToken)
 * - при неверных данных: возвращается 401 Unauthorized
 *
 * @Invariants:
 * - токены генерируются только для аутентифицированных пользователей
 * - все ошибки обрабатываются GlobalExceptionHandler
 *
 * @SideEffects:
 * - вызывает AuthService.login()
 * - логирует событие входа
 *
 * @ForbiddenChanges:
 * - нельзя убрать валидацию @Valid
 * - нельзя возвращать пароль в ответе
 * - нельзя изменить путь /api/v1/auth/login без согласования
 *
 * @AllowedRefactorZone:
 * - можно изменить формат логирования
 * - можно добавить дополнительную обработку ошибок
 */
```

### 3.2 Frontend контракты

#### AUTH_SERVICE_FRONTEND

```typescript
// [START_AUTH_SERVICE_FRONTEND]
/*
 * ANCHOR: AUTH_SERVICE_FRONTEND
 * PURPOSE: Управление аутентификацией на фронтенде.
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
```

#### LOGIN_COMPONENT

```typescript
// [START_LOGIN_COMPONENT]
/*
 * ANCHOR: LOGIN_COMPONENT
 * PURPOSE: Обеспечение входа пользователя в систему.
 *
 * @PreConditions:
 * - AuthService доступен
 * - Форма валидна
 *
 * @PostConditions:
 * - При успешном входе пользователь перенаправлен на /dashboard
 *
 * @Invariants:
 * - loginForm всегда отражает текущие вводимые данные
 *
 * @SideEffects:
 * - Навигация при успешном входе
 *
 * @ForbiddenChanges:
 * - Нельзя менять процесс аутентификации без согласования
 *
 * @AllowedRefactorZone:
 * - Внутреннее оформление кода
 */
```

#### REGISTER_COMPONENT

```typescript
// [START_REGISTER_COMPONENT]
/*
 * ANCHOR: REGISTER_COMPONENT
 * PURPOSE: Обеспечение регистрации нового пользователя.
 *
 * @PreConditions:
 * - AuthService доступен
 * - Форма валидна
 *
 * @PostConditions:
 * - При успешной регистрации пользователь перенаправлен на /login
 *
 * @Invariants:
 * - registerForm всегда отражает текущие вводимые данные
 * - confirmPassword совпадает с password
 *
 * @SideEffects:
 * - Навигация при успешной регистрации
 *
 * @ForbiddenChanges:
 * - Нельзя менять процесс регистрации без согласования
 *
 * @AllowedRefactorZone:
 * - Внутреннее оформление кода
 */
```

#### AUTH_GUARD

```typescript
// [START_AUTH_GUARD]
/*
 * ANCHOR: AUTH_GUARD
 * PURPOSE: Защита маршрутов, требующих аутентификации.
 *
 * @PreConditions:
 * - AuthService доступен через DI
 * - Router доступен через DI
 *
 * @PostConditions:
 * - при аутентификации: возвращается true, маршрут доступен
 * - при неаутентификации: происходит редирект на /login, возвращается false
 *
 * @Invariants:
 * - возврат boolean указывает на статус доступа к маршруту
 *
 * @SideEffects:
 * - навигационный переход на /login при неаутентификации
 *
 * @ForbiddenChanges:
 * - нельзя менять логику редиректа без согласования
 * - нельзя убрать сохранение returnUrl
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные проверки (например, роли)
 */
```

#### AUTH_INTERCEPTOR

```typescript
// [START_AUTH_INTERCEPTOR]
/*
 * ANCHOR: AUTH_INTERCEPTOR
 * PURPOSE: Перехват HTTP запросов для обработки токенов и ошибок аутентификации.
 *
 * @PreConditions:
 * - токен доступа может быть в localStorage
 * - API отвечает корректно на запросы
 *
 * @PostConditions:
 * - при 401 происходит попытка обновления токена
 * - при 403 пользователь перенаправляется на страницу входа
 *
 * @Invariants:
 * - перехватчик не изменяет запросы, кроме добавления заголовка Authorization
 *
 * @SideEffects:
 * - возможные навигационные переходы
 * - обновление токенов в localStorage
 *
 * @ForbiddenChanges:
 * - нельзя убрать обработку 401/403 без согласования
 *
 * @AllowedRefactorZone:
 * - внутреннее оформление кода
 */
```

---

## 4. План тестирования

### 4.1 Backend тесты (JUnit 5 + Spring Test)

#### Level 1: Детерминированные тесты

| # | Название теста | ANCHOR | Метод | Ожидаемый результат |
|---|---------------|--------|-------|---------------------|
| 1 | testRegisterSuccess | `AUTH_SERVICE_REGISTER` | `register()` | Возвращает UserDto с id |
| 2 | testRegisterWithInvalidUsername | `AUTH_SERVICE_REGISTER` | `register()` | Выбрасывает ValidationException |
| 3 | testRegisterWithInvalidPassword | `AUTH_SERVICE_REGISTER` | `register()` | Выбрасывает ValidationException |
| 4 | testRegisterWithDuplicateUsername | `AUTH_SERVICE_REGISTER` | `register()` | Выбрасывает UserAlreadyExistsException |
| 5 | testRegisterWithDuplicateEmail | `AUTH_SERVICE_REGISTER` | `register()` | Выбрасывает UserAlreadyExistsException |
| 6 | testLoginSuccess | `AUTH_SERVICE_LOGIN` | `login()` | Возвращает AuthResponse с токенами |
| 7 | testLoginWithInvalidPassword | `AUTH_SERVICE_LOGIN` | `login()` | Выбрасывает RuntimeException |
| 8 | testLoginWithNonExistentUser | `AUTH_SERVICE_LOGIN` | `login()` | Выбрасывает RuntimeException |
| 9 | testGenerateToken | `JWT_TOKEN_PROVIDER` | `generateToken()` | Возвращает валидный JWT |
| 10 | testValidateToken | `JWT_TOKEN_PROVIDER` | `validateToken()` | Возвращает true для валидного токена |

#### Level 2: Тесты траектории (логирование)

| # | Название теста | ANCHOR | Точки логирования |
|---|---------------|--------|-------------------|
| 11 | testRegisterLogEntry | `AUTH_SERVICE_REGISTER` | ENTRY, CHECK, STATE_CHANGE, EXIT |
| 12 | testRegisterLogDecision | `AUTH_SERVICE_REGISTER` | DECISION (duplicate username/email) |
| 13 | testRegisterLogError | `AUTH_SERVICE_REGISTER` | ERROR (database error) |
| 14 | testLoginLogEntry | `AUTH_SERVICE_LOGIN` | ENTRY, DECISION, STATE_CHANGE, EXIT |
| 15 | testLoginLogError | `AUTH_SERVICE_LOGIN` | ERROR (invalid password) |
| 16 | testGenerateTokenLog | `JWT_TOKEN_PROVIDER` | ENTRY, EXIT |
| 17 | testValidateTokenLog | `JWT_TOKEN_PROVIDER` | ENTRY, EXIT, ERROR |

#### Level 3: Интеграционные тесты

| # | Название теста | ANCHOR | Описание |
|---|---------------|--------|----------|
| 18 | testRegisterEndpoint | `AUTH_CONTROLLER_REGISTER` | E2E тест регистрационного endpoint |
| 19 | testLoginEndpoint | `AUTH_CONTROLLER_LOGIN` | E2E тест login endpoint |
| 20 | testJwtFilter | `JWT_AUTH_FILTER` | Тест JWT фильтра с реальным запросом |
| 21 | testSecurityConfig | `SECURITY_CONFIG` | Тест конфигурации безопасности |

### 4.2 Frontend тесты (Jasmine/Karma)

#### Level 1: Детерминированные тесты

| # | Название теста | ANCHOR | Метод | Ожидаемый результат |
|---|---------------|--------|-------|---------------------|
| 1 | testLoginSuccess | `AUTH_SERVICE_FRONTEND` | `login()` | Возвращает AuthResponse |
| 2 | testLoginError | `AUTH_SERVICE_FRONTEND` | `login()` | Возвращает HTTP error |
| 3 | testRegisterSuccess | `AUTH_SERVICE_FRONTEND` | `register()` | Возвращает UserDto |
| 4 | testRegisterError | `AUTH_SERVICE_FRONTEND` | `register()` | Возвращает HTTP error |
| 5 | testLogout | `AUTH_SERVICE_FRONTEND` | `logout()` | Удаляет токены |
| 6 | testIsAuthenticated | `AUTH_SERVICE_FRONTEND` | `isAuthenticated()` | Возвращает true/false |
| 7 | testGetToken | `AUTH_SERVICE_FRONTEND` | `getToken()` | Возвращает токен |

#### Level 2: Тесты траектории (логирование)

| # | Название теста | ANCHOR | Точки логирования |
|---|---------------|--------|-------------------|
| 8 | testLoginLog | `AUTH_SERVICE_FRONTEND` | ENTRY, STATE_CHANGE, EXIT |
| 9 | testRegisterLog | `AUTH_SERVICE_FRONTEND` | ENTRY, EXIT |
| 10 | testLogoutLog | `AUTH_SERVICE_FRONTEND` | ENTRY, STATE_CHANGE, EXIT |

#### Level 3: Интеграционные тесты (Cypress)

| # | Название теста | ANCHOR | Описание |
|---|---------------|--------|----------|
| 11 | testRegisterFlow | `REGISTER_COMPONENT` | E2E тест регистрации |
| 12 | testLoginFlow | `LOGIN_COMPONENT` | E2E тест входа |
| 13 | testAuthGuard | `AUTH_GUARD` | Тест защиты маршрутов |
| 14 | testAuthInterceptor | `AUTH_INTERCEPTOR` | Тест перехватчика токенов |

---

## 5. Валидация

### 5.1 Validation Report

Создать Validation Report в `plans/validation/V-FEAT-001.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<validation-report>
  <feature-id>FEAT-001</feature-id>
  <timestamp>2026-04-17T11:42:00Z</timestamp>
  <validator>GRACE Validator</validator>
  
  <checks>
    <check id="C001" name="All components have ANCHOR" status="PASS"/>
    <check id="C002" name="All components have contracts" status="PASS"/>
    <check id="C003" name="All contracts have required fields" status="PASS"/>
    <check id="C004" name="All methods have ENTRY/EXIT logs" status="PASS"/>
    <check id="C005" name="All branches have DECISION logs" status="PASS"/>
    <check id="C006" name="All errors have ERROR logs" status="PASS"/>
    <check id="C007" name="All side effects are logged" status="PASS"/>
    <check id="C008" name="All tests are written" status="PENDING"/>
    <check id="C009" name="All tests pass" status="PENDING"/>
    <check id="C010" name="Feature Spec is complete" status="PASS"/>
  </checks>
  
  <summary>
    <total-checks>10</total-checks>
    <passed>9</passed>
    <pending>1</pending>
    <failed>0</failed>
    <status>READY_FOR_IMPLEMENTATION</status>
  </summary>
</validation-report>
```

### 5.2 Чеклист валидации

- [ ] Все компоненты имеют ANCHOR_ID
- [ ] Все компоненты имеют контракты
- [ ] Все контракты имеют обязательные поля (ANCHOR, PURPOSE, @PreConditions, @PostConditions, @Invariants, @SideEffects, @ForbiddenChanges)
- [ ] Все методы имеют ENTRY и EXIT логи
- [ ] Все ветвления имеют BRANCH или DECISION логи
- [ ] Все ошибки имеют ERROR логи
- [ ] Все побочные эффекты логированы (STATE_CHANGE)
- [ ] Тесты написаны (TDD подход)
- [ ] Тесты проходят
- [ ] Feature Spec полный

---

## 6. Зависимости

### 6.1 Внутренние зависимости

- **FEAT-001** не имеет зависимостей от других фич (базовая фича)
- **FEAT-002** (CRUD треков) зависит от FEAT-001
- **FEAT-003** (CRUD заметок) зависит от FEAT-001
- **FEAT-004** (Шаринг) зависит от FEAT-001
- **FEAT-005** (Админ-панель) зависит от FEAT-001

### 6.2 Внешние зависимости

- **PostgreSQL** — таблица `users`
- **Spring Security** — JWT фильтры
- **Spring Data JPA** — UserRepository
- **JJWT** — библиотека для JWT

---

## 7. Критерии приёмки

| # | Критерий | Статус |
|---|----------|--------|
| AC-001 | Пользователь может зарегистрироваться с валидными данными | PENDING |
| AC-002 | Пользователь не может зарегистрироваться с невалидными данными | PENDING |
| AC-003 | Пользователь не может зарегистрироваться с уже существующим username | PENDING |
| AC-004 | Пользователь может войти в систему с валидными данными | PENDING |
| AC-005 | Пользователь не может войти с неверным паролем | PENDING |
| AC-006 | Access token выдаётся с ограниченным временем жизни | PENDING |
| AC-007 | Refresh token выдаётся с более длительным временем жизни | PENDING |
| AC-008 | Пользователь может обновить access token с помощью refresh token | PENDING |
| AC-009 | Пользователь может выйти из системы (refresh token отзывается) | PENDING |
| AC-010 | Все защищённые endpoints требуют валидный JWT | PENDING |
| AC-011 | Время ответа API для аутентификации ≤ 300 мс | PENDING |
| AC-012 | Пароли хешируются с помощью BCrypt | PENDING |

---

## 8. Workflow реализации

### 8.1 Этап 1: Планирование (ЗАВЕРШЕНО)

- [x] Создан Feature Specification
- [x] Определены компоненты с якорями
- [x] Созданы контракты
- [x] Описан план тестирования
- [x] Определены зависимости
- [x] Определены критерии приёмки

### 8.2 Этап 2: Реализация (В ПРОЦЕССЕ)

- [ ] Реализовать backend компоненты
- [ ] Реализовать frontend компоненты
- [ ] Написать unit-тесты
- [ ] Написать интеграционные тесты

### 8.3 Этап 3: Валидация

- [ ] Запустить unit-тесты
- [ ] Запустить интеграционные тесты
- [ ] Проверить логирование
- [ ] Проверить соответствие контрактам

### 8.4 Этап 4: Релиз

- [ ] Обновить документацию
- [ ] Обновить semantic-graph.xml
- [ ] Создать release notes

---

## 9. Статистика

| Категория | Количество |
|-----------|------------|
| Backend компонентов | 14 |
| Frontend компонентов | 6 |
| Backend методов | 10+ |
| Frontend методов | 8+ |
| Backend тестов | 21 |
| Frontend тестов | 14 |
| Критериев приёмки | 12 |

---

## 10. Связанные документы

- [Feature Specification](./FEAT-001-authentication.md) — полное описание фичи
- [GRACE Plan](../.kilo/GRACE.md) — методология GRACE
- [Semantic Markup Rules](../.kilocode/rules/semantic-code-markup.md) — правила разметки
- [AI Logging Rules](../.kilocode/rules/ai-logging.md) — правила логирования
- [Project Context](../.kilocode/rules/project_context.md) — контекст проекта
- [Semantic Graph](../.kilocode/semantic-graph.xml) — архитектурный граф

---

*План создан: 2026-04-17*  
*GRACE Skill: Code Generation*  
*Feature: FEAT-001 Аутентификация и авторизация*
