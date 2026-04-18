# Feature Specification: FEAT-002

## Status
planning

## Overview
CRUD операции для управления треками развития. Создание, чтение, обновление, удаление треков с поддержкой пагинации, сортировки и прав доступа.

## Requirements

### Functional Requirements
- FR-001: Пользователь может создать новый трек с title (не пустое, max 200 символов) и description (опционально)
- FR-002: Пользователь может получить список своих треков + треков с доступом (VIEW/EDIT)
- FR-003: Список треков поддерживает пагинацию (page, size) и сортировку (sort)
- FR-004: Пользователь может получить детали трека по ID (требуется VIEW/EDIT/OWNER права)
- FR-005: Пользователь может обновить трек (требуется EDIT/OWNER права)
- FR-006: Пользователь может удалить трек (требуется OWNER права)
- FR-007: При удалении трека каскадно удаляются все связанные заметки

### Non-Functional Requirements
- NFR-001: Время ответа API для получения списка треков ≤ 300 мс для 95% запросов
- NFR-002: Система должна поддерживать до 100 одновременных пользователей
- NFR-003: Индексы для ускорения запросов по track_id, owner_id
- NFR-004: Использование WITH RECURSIVE для получения дерева заметок

## Architecture

### Backend Components
- TrackController (ANCHOR: TRACK_CONTROLLER)
- TrackService (ANCHOR: TRACK_SERVICE)
- TrackRepository (ANCHOR: TRACK_REPOSITORY)
- Track (ANCHOR: TRACK_MODEL)
- CreateTrackRequest (ANCHOR: CREATE_TRACK_REQUEST)
- UpdateTrackRequest (ANCHOR: UPDATE_TRACK_REQUEST)
- TrackDto (ANCHOR: TRACK_DTO)
- TrackSummaryDto (ANCHOR: TRACK_SUMMARY_DTO)

### Frontend Components
- DashboardComponent (ANCHOR: DASHBOARD_COMPONENT)
- TrackService (ANCHOR: TRACK_SERVICE_FRONTEND)
- TrackModel (ANCHOR: TRACK_MODEL_FRONTEND)
- TrackDetailComponent (ANCHOR: TRACK_DETAIL_COMPONENT)

### Data Flow
```
Frontend (Dashboard) → TrackService → Backend (TrackController)
→ TrackService → TrackRepository → PostgreSQL
→ TrackDto/TrackSummaryDto → TrackService → Frontend
```

## Contracts

### Backend Component 1 (ANCHOR: TRACK_SERVICE)
```
[START_TRACK_SERVICE]
/*
 * ANCHOR: TRACK_SERVICE
 * PURPOSE: Бизнес-логика управления треками развития
 *
 * @PreConditions:
 * - пользователь аутентифицирован (имеет валидный JWT)
 * - title трека не пустое и не превышает 200 символов
 *
 * @PostConditions:
 * - при успехе создания: возвращается TrackDto с новым треком
 * - при успехе получения: возвращается TrackDto или TrackSummaryDto
 * - при успехе обновления: возвращается TrackDto с обновлёнными данными
 * - при успехе удаления: трек и все связанные заметки удалены
 *
 * @Invariants:
 * - трек всегда имеет владельца (owner_id)
 * - трек всегда имеет title (не пустой)
 * - пользователь не может удалить чужой трек без OWNER прав
 *
 * @SideEffects:
 * - создаёт запись трека в БД
 * - обновляет запись трека в БД
 * - удаляет запись трека и связанные заметки из БД
 * - пишет лог операции в audit log
 *
 * @ForbiddenChanges:
 * - нельзя удалять трек без проверки OWNER прав
 * - нельзя обновлять чужой трек без EDIT прав
 * - нельзя удалять трек без каскадного удаления заметок
 *
 * @AllowedRefactorZone:
 * - можно изменить способ получения списка треков (с пагинацией/без)
 * - можно добавить кэширование треков
 * - можно изменить формат логирования
 */
public class TrackService {
    public TrackDto createTrack(CreateTrackRequest request, Long userId);
    public Page<TrackSummaryDto> listTracks(Long userId, Pageable pageable);
    public TrackDto getTrack(Long trackId, Long userId);
    public TrackDto updateTrack(Long trackId, UpdateTrackRequest request, Long userId);
    public void deleteTrack(Long trackId, Long userId);
}
// [END_TRACK_SERVICE]
```

### Frontend Component 1 (ANCHOR: TRACK_SERVICE_FRONTEND)
```
[START_TRACK_SERVICE_FRONTEND]
/*
 * ANCHOR: TRACK_SERVICE_FRONTEND
 * PURPOSE: Управление треками на фронтенде
 *
 * @PreConditions:
 * - HTTP клиент инициализирован
 * - AuthInterceptor настроен
 *
 * @PostConditions:
 * - при успехе: данные треков сохранены в BehaviorSubject/Signal
 * - при ошибке: отображается сообщение об ошибке
 *
 * @Invariants:
 * - данные треков никогда не логируются в открытом виде
 * - список треков обновляется автоматически при изменении
 *
 * @SideEffects:
 * - сохраняет данные треков в BehaviorSubject/Signal
 * - устанавливает заголовок Authorization для последующих запросов
 *
 * @ForbiddenChanges:
 * - нельзя удалять данные треков при ошибке без перенаправления на login
 * - нельзя хранить данные треков в небезопасных местах
 *
 * @AllowedRefactorZone:
 * - можно изменить способ хранения данных (BehaviorSubject → Signals)
 * - можно добавить кэширование данных треков
 */
export class TrackService {
    createTrack(title: string, description?: string): Observable<TrackDto>
    listTracks(page: number, size: number, sort?: string): Observable<Page<TrackSummaryDto>>
    getTrack(trackId: number): Observable<TrackDto>
    updateTrack(trackId: number, title: string, description?: string): Observable<TrackDto>
    deleteTrack(trackId: number): Observable<void>
    getTracks(): BehaviorSubject<Track[]>
}
// [END_TRACK_SERVICE_FRONTEND]
```

## API

### Endpoints
- GET /api/v1/tracks — Список треков текущего пользователя + треки с доступом (пагинация, сортировка)
- POST /api/v1/tracks — Создание нового трека
- GET /api/v1/tracks/{id} — Получение деталей трека
- PUT /api/v1/tracks/{id} — Обновление трека
- DELETE /api/v1/tracks/{id} — Удаление трека (каскадное)

### DTO
- CreateTrackRequest — Запрос создания трека (title, description)
- UpdateTrackRequest — Запрос обновления трека (title, description)
- TrackDto — Детали трека (id, title, description, owner, myRole, createdAt, updatedAt)
- TrackSummaryDto — Краткая информация о треке (id, title, owner, myRole, updatedAt)

## UI

### Components
- DashboardComponent — Таблица треков с пагинацией, сортировкой, фильтрацией
- TrackDetailComponent — Детали трека с деревом заметок

### Services
- TrackService — HTTP запросы к API треков

## Test Plan

### Backend Tests (JUnit 5 + Spring Test)

#### Level 1: Детерминированные тесты
- [ ] Тест 1: Проверка создания трека
- [ ] Тест 2: Проверка получения трека по ID
- [ ] Тест 3: Проверка обновления трека
- [ ] Тест 4: Проверка удаления трека
- [ ] Тест 5: Проверка получения списка треков пользователя
- [ ] Тест 6: Проверка прав доступа (OWNER)
- [ ] Тест 7: Проверка пагинации списка треков
- [ ] Тест 8: Проверка сортировки списка треков

#### Level 2: Тесты траектории
- [ ] Тест 1: Проверка ENTRY/EXIT логов в createTrack()
- [ ] Тест 2: Проверка CHECK логов прав доступа
- [ ] Тест 3: Проверка DECISION логов при отказе
- [ ] Тест 4: Проверка STATE_CHANGE логов при обновлении

#### Level 3: Интеграционные тесты
- [ ] Тест 1: E2E сценарий создания и получения трека
- [ ] Тест 2: Интеграция с PostgreSQL
- [ ] Тест 3: Тестирование REST API (MockMvc)
- [ ] Тест 4: Тестирование прав доступа

### Frontend Tests (Jasmine/Karma)

#### Level 1: Детерминированные тесты
- [ ] Тест 1: Проверка отображения списка треков
- [ ] Тест 2: Проверка создания трека
- [ ] Тест 3: Проверка редактирования трека
- [ ] Тест 4: Проверка удаления трека
- [ ] Тест 5: Проверка пагинации
- [ ] Тест 6: Проверка сортировки
- [ ] Тест 7: Проверка фильтрации

#### Level 2: Тесты траектории
- [ ] Тест 1: Проверка ENTRY/EXIT логов в DashboardComponent
- [ ] Тест 2: Проверка STATE_CHANGE логов при создании трека

#### Level 3: Интеграционные тесты
- [ ] Тест 1: E2E сценарий создания трека (Cypress)
- [ ] Тест 2: Интеграция с backend API

## Dependencies
- FEAT-001 (Аутентификация) — требуется валидный JWT для доступа к трекам
- Зависит от: PostgreSQL (таблица tracks)
- Зависит от: Spring Data JPA (TrackRepository)

## Acceptance Criteria
- [ ] AC-001: Пользователь может создать трек с валидными данными
- [ ] AC-002: Пользователь не может создать трек с пустым title
- [ ] AC-003: Пользователь может получить список своих треков
- [ ] AC-004: Пользователь может получить треки с доступом (VIEW/EDIT)
- [ ] AC-005: Пользователь может получить детали трека с правами VIEW/EDIT/OWNER
- [ ] AC-006: Пользователь может обновить трек с правами EDIT/OWNER
- [ ] AC-007: Пользователь может удалить трек с правами OWNER
- [ ] AC-008: При удалении трека каскадно удаляются заметки
- [ ] AC-009: Список треков поддерживает пагинацию (page, size)
- [ ] AC-010: Список треков поддерживает сортировку (sort)
- [ ] AC-011: Время ответа API для получения списка треков ≤ 300 мс
- [ ] AC-012: Индексы для ускорения запросов по track_id, owner_id
