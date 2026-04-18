# Feature Specification: FEAT-005

## Status
planning

## Overview
Админ-панель. Управление пользователями, блокировка/разблокировка пользователей, изменение ролей пользователей.

## Requirements

### Functional Requirements
- FR-001: Администратор может получить список всех пользователей с пагинацией
- FR-002: Администратор может заблокировать пользователя
- FR-003: Администратор может разблокировать пользователя
- FR-004: Администратор может изменить роль пользователя (USER ↔ ADMIN)
- FR-005: Администратор может удалить пользователя (каскадное удаление треков и заметок)
- FR-006: Администратор может просматривать любой контент без явного VIEW-доступа

### Non-Functional Requirements
- NFR-001: Время ответа API для управления пользователями ≤ 300 мс для 95% запросов
- NFR-002: Система должна поддерживать до 100 одновременных пользователей
- NFR-003: Индексы для ускорения запросов по user_id, role
- NFR-004: Администратор не может быть заблокирован самим собой

## Architecture

### Backend Components
- AdminController (ANCHOR: ADMIN_CONTROLLER)
- AdminService (ANCHOR: ADMIN_SERVICE)
- UserRepository (ANCHOR: USER_REPOSITORY)
- User (ANCHOR: USER_MODEL)
- UserAdminDto (ANCHOR: USER_ADMIN_DTO)
- BlockUserRequest (ANCHOR: BLOCK_USER_REQUEST)
- ChangeRoleRequest (ANCHOR: CHANGE_ROLE_REQUEST)

### Frontend Components
- AdminPanelComponent (ANCHOR: ADMIN_PANEL_COMPONENT)
- AdminService (ANCHOR: ADMIN_SERVICE_FRONTEND)
- AdminGuard (ANCHOR: ADMIN_GUARD)
- UserModel (ANCHOR: USER_MODEL_FRONTEND)

### Data Flow
```
Frontend (AdminPanel) → AdminService → Backend (AdminController)
→ AdminService → UserRepository → PostgreSQL
→ UserAdminDto → AdminService → Frontend
```

## Contracts

### Backend Component 1 (ANCHOR: ADMIN_SERVICE)
```
[START_ADMIN_SERVICE]
/*
 * ANCHOR: ADMIN_SERVICE
 * PURPOSE: Управление пользователями (только для ADMIN)
 *
 * @PreConditions:
 * - пользователь аутентифицирован (имеет валидный JWT)
 * - пользователь имеет роль ADMIN
 *
 * @PostConditions:
 * - при успехе получения списка: возвращается Page<UserAdminDto>
 * - при успехе блокировки: пользователь заблокирован (enabled = false)
 * - при успехе разблокировки: пользователь разблокирован (enabled = true)
 * - при успехе изменения роли: роль пользователя изменена
 * - при успехе удаления: пользователь и все его треки/заметки удалены
 *
 * @Invariants:
 * - пользователь с ролью ADMIN имеет полный доступ ко всем данным
 * - пользователь не может быть заблокирован без причины
 * - пользователь не может изменить свою роль без ADMIN прав
 *
 * @SideEffects:
 * - обновляет запись пользователя в БД (enabled, role)
 * - удаляет запись пользователя и связанные данные из БД
 * - пишет лог операции в audit log
 *
 * @ForbiddenChanges:
 * - нельзя заблокировать пользователя без ADMIN прав
 * - нельзя изменить роль пользователя без ADMIN прав
 * - нельзя удалить пользователя без ADMIN прав
 * - нельзя изменить роль текущего пользователя без ADMIN прав
 *
 * @AllowedRefactorZone:
 * - можно изменить способ получения списка пользователей
 * - можно добавить кэширование списка пользователей
 * - можно изменить формат логирования
 */
public class AdminService {
    public Page<UserAdminDto> listUsers(Pageable pageable);
    public void blockUser(Long userId, Long adminId);
    public void unblockUser(Long userId, Long adminId);
    public UserAdminDto changeRole(Long userId, String role, Long adminId);
    public void deleteUser(Long userId, Long adminId);
}
// [END_ADMIN_SERVICE]
```

### Frontend Component 1 (ANCHOR: ADMIN_SERVICE_FRONTEND)
```
[START_ADMIN_SERVICE_FRONTEND]
/*
 * ANCHOR: ADMIN_SERVICE_FRONTEND
 * PURPOSE: Управление пользователями на фронтенде
 *
 * @PreConditions:
 * - HTTP клиент инициализирован
 * - AuthInterceptor настроен
 * - Пользователь имеет роль ADMIN
 *
 * @PostConditions:
 * - при успехе: данные пользователей сохранены в BehaviorSubject/Signal
 * - при ошибке: отображается сообщение об ошибке
 *
 * @Invariants:
 * - данные пользователей никогда не логируются в открытом виде
 * - список пользователей обновляется автоматически при изменении
 *
 * @SideEffects:
 * - сохраняет данные пользователей в BehaviorSubject/Signal
 * - устанавливает заголовок Authorization для последующих запросов
 *
 * @ForbiddenChanges:
 * - нельзя удалять данные пользователей при ошибке без перенаправления на login
 * - нельзя хранить данные пользователей в небезопасных местах
 *
 * @AllowedRefactorZone:
 * - можно изменить способ хранения данных (BehaviorSubject → Signals)
 * - можно добавить кэширование данных пользователей
 */
export class AdminService {
    listUsers(page: number, size: number): Observable<Page<UserAdminDto>>
    blockUser(userId: number): Observable<void>
    unblockUser(userId: number): Observable<void>
    changeRole(userId: number, role: 'USER' | 'ADMIN'): Observable<UserAdminDto>
    deleteUser(userId: number): Observable<void>
    getUsers(): BehaviorSubject<UserAdminDto[]>
}
// [END_ADMIN_SERVICE_FRONTEND]
```

## API

### Endpoints
- GET /api/v1/admin/users — Список всех пользователей (пагинация)
- PATCH /api/v1/admin/users/{id}/lock — Блокировка/разблокировка пользователя
- PATCH /api/v1/admin/users/{id}/role — Изменение роли пользователя
- DELETE /api/v1/admin/users/{id} — Удаление пользователя (каскадное)

### DTO
- UserAdminDto — Данные пользователя для админа (id, username, role, enabled, createdAt)
- BlockUserRequest — Запрос блокировки (enabled: boolean)
- ChangeRoleRequest — Запрос изменения роли (role: 'USER' | 'ADMIN')

## UI

### Components
- AdminPanelComponent — Таблица пользователей с фильтрацией, пагинацией, кнопками блокировки/удаления/изменения роли

### Services
- AdminService — HTTP запросы к API управления пользователями
- AdminGuard — Guard для защиты admin-маршрутов

## Test Plan

### Backend Tests (JUnit 5 + Spring Test)

#### Level 1: Детерминированные тесты
- [ ] Тест 1: Проверка получения списка пользователей
- [ ] Тест 2: Проверка блокировки пользователя
- [ ] Тест 3: Проверка разблокировки пользователя
- [ ] Тест 4: Проверка изменения роли пользователя
- [ ] Тест 5: Проверка удаления пользователя
- [ ] Тест 6: Проверка ADMIN прав доступа

#### Level 2: Тесты траектории
- [ ] Тест 1: Проверка ENTRY/EXIT логов в blockUser()
- [ ] Тест 2: Проверка STATE_CHANGE логов при блокировке

#### Level 3: Интеграционные тесты
- [ ] Тест 1: E2E сценарий блокировки пользователя
- [ ] Тест 2: Интеграция с PostgreSQL
- [ ] Тест 3: Тестирование REST API (MockMvc)

### Frontend Tests (Jasmine/Karma)

#### Level 1: Детерминированные тесты
- [ ] Тест 1: Проверка отображения списка пользователей
- [ ] Тест 2: Проверка блокировки пользователя
- [ ] Тест 3: Проверка разблокировки пользователя
- [ ] Тест 4: Проверка изменения роли
- [ ] Тест 5: Проверка удаления пользователя

#### Level 2: Тесты траектории
- [ ] Тест 1: Проверка ENTRY/EXIT логов в AdminPanelComponent

#### Level 3: Интеграционные тесты
- [ ] Тест 1: E2E сценарий блокировки пользователя (Cypress)
- [ ] Тест 2: Интеграция с backend API

## Dependencies
- FEAT-001 (Аутентификация) — требуется валидный JWT с ролью ADMIN
- Зависит от: PostgreSQL (таблица users)
- Зависит от: Spring Data JPA (UserRepository)

## Acceptance Criteria
- [ ] AC-001: Администратор может получить список всех пользователей
- [ ] AC-002: Администратор может заблокировать пользователя
- [ ] AC-003: Администратор может разблокировать пользователя
- [ ] AC-004: Администратор может изменить роль пользователя (USER ↔ ADMIN)
- [ ] AC-005: Администратор может удалить пользователя (каскадное удаление)
- [ ] AC-006: Администратор может просматривать любой контент без явного VIEW-доступа
- [ ] AC-007: Время ответа API для управления пользователями ≤ 300 мс
- [ ] AC-008: Администратор не может быть заблокирован самим собой
