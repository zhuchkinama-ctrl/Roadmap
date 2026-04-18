# План покрытия тестами FEAT-001: Аутентификация и авторизация

## Обзор

Этот документ описывает план покрытия тестами для фичи FEAT-001 в соответствии с методологией GRACE (3 уровня тестирования).

**Дата создания**: 2026-04-18  
**Статус**: В разработке  
**Фича**: FEAT-001-authentication.md

---

## Уровни тестирования GRACE

### Level 1: Детерминированные тесты (Unit Tests)
**Назначение**: Проверка постусловий контрактов  
**Что проверяем**: Возвращаемые значения, состояние объектов, исключения, побочные эффекты

### Level 2: Тесты траектории (Trajectory Tests)
**Назначение**: Проверка log-маркеров и траектории выполнения  
**Что проверяем**: ENTRY, EXIT, BRANCH, DECISION, ERROR, STATE_CHANGE логи

### Level 3: Интеграционные тесты (Integration Tests)
**Назначение**: Проверка E2E сценариев и интеграции компонентов  
**Что проверяем**: Сквозной поток данных, интеграцию между компонентами, API эндпоинты, UI сценарии

---

## Backend: Существующие тесты

### 1. AuthControllerTest.java
**Тип**: Level 1 (Unit Tests)  
**Покрытие**: REST-эндпоинты AuthController

| Тест | Сценарий | Статус | Контракт |
|------|----------|--------|----------|
| `register_Success` | Успешная регистрация | ✅ | AUTH_CONTROLLER |
| `register_ValidationError` | Ошибка валидации при регистрации | ✅ | AUTH_CONTROLLER |
| `register_ServiceError` | Ошибка сервиса при регистрации | ✅ | AUTH_CONTROLLER |
| `login_Success` | Успешный вход | ✅ | AUTH_CONTROLLER |
| `login_ValidationError` | Ошибка валидации при входе | ✅ | AUTH_CONTROLLER |
| `login_ServiceError` | Ошибка сервиса при входе | ✅ | AUTH_CONTROLLER |
| `refresh_Success` | Успешное обновление токена | ✅ | AUTH_CONTROLLER |
| `refresh_ValidationError` | Ошибка валидации при обновлении | ✅ | AUTH_CONTROLLER |
| `logout_Success` | Успешный выход | ✅ | AUTH_CONTROLLER |

**Покрытие контракта AUTH_CONTROLLER**: 100%

---

### 2. AuthServiceTest.java
**Тип**: Level 1 (Unit Tests)  
**Покрытие**: Бизнес-логика AuthService

| Тест | Сценарий | Статус | Контракт |
|------|----------|--------|----------|
| `register_Success` | Успешная регистрация | ✅ | AUTH_SERVICE |
| `register_UserAlreadyExists` | Пользователь уже существует | ✅ | AUTH_SERVICE |
| `register_ValidationError` | Ошибка валидации | ✅ | AUTH_SERVICE |
| `login_Success` | Успешный вход | ✅ | AUTH_SERVICE |
| `login_InvalidCredentials` | Неверные учетные данные | ✅ | AUTH_SERVICE |
| `login_UserNotFound` | Пользователь не найден | ✅ | AUTH_SERVICE |
| `login_UserDisabled` | Пользователь отключен | ✅ | AUTH_SERVICE |
| `refresh_Success` | Успешное обновление токена | ✅ | AUTH_SERVICE |
| `refresh_InvalidToken` | Неверный токен | ✅ | AUTH_SERVICE |
| `logout_Success` | Успешный выход | ✅ | AUTH_SERVICE |

**Покрытие контракта AUTH_SERVICE**: 100%

---

### 3. JwtTokenProviderTest.java
**Тип**: Level 1 (Unit Tests)  
**Покрытие**: Генерация и валидация JWT токенов

| Тест | Сценарий | Статус | Контракт |
|------|----------|--------|----------|
| `generateAccessToken_Success` | Генерация access token | ✅ | JWT_TOKEN_PROVIDER |
| `generateRefreshToken_Success` | Генерация refresh token | ✅ | JWT_TOKEN_PROVIDER |
| `validateToken_Success` | Валидация валидного токена | ✅ | JWT_TOKEN_PROVIDER |
| `validateToken_Invalid` | Валидация невалидного токена | ✅ | JWT_TOKEN_PROVIDER |
| `validateToken_Expired` | Валидация истекшего токена | ✅ | JWT_TOKEN_PROVIDER |
| `getUsernameFromToken_Success` | Получение username из токена | ✅ | JWT_TOKEN_PROVIDER |
| `isTokenExpired_Success` | Проверка истечения токена | ✅ | JWT_TOKEN_PROVIDER |

**Покрытие контракта JWT_TOKEN_PROVIDER**: 100%

---

### 4. JwtTokenProviderTrajectoryTest.java
**Тип**: Level 2 (Trajectory Tests)  
**Покрытие**: Log-маркеры JwtTokenProvider

| Тест | Сценарий | Статус | Контракт |
|------|----------|--------|----------|
| `testGenerateAccessTokenMarkers` | Проверка ENTRY/EXIT логов генерации | ✅ | JWT_TOKEN_PROVIDER |
| `testValidateTokenMarkers` | Проверка ENTRY/EXIT логов валидации | ✅ | JWT_TOKEN_PROVIDER |
| `testValidateTokenErrorMarkers` | Проверка ERROR логов при ошибке | ✅ | JWT_TOKEN_PROVIDER |

**Покрытие логов JWT_TOKEN_PROVIDER**: 100%

---

### 5. AuthIntegrationTest.java
**Тип**: Level 3 (Integration Tests)  
**Покрытие**: E2E сценарии аутентификации

| Тест | Сценарий | Статус | Контракты |
|------|----------|--------|-----------|
| `testE2E_RegisterAndLogin` | Регистрация и вход | ✅ | AUTH_CONTROLLER, AUTH_SERVICE |
| `testE2E_LoginAndAccessProtectedEndpoint` | Вход и доступ к защищённому endpoint | ✅ | AUTH_CONTROLLER, JWT_AUTH_FILTER |
| `testE2E_RefreshToken` | Обновление токена | ✅ | AUTH_CONTROLLER, AUTH_SERVICE |
| `testE2E_Logout` | Выход из системы | ✅ | AUTH_CONTROLLER, AUTH_SERVICE |

**Покрытие E2E сценариев**: 100%

---

## Frontend: Существующие тесты

### 1. login.component.spec.ts
**Тип**: Level 1 (Unit Tests)  
**Покрытие**: LoginComponent

| Тест | Сценарий | Статус | Контракт |
|------|----------|--------|----------|
| `should create` | Компонент создаётся | ✅ | LOGIN_COMPONENT |
| `should initialize form` | Форма инициализируется | ✅ | LOGIN_COMPONENT |
| `should login successfully` | Успешный вход | ✅ | LOGIN_COMPONENT |
| `should show error on invalid credentials` | Ошибка при неверных данных | ✅ | LOGIN_COMPONENT |
| `should show validation error` | Ошибка валидации | ✅ | LOGIN_COMPONENT |
| `should redirect to dashboard on success` | Редирект на dashboard | ✅ | LOGIN_COMPONENT |

**Покрытие контракта LOGIN_COMPONENT**: 100%

---

### 2. register.component.spec.ts
**Тип**: Level 1 (Unit Tests)  
**Покрытие**: RegisterComponent

| Тест | Сценарий | Статус | Контракт |
|------|----------|--------|----------|
| `should create` | Компонент создаётся | ✅ | REGISTER_COMPONENT |
| `should initialize form` | Форма инициализируется | ✅ | REGISTER_COMPONENT |
| `should register successfully` | Успешная регистрация | ✅ | REGISTER_COMPONENT |
| `should show error on duplicate user` | Ошибка при дубликате | ✅ | REGISTER_COMPONENT |
| `should show validation error` | Ошибка валидации | ✅ | REGISTER_COMPONENT |
| `should redirect to login on success` | Редирект на login | ✅ | REGISTER_COMPONENT |

**Покрытие контракта REGISTER_COMPONENT**: 100%

---

### 3. auth.service.spec.ts
**Тип**: Level 1 (Unit Tests)  
**Покрытие**: AuthService (Frontend)

| Тест | Сценарий | Статус | Контракт |
|------|----------|--------|----------|
| `should login successfully` | Успешный вход | ✅ | AUTH_SERVICE_FRONTEND |
| `should handle login error` | Обработка ошибки входа | ✅ | AUTH_SERVICE_FRONTEND |
| `should register successfully` | Успешная регистрация | ✅ | AUTH_SERVICE_FRONTEND |
| `should handle register error` | Обработка ошибки регистрации | ✅ | AUTH_SERVICE_FRONTEND |
| `should refresh token successfully` | Успешное обновление токена | ✅ | AUTH_SERVICE_FRONTEND |
| `should handle refresh error` | Обработка ошибки обновления | ✅ | AUTH_SERVICE_FRONTEND |
| `should logout successfully` | Успешный выход | ✅ | AUTH_SERVICE_FRONTEND |
| `should handle logout error` | Обработка ошибки выхода | ✅ | AUTH_SERVICE_FRONTEND |

**Покрытие контракта AUTH_SERVICE_FRONTEND**: 100%

---

### 4. auth.guard.spec.ts
**Тип**: Level 1 (Unit Tests)  
**Покрытие**: AuthGuard

| Тест | Сценарий | Статус | Контракт |
|------|----------|--------|----------|
| `should allow access when authenticated` | Доступ при аутентификации | ✅ | AUTH_GUARD |
| `should deny access when not authenticated` | Отказ без аутентификации | ✅ | AUTH_GUARD |
| `should redirect to login when not authenticated` | Редирект на login | ✅ | AUTH_GUARD |

**Покрытие контракта AUTH_GUARD**: 100%

---

### 5. auth.interceptor.spec.ts
**Тип**: Level 1 (Unit Tests)  
**Покрытие**: AuthInterceptor

| Тест | Сценарий | Статус | Контракт |
|------|----------|--------|----------|
| `should add token to request` | Добавление токена к запросу | ✅ | AUTH_INTERCEPTOR |
| `should not add token to auth requests` | Не добавлять токен к auth запросам | ✅ | AUTH_INTERCEPTOR |
| `should handle 401 error` | Обработка 401 ошибки | ✅ | AUTH_INTERCEPTOR |
| `should handle 403 error` | Обработка 403 ошибки | ✅ | AUTH_INTERCEPTOR |

**Покрытие контракта AUTH_INTERCEPTOR**: 100%

---

## Отсутствующие тесты (Gap Analysis)

### Backend: Level 2 (Trajectory Tests)

#### AuthServiceTrajectoryTest.java
**Статус**: ❌ Не создан  
**Приоритет**: HIGH  
**Контракт**: AUTH_SERVICE

Необходимые тесты:
- [ ] `testRegisterMarkers` - Проверка ENTRY/EXIT логов регистрации
- [ ] `testRegisterErrorMarkers` - Проверка ERROR логов при ошибке регистрации
- [ ] `testLoginMarkers` - Проверка ENTRY/EXIT логов входа
- [ ] `testLoginErrorMarkers` - Проверка ERROR логов при ошибке входа
- [ ] `testRefreshMarkers` - Проверка ENTRY/EXIT логов обновления токена
- [ ] `testRefreshErrorMarkers` - Проверка ERROR логов при ошибке обновления
- [ ] `testLogoutMarkers` - Проверка ENTRY/EXIT логов выхода

---

#### AuthControllerTrajectoryTest.java
**Статус**: ❌ Не создан  
**Приоритет**: MEDIUM  
**Контракт**: AUTH_CONTROLLER

Необходимые тесты:
- [ ] `testRegisterMarkers` - Проверка ENTRY/EXIT логов регистрации
- [ ] `testLoginMarkers` - Проверка ENTRY/EXIT логов входа
- [ ] `testRefreshMarkers` - Проверка ENTRY/EXIT логов обновления
- [ ] `testLogoutMarkers` - Проверка ENTRY/EXIT логов выхода

---

#### JwtAuthenticationFilterTrajectoryTest.java
**Статус**: ❌ Не создан  
**Приоритет**: HIGH  
**Контракт**: JWT_AUTH_FILTER

Необходимые тесты:
- [ ] `testDoFilterInternalMarkers` - Проверка ENTRY/EXIT логов фильтра
- [ ] `testDoFilterInternalErrorMarkers` - Проверка ERROR логов при ошибке

---

### Frontend: Level 2 (Trajectory Tests)

#### AuthServiceTrajectoryTest.ts
**Статус**: ❌ Не создан  
**Приоритет**: HIGH  
**Контракт**: AUTH_SERVICE_FRONTEND

Необходимые тесты:
- [ ] `testLoginMarkers` - Проверка ENTRY/EXIT логов входа
- [ ] `testLoginErrorMarkers` - Проверка ERROR логов при ошибке
- [ ] `testRegisterMarkers` - Проверка ENTRY/EXIT логов регистрации
- [ ] `testRegisterErrorMarkers` - Проверка ERROR логов при ошибке
- [ ] `testRefreshMarkers` - Проверка ENTRY/EXIT логов обновления
- [ ] `testRefreshErrorMarkers` - Проверка ERROR логов при ошибке
- [ ] `testLogoutMarkers` - Проверка ENTRY/EXIT логов выхода

---

#### LoginComponentTrajectoryTest.ts
**Статус**: ❌ Не создан  
**Приоритет**: MEDIUM  
**Контракт**: LOGIN_COMPONENT

Необходимые тесты:
- [ ] `testLoginMarkers` - Проверка ENTRY/EXIT логов входа
- [ ] `testLoginErrorMarkers` - Проверка ERROR логов при ошибке

---

#### RegisterComponentTrajectoryTest.ts
**Статус**: ❌ Не создан  
**Приоритет**: MEDIUM  
**Контракт**: REGISTER_COMPONENT

Необходимые тесты:
- [ ] `testRegisterMarkers` - Проверка ENTRY/EXIT логов регистрации
- [ ] `testRegisterErrorMarkers` - Проверка ERROR логов при ошибке

---

### Frontend: Level 3 (E2E Tests)

#### E2E Authentication Tests (Cypress)
**Статус**: ❌ Не созданы  
**Приоритет**: HIGH  
**Контракты**: LOGIN_COMPONENT, REGISTER_COMPONENT, AUTH_SERVICE_FRONTEND

Необходимые тесты:
- [ ] `should login and access dashboard` - Вход и доступ к dashboard
- [ ] `should show error with invalid credentials` - Ошибка при неверных данных
- [ ] `should register and login` - Регистрация и вход
- [ ] `should show error on duplicate user` - Ошибка при дубликате
- [ ] `should logout and redirect to login` - Выход и редирект на login
- [ ] `should refresh token automatically` - Автоматическое обновление токена

---

## Статистика покрытия

### Backend

| Компонент | Level 1 | Level 2 | Level 3 | Итого |
|-----------|---------|---------|---------|-------|
| AuthController | ✅ 100% | ❌ 0% | ✅ 100% | 67% |
| AuthService | ✅ 100% | ❌ 0% | ✅ 100% | 67% |
| JwtTokenProvider | ✅ 100% | ✅ 100% | ✅ 100% | 100% |
| JwtAuthenticationFilter | ❌ 0% | ❌ 0% | ✅ 100% | 33% |
| **Итого Backend** | **75%** | **25%** | **100%** | **67%** |

### Frontend

| Компонент | Level 1 | Level 2 | Level 3 | Итого |
|-----------|---------|---------|---------|-------|
| LoginComponent | ✅ 100% | ❌ 0% | ❌ 0% | 33% |
| RegisterComponent | ✅ 100% | ❌ 0% | ❌ 0% | 33% |
| AuthService | ✅ 100% | ❌ 0% | ❌ 0% | 33% |
| AuthGuard | ✅ 100% | ❌ 0% | ❌ 0% | 33% |
| AuthInterceptor | ✅ 100% | ❌ 0% | ❌ 0% | 33% |
| **Итого Frontend** | **100%** | **0%** | **0%** | **33%** |

### Общее покрытие

| Уровень | Backend | Frontend | Итого |
|---------|---------|----------|-------|
| Level 1 (Unit) | 75% | 100% | 88% |
| Level 2 (Trajectory) | 25% | 0% | 13% |
| Level 3 (Integration/E2E) | 100% | 0% | 50% |
| **Общее** | **67%** | **33%** | **50%** |

---

## Рекомендации

### Приоритет 1 (Критично)

1. **Создать AuthServiceTrajectoryTest.java** - Проверка логов AuthService
2. **Создать JwtAuthenticationFilterTrajectoryTest.java** - Проверка логов фильтра
3. **Создать AuthServiceTrajectoryTest.ts** - Проверка логов AuthService (Frontend)
4. **Создать E2E тесты (Cypress)** - Проверка сквозных сценариев

### Приоритет 2 (Важно)

1. **Создать AuthControllerTrajectoryTest.java** - Проверка логов контроллера
2. **Создать LoginComponentTrajectoryTest.ts** - Проверка логов компонента входа
3. **Создать RegisterComponentTrajectoryTest.ts** - Проверка логов компонента регистрации

### Приоритет 3 (Желательно)

1. **Создать JwtAuthenticationFilterTest.java** - Unit тесты для фильтра
2. **Создать CustomUserDetailsServiceTest.java** - Unit тесты для UserDetailsService
3. **Создать SecurityConfigTest.java** - Unit тесты для конфигурации безопасности

---

## План реализации

### Неделя 1: Backend Level 2 Tests

- [ ] Создать `AuthServiceTrajectoryTest.java`
- [ ] Создать `AuthControllerTrajectoryTest.java`
- [ ] Создать `JwtAuthenticationFilterTrajectoryTest.java`

### Неделя 2: Frontend Level 2 Tests

- [ ] Создать `AuthServiceTrajectoryTest.ts`
- [ ] Создать `LoginComponentTrajectoryTest.ts`
- [ ] Создать `RegisterComponentTrajectoryTest.ts`

### Неделя 3: Frontend Level 3 Tests

- [ ] Настроить Cypress
- [ ] Создать E2E тесты для аутентификации
- [ ] Интегрировать в CI/CD

### Неделя 4: Дополнительные тесты

- [ ] Создать `JwtAuthenticationFilterTest.java`
- [ ] Создать `CustomUserDetailsServiceTest.java`
- [ ] Создать `SecurityConfigTest.java`

---

## Критерии завершения

- [ ] Все Level 1 тесты покрывают 100% контрактов
- [ ] Все Level 2 тесты покрывают 100% критичных функций
- [ ] Все Level 3 тесты покрывают основные E2E сценарии
- [ ] Покрытие кода ≥ 80%
- [ ] Все тесты проходят в CI/CD
- [ ] Нет flaky тестов

---

## Связанные документы

- [FEAT-001-authentication.md](../features/FEAT-001-authentication.md) - Спецификация фичи
- [GRACE.md](../.kilo/GRACE.md) - Методология GRACE
- [grace-test-generation/SKILL.md](../.kilo/skills/grace-test-generation/SKILL.md) - Скилл генерации тестов
- [unit_tests_plan.md](unit_tests_plan.md) - План юнит-тестов

---

*Дата последнего обновления: 2026-04-18*
