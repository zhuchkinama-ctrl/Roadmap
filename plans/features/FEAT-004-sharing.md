# Feature Specification: FEAT-004

## Status
planning

## Overview
Система шаринга треков. Предоставление доступа другим пользователям с разграничением прав (VIEW/EDIT) и управление правами доступа.

## Requirements

### Functional Requirements
- FR-001: Владелец трека может предоставить доступ другому пользователю (VIEW или EDIT)
- FR-002: Владелец трека может изменить тип доступа пользователя (VIEW ↔ EDIT)
- FR-003: Владелец трека может отозвать доступ у пользователя
- FR-004: Пользователь может получить список всех пользователей с доступом к треку
- FR-005: Пользователь с правом VIEW может просматривать трек и заметки
- FR-006: Пользователь с правом EDIT может просматривать и редактировать трек и заметки
- FR-007: Пользователь с правом OWNER может управлять треком и правами доступа
- FR-008: При удалении пользователя каскадно удаляются все его права доступа

### Non-Functional Requirements
- NFR-001: Время ответа API для управления правами доступа ≤ 300 мс для 95% запросов
- NFR-002: Система должна поддерживать до 100 одновременных пользователей
- NFR-003: Индексы для ускорения запросов по track_id, user_id
- NFR-004: Уникальный constraint (track_id, user_id) в таблице track_permissions

## Architecture

### Backend Components
- PermissionController (ANCHOR: PERMISSION_CONTROLLER)
- PermissionService (ANCHOR: PERMISSION_SERVICE)
- TrackPermissionRepository (ANCHOR: TRACK_PERMISSION_REPOSITORY)
- TrackPermission (ANCHOR: TRACK_PERMISSION_MODEL)
- PermissionDto (ANCHOR: PERMISSION_DTO)
- GrantPermissionRequest (ANCHOR: GRANT_PERMISSION_REQUEST)

### Frontend Components
- ShareDialogComponent (ANCHOR: SHARE_DIALOG_COMPONENT)
- PermissionService (ANCHOR: PERMISSION_SERVICE_FRONTEND)
- PermissionModel (ANCHOR: PERMISSION_MODEL_FRONTEND)

### Data Flow
```
Frontend (ShareDialog) → PermissionService → Backend (PermissionController)
→ PermissionService → TrackPermissionRepository → PostgreSQL
→ PermissionDto → PermissionService → Frontend
```

## Contracts

### Backend Component 1 (ANCHOR: PERMISSION_SERVICE)
```
[START_PERMISSION_SERVICE]
/*
 * ANCHOR: PERMISSION_SERVICE
 * PURPOSE: Управление правами доступа к трекам
 *
 * @PreConditions:
 * - пользователь аутентифицирован (имеет валидный JWT)
 * - трек существует
 * - пользователь имеет OWNER права на трек (для предоставления/изменения/удаления прав)
 *
 * @PostConditions:
 * - при успехе предоставления: возвращается PermissionDto с новым правом
 * - при успехе изменения: возвращается PermissionDto с обновлённым правом
 * - при успехе удаления: право доступа удалено
 * - при успехе получения: возвращается список всех прав доступа к треку
 *
 * @Invariants:
 * - право всегда принадлежит треку (track_id)
 * - право всегда принадлежит пользователю (user_id)
 * - permission_type может быть только VIEW или EDIT
 * - уникальный constraint (track_id, user_id)
 *
 * @SideEffects:
 * - создаёт запись права доступа в БД
 * - обновляет запись права доступа в БД
 * - удаляет запись права доступа из БД
 * - пишет лог операции в audit log
 *
 * @ForbiddenChanges:
 * - нельзя предоставить право без OWNER прав на трек
 * - нельзя изменить право без OWNER прав на трек
 * - нельзя удалить право без OWNER прав на трек
 * - нельзя нарушить уникальный constraint (track_id, user_id)
 *
 * @AllowedRefactorZone:
 * - можно изменить способ получения списка прав доступа
 * - можно добавить кэширование прав доступа
 * - можно изменить формат логирования
 */
public class PermissionService {
    public PermissionDto grantPermission(Long trackId, String username, String permissionType, Long ownerId);
    public PermissionDto updatePermission(Long trackId, Long userId, String permissionType, Long ownerId);
    public void revokePermission(Long trackId, Long userId, Long ownerId);
    public List<PermissionDto> listPermissions(Long trackId, Long userId);
}
// [END_PERMISSION_SERVICE]
```

### Frontend Component 1 (ANCHOR: PERMISSION_SERVICE_FRONTEND)
```
[START_PERMISSION_SERVICE_FRONTEND]
/*
 * ANCHOR: PERMISSION_SERVICE_FRONTEND
 * PURPOSE: Управление правами доступа на фронтенде
 *
 * @PreConditions:
 * - HTTP клиент инициализирован
 * - AuthInterceptor настроен
 *
 * @PostConditions:
 * - при успехе: данные прав доступа сохранены в BehaviorSubject/Signal
 * - при ошибке: отображается сообщение об ошибке
 *
 * @Invariants:
 * - данные прав доступа никогда не логируются в открытом виде
 * - список прав доступа обновляется автоматически при изменении
 *
 * @SideEffects:
 * - сохраняет данные прав доступа в BehaviorSubject/Signal
 * - устанавливает заголовок Authorization для последующих запросов
 *
 * @ForbiddenChanges:
 * - нельзя удалять данные прав доступа при ошибке без перенаправления на login
 * - нельзя хранить данные прав доступа в небезопасных местах
 *
 * @AllowedRefactorZone:
 * - можно изменить способ хранения данных (BehaviorSubject → Signals)
 * - можно добавить кэширование данных прав доступа
 */
export class PermissionService {
    grantPermission(trackId: number, username: string, permissionType: 'VIEW' | 'EDIT'): Observable<PermissionDto>
    updatePermission(trackId: number, userId: number, permissionType: 'VIEW' | 'EDIT'): Observable<PermissionDto>
    revokePermission(trackId: number, userId: number): Observable<void>
    listPermissions(trackId: number): Observable<PermissionDto[]>
    getPermissions(): BehaviorSubject<PermissionDto[]>
}
// [END_PERMISSION_SERVICE_FRONTEND]
```

## API

### Endpoints
- GET /api/v1/tracks/{id}/permissions — Список пользователей с доступом
- POST /api/v1/tracks/{id}/permissions — Выдать доступ (username, permissionType)
- PUT /api/v1/tracks/{id}/permissions/{userId} — Изменить тип доступа
- DELETE /api/v1/tracks/{id}/permissions/{userId} — Отозвать доступ

### DTO
- GrantPermissionRequest — Запрос предоставления доступа (username, permissionType)
- PermissionDto — Данные права доступа (id, user, permissionType, grantedAt)

## UI

### Components
- ShareDialogComponent — Диалог выбора пользователя и роли (VIEW/EDIT)

### Services
- PermissionService — HTTP запросы к API прав доступа

## Test Plan

### Backend Tests (JUnit 5 + Spring Test)

#### Level 1: Детерминированные тесты
- [ ] Тест 1: Проверка предоставления доступа VIEW
- [ ] Тест 2: Проверка предоставления доступа EDIT
- [ ] Тест 3: Проверка отзыва доступа
- [ ] Тест 4: Проверка получения списка разрешений
- [ ] Тест 5: Проверка прав доступа (VIEW/EDIT)
- [ ] Тест 6: Проверка уникального constraint (track_id, user_id)

#### Level 2: Тесты траектории
- [ ] Тест 1: Проверка ENTRY/EXIT логов в grantPermission()
- [ ] Тест 2: Проверка DECISION логов при проверке прав

#### Level 3: Интеграционные тесты
- [ ] Тест 1: E2E сценарий шаринга трека
- [ ] Тест 2: Интеграция с PostgreSQL
- [ ] Тест 3: Тестирование REST API (MockMvc)

### Frontend Tests (Jasmine/Karma)

#### Level 1: Детерминированные тесты
- [ ] Тест 1: Проверка отображения диалога шаринга
- [ ] Тест 2: Проверка выбора пользователя
- [ ] Тест 3: Проверка выбора роли (VIEW/EDIT)
- [ ] Тест 4: Проверка предоставления доступа
- [ ] Тест 5: Проверка отзыва доступа

#### Level 2: Тесты траектории
- [ ] Тест 1: Проверка ENTRY/EXIT логов в ShareDialogComponent

#### Level 3: Интеграционные тесты
- [ ] Тест 1: E2E сценарий шаринга трека (Cypress)
- [ ] Тест 2: Интеграция с backend API

## Dependencies
- FEAT-002 (CRUD треков) — требуется трек для предоставления доступа
- Зависит от: PostgreSQL (таблица track_permissions)
- Зависит от: Spring Data JPA (TrackPermissionRepository)

## Acceptance Criteria
- [ ] AC-001: Владелец может предоставить доступ VIEW другому пользователю
- [ ] AC-002: Владелец может предоставить доступ EDIT другому пользователю
- [ ] AC-003: Владелец может изменить тип доступа (VIEW ↔ EDIT)
- [ ] AC-004: Владелец может отозвать доступ у пользователя
- [ ] AC-005: Пользователь может получить список всех прав доступа к треку
- [ ] AC-006: Пользователь с правом VIEW может просматривать трек и заметки
- [ ] AC-007: Пользователь с правом EDIT может просматривать и редактировать трек и заметки
- [ ] AC-008: При удалении пользователя каскадно удаляются все его права доступа
- [ ] AC-009: Время ответа API для управления правами доступа ≤ 300 мс
- [ ] AC-010: Уникальный constraint (track_id, user_id) в таблице track_permissions
