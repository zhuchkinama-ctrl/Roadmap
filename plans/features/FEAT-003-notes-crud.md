# Feature Specification: FEAT-003

## Status
planning

## Overview
CRUD операции для управления иерархическими заметками внутри треков. Создание, чтение, обновление, удаление заметок с поддержкой иерархии (parent-child) и порядка сортировки (orderIndex).

## Requirements

### Functional Requirements
- FR-001: Пользователь может создать корневую заметку в треке (без parent_id)
- FR-002: Пользователь может создать дочернюю заметку (с parent_id)
- FR-003: Пользователь может получить полное дерево заметок трека
- FR-004: Пользователь может получить заметку по ID
- FR-005: Пользователь может обновить заметку (title, content)
- FR-006: Пользователь может удалить заметку с поддеревом (каскадное)
- FR-007: Пользователь может переместить заметку (изменить parent_id и orderIndex)
- FR-008: Дерево заметок возвращается одним запросом (WITH RECURSIVE)

### Non-Functional Requirements
- NFR-001: Время ответа API для получения дерева заметок ≤ 300 мс для 95% запросов
- NFR-002: Система должна поддерживать до 100 одновременных пользователей
- NFR-003: Индексы для ускорения запросов по track_id, parent_id
- NFR-004: Использование WITH RECURSIVE для получения дерева заметок

## Architecture

### Backend Components
- NoteController (ANCHOR: NOTE_CONTROLLER)
- NoteService (ANCHOR: NOTE_SERVICE)
- NoteRepository (ANCHOR: NOTE_REPOSITORY)
- Note (ANCHOR: NOTE_MODEL)
- CreateNoteRequest (ANCHOR: CREATE_NOTE_REQUEST)
- UpdateNoteRequest (ANCHOR: UPDATE_NOTE_REQUEST)
- NoteDto (ANCHOR: NOTE_DTO)
- NoteTreeDto (ANCHOR: NOTE_TREE_DTO)

### Frontend Components
- TrackDetailComponent (ANCHOR: TRACK_DETAIL_COMPONENT)
- NoteService (ANCHOR: NOTE_SERVICE_FRONTEND)
- NoteModel (ANCHOR: NOTE_MODEL_FRONTEND)

### Data Flow
```
Frontend (TrackDetail) → NoteService → Backend (NoteController)
→ NoteService → NoteRepository → PostgreSQL (WITH RECURSIVE)
→ NoteTreeDto → NoteService → Frontend (p-tree)
```

## Contracts

### Backend Component 1 (ANCHOR: NOTE_SERVICE)
```
[START_NOTE_SERVICE]
/*
 * ANCHOR: NOTE_SERVICE
 * PURPOSE: Бизнес-логика управления иерархическими заметками
 *
 * @PreConditions:
 * - пользователь аутентифицирован (имеет валидный JWT)
 * - трек существует и пользователь имеет VIEW/EDIT права
 * - parent_id (если указан) существует и принадлежит тому же треку
 *
 * @PostConditions:
 * - при успехе создания: возвращается NoteDto с новой заметкой
 * - при успехе получения дерева: возвращается List<NoteTreeDto>
 * - при успехе обновления: возвращается NoteDto с обновлёнными данными
 * - при успехе удаления: заметка и все дочерние заметки удалены
 * - при успехе перемещения: заметка перемещена в новое место
 *
 * @Invariants:
 * - заметка всегда принадлежит треку (track_id)
 * - заметка всегда имеет title (не пустой)
 * - parent_id должен принадлежать тому же треку
 * - orderIndex определяет порядок отображения заметок
 *
 * @SideEffects:
 * - создаёт запись заметки в БД
 * - обновляет запись заметки в БД
 * - удаляет запись заметки и дочерние заметки из БД
 * - перемещает запись заметки в новое место
 * - пишет лог операции в audit log
 *
 * @ForbiddenChanges:
 * - нельзя создавать заметку без прав EDIT/OWNER
 * - нельзя удалять заметку без прав EDIT/OWNER
 * - нельзя перемещать заметку без прав EDIT/OWNER
 * - нельзя нарушать иерархию (parent_id должен принадлежать тому же треку)
 *
 * @AllowedRefactorZone:
 * - можно изменить способ получения дерева заметок (WITH RECURSIVE → Materialized Path)
 * - можно добавить кэширование дерева заметок
 * - можно изменить формат логирования
 */
public class NoteService {
    public NoteDto createNote(Long trackId, CreateNoteRequest request, Long userId);
    public List<NoteTreeDto> getNoteTree(Long trackId, Long userId);
    public NoteDto getNote(Long noteId, Long userId);
    public NoteDto updateNote(Long noteId, UpdateNoteRequest request, Long userId);
    public void deleteNote(Long noteId, Long userId);
    public void moveNote(Long noteId, Long parentId, Integer orderIndex, Long userId);
}
// [END_NOTE_SERVICE]
```

### Frontend Component 1 (ANCHOR: NOTE_SERVICE_FRONTEND)
```
[START_NOTE_SERVICE_FRONTEND]
/*
 * ANCHOR: NOTE_SERVICE_FRONTEND
 * PURPOSE: Управление заметками на фронтенде
 *
 * @PreConditions:
 * - HTTP клиент инициализирован
 * - AuthInterceptor настроен
 *
 * @PostConditions:
 * - при успехе: данные заметок сохранены в BehaviorSubject/Signal
 * - при ошибке: отображается сообщение об ошибке
 *
 * @Invariants:
 * - данные заметок никогда не логируются в открытом виде
 * - дерево заметок обновляется автоматически при изменении
 *
 * @SideEffects:
 * - сохраняет данные заметок в BehaviorSubject/Signal
 * - устанавливает заголовок Authorization для последующих запросов
 *
 * @ForbiddenChanges:
 * - нельзя удалять данные заметок при ошибке без перенаправления на login
 * - нельзя хранить данные заметок в небезопасных местах
 *
 * @AllowedRefactorZone:
 * - можно изменить способ хранения данных (BehaviorSubject → Signals)
 * - можно добавить кэширование данных заметок
 */
export class NoteService {
    createNote(trackId: number, title: string, content?: string, parentId?: number): Observable<NoteDto>
    getNoteTree(trackId: number): Observable<NoteTreeDto[]>
    getNote(noteId: number): Observable<NoteDto>
    updateNote(noteId: number, title: string, content?: string): Observable<NoteDto>
    deleteNote(noteId: number): Observable<void>
    moveNote(noteId: number, parentId?: number, orderIndex?: number): Observable<void>
    getNoteTree(): BehaviorSubject<NoteTreeDto[]>
}
// [END_NOTE_SERVICE_FRONTEND]
```

## API

### Endpoints
- GET /api/v1/tracks/{trackId}/notes/tree — Полное дерево заметок
- POST /api/v1/tracks/{trackId}/notes — Создание заметки (корневой или дочерней)
- PUT /api/v1/notes/{id} — Обновление заметки
- DELETE /api/v1/notes/{id} — Удаление заметки с поддеревом
- PATCH /api/v1/notes/{id}/move — Перемещение заметки

### DTO
- CreateNoteRequest — Запрос создания заметки (title, content, parentId?)
- UpdateNoteRequest — Запрос обновления заметки (title, content)
- NoteDto — Данные заметки (id, track, title, content, parent, orderIndex, createdAt, updatedAt)
- NoteTreeDto — Дерево заметок (id, title, content, orderIndex, children: NoteTreeDto[])

## UI

### Components
- TrackDetailComponent — Дерево заметок (p-tree) и редактор заметки

### Services
- NoteService — HTTP запросы к API заметок

## Test Plan

### Backend Tests (JUnit 5 + Spring Test)

#### Level 1: Детерминированные тесты
- [ ] Тест 1: Проверка создания корневой заметки
- [ ] Тест 2: Проверка создания дочерней заметки
- [ ] Тест 3: Проверка получения дерева заметок
- [ ] Тест 4: Проверка получения заметки по ID
- [ ] Тест 5: Проверка обновления заметки
- [ ] Тест 6: Проверка удаления заметки с поддеревом
- [ ] Тест 7: Проверка перемещения заметки
- [ ] Тест 8: Проверка иерархии (parent-child)

#### Level 2: Тесты траектории
- [ ] Тест 1: Проверка ENTRY/EXIT логов в createNote()
- [ ] Тест 2: Проверка BRANCH логов при иерархии
- [ ] Тест 3: Проверка STATE_CHANGE логов при перемещении

#### Level 3: Интеграционные тесты
- [ ] Тест 1: E2E сценарий создания иерархии заметок
- [ ] Тест 2: Интеграция с PostgreSQL (WITH RECURSIVE)
- [ ] Тест 3: Тестирование REST API (MockMvc)

### Frontend Tests (Jasmine/Karma)

#### Level 1: Детерминированные тесты
- [ ] Тест 1: Проверка отображения дерева заметок
- [ ] Тест 2: Проверка создания заметки
- [ ] Тест 3: Проверка редактирования заметки
- [ ] Тест 4: Проверка удаления заметки
- [ ] Тест 5: Проверка перемещения заметки
- [ ] Тест 6: Проверка разворачивания/сворачивания

#### Level 2: Тесты траектории
- [ ] Тест 1: Проверка ENTRY/EXIT логов в TrackDetailComponent
- [ ] Тест 2: Проверка BRANCH логов при иерархии

#### Level 3: Интеграционные тесты
- [ ] Тест 1: E2E сценарий создания иерархии заметок (Cypress)
- [ ] Тест 2: Интеграция с backend API

## Dependencies
- FEAT-002 (CRUD треков) — требуется трек для создания заметок
- Зависит от: PostgreSQL (таблица notes)
- Зависит от: Spring Data JPA (NoteRepository)

## Acceptance Criteria
- [ ] AC-001: Пользователь может создать корневую заметку
- [ ] AC-002: Пользователь может создать дочернюю заметку
- [ ] AC-003: Пользователь может получить полное дерево заметок
- [ ] AC-004: Пользователь может получить заметку по ID
- [ ] AC-005: Пользователь может обновить заметку
- [ ] AC-006: Пользователь может удалить заметку с поддеревом
- [ ] AC-007: Пользователь может переместить заметку
- [ ] AC-008: Дерево заметок возвращается одним запросом (WITH RECURSIVE)
- [ ] AC-009: Время ответа API для получения дерева заметок ≤ 300 мс
- [ ] AC-010: Индексы для ускорения запросов по track_id, parent_id
