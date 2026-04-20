# План реализации FEAT-002: CRUD для треков

## Статус
planning

## Обзор
Этот документ описывает план реализации фичи FEAT-002 - CRUD операции для управления треками развития.

## Текущее состояние реализации

### Backend (Java 17 + Spring Boot)
**Существующие компоненты:**
- ✅ [`TrackController.java`](src/main/java/org/homework/controller/TrackController.java) - REST контроллер с эндпоинтами
- ✅ [`TrackService.java`](src/main/java/org/homework/service/TrackService.java) - бизнес-логика управления треками
- ✅ [`TrackRepository.java`](src/main/java/org/homework/repository/TrackRepository.java) - JPA репозиторий
- ✅ [`Track.java`](src/main/java/org/homework/model/Track.java) - JPA сущность
- ✅ [`TrackDto.java`](src/main/java/org/homework/dto/response/TrackDto.java) - DTO для деталей трека
- ✅ [`CreateTrackRequest.java`](src/main/java/org/homework/dto/request/CreateTrackRequest.java) - DTO для создания
- ✅ [`UpdateTrackRequest.java`](src/main/java/org/homework/dto/request/UpdateTrackRequest.java) - DTO для обновления

**Отсутствующие компоненты:**
- ❌ [`TrackSummaryDto.java`](src/main/java/org/homework/dto/response/TrackSummaryDto.java) - DTO для краткой информации о треке

### Frontend (Angular 17)
**Существующие компоненты:**
- ✅ [`track.service.ts`](frontend/src/app/core/services/track.service.ts) - HTTP сервис для работы с треками
- ✅ [`track.model.ts`](frontend/src/app/shared/models/track.model.ts) - TypeScript модели треков

**Компоненты для проверки:**
- ⚠️ [`dashboard.component.ts`](frontend/src/app/features/dashboard/dashboard.component.ts) - требует проверки
- ⚠️ [`track-detail.component.ts`](frontend/src/app/features/track-detail/track-detail.component.ts) - требует проверки

## Задачи реализации

### 1. Backend задачи

#### 1.1 Создание отсутствующего DTO
**Задача:** Создать [`TrackSummaryDto.java`](src/main/java/org/homework/dto/response/TrackSummaryDto.java)

**Описание:**
- Создать DTO для краткой информации о треке (используется в списках)
- Поля: id, title, description, owner, myRole, updatedAt
- Добавить семантическую разметку (ANCHOR: TRACK_SUMMARY_DTO)
- Использовать Lombok аннотации (@Data, @NoArgsConstructor, @AllArgsConstructor, @Builder)

**Контракт:**
```
ANCHOR: TRACK_SUMMARY_DTO
PURPOSE: DTO для краткой информации о треке (используется в списках).

@PreConditions:
- нет нетривиальных предусловий

@PostConditions:
- DTO готово для сериализации в JSON

@Invariants:
- id всегда присутствует для существующего трека
- myRole всегда присутствует (OWNER/VIEW/EDIT)

@SideEffects:
- нет побочных эффектов

@ForbiddenChanges:
- нельзя убрать поле myRole
- нельзя изменить тип myRole на не-String

@AllowedRefactorZone:
- можно добавить дополнительные поля (например, color, icon)
```

#### 1.2 Обновление TrackService
**Задача:** Обновить метод `getUserTracks` в [`TrackService.java`](src/main/java/org/homework/service/TrackService.java)

**Описание:**
- Изменить возвращаемый тип с `Page<TrackDto>` на `Page<TrackSummaryDto>`
- Обновить метод маппинга для использования TrackSummaryDto
- Добавить метод `mapToTrackSummaryDto` для маппинга в TrackSummaryDto

**Контракт:**
```
ANCHOR: TRACK_SERVICE_GET_USER_TRACKS
PURPOSE: Получение списка треков пользователя (свои + доступные).

@PreConditions:
- userId валиден
- pageable валиден

@PostConditions:
- при успехе: возвращается Page<TrackSummaryDto> с треками пользователя
- при ошибке: выбрасывается исключение

@Invariants:
- возвращаются только треки, к которым пользователь имеет доступ

@SideEffects:
- чтение треков из БД

@ForbiddenChanges:
- нельзя изменить сигнатуру метода без согласования

@AllowedRefactorZone:
- можно изменить способ объединения результатов
```

#### 1.3 Обновление TrackController
**Задача:** Обновить метод `getAll` в [`TrackController.java`](src/main/java/org/homework/controller/TrackController.java)

**Описание:**
- Изменить возвращаемый тип с `Page<TrackDto>` на `Page<TrackSummaryDto>`
- Обновить логирование для TrackSummaryDto

**Контракт:**
```
ANCHOR: TRACK_CONTROLLER_GET_ALL
PURPOSE: Получение списка треков текущего пользователя + треки с доступом.

@PreConditions:
- пользователь аутентифицирован

@PostConditions:
- возвращается 200 OK со списком треков

@Invariants:
- всегда возвращается Page<TrackSummaryDto>

@SideEffects:
- чтение треков из БД

@ForbiddenChanges:
- нельзя изменить сигнатуру метода без согласования

@AllowedRefactorZone:
- можно добавить дополнительные фильтры
```

#### 1.4 Создание backend тестов
**Задача:** Создать тесты для TrackService

**Описание:**
- Создать файл `src/test/java/org/homework/service/TrackServiceTest.java`
- Level 1: Детерминированные тесты
  - Тест 1: Проверка создания трека
  - Тест 2: Проверка получения трека по ID
  - Тест 3: Проверка обновления трека
  - Тест 4: Проверка удаления трека
  - Тест 5: Проверка получения списка треков пользователя
  - Тест 6: Проверка прав доступа (OWNER)
  - Тест 7: Проверка пагинации списка треков
  - Тест 8: Проверка сортировки списка треков
- Level 2: Тесты траектории
  - Тест 1: Проверка ENTRY/EXIT логов в createTrack()
  - Тест 2: Проверка CHECK логов прав доступа
  - Тест 3: Проверка DECISION логов при отказе
  - Тест 4: Проверка STATE_CHANGE логов при обновлении
- Level 3: Интеграционные тесты
  - Тест 1: E2E сценарий создания и получения трека
  - Тест 2: Интеграция с PostgreSQL
  - Тест 3: Тестирование REST API (MockMvc)
  - Тест 4: Тестирование прав доступа

**Задача:** Создать тесты для TrackController

**Описание:**
- Создать файл `src/test/java/org/homework/controller/TrackControllerTest.java`
- Level 1: Детерминированные тесты
  - Тест 1: Проверка GET /api/v1/tracks
  - Тест 2: Проверка POST /api/v1/tracks
  - Тест 3: Проверка GET /api/v1/tracks/{id}
  - Тест 4: Проверка PUT /api/v1/tracks/{id}
  - Тест 5: Проверка DELETE /api/v1/tracks/{id}
  - Тест 6: Проверка пагинации
  - Тест 7: Проверка сортировки
- Level 2: Тесты траектории
  - Тест 1: Проверка ENTRY/EXIT логов в контроллере
- Level 3: Интеграционные тесты
  - Тест 1: E2E сценарий CRUD операций
  - Тест 2: Тестирование прав доступа через REST API

### 2. Frontend задачи

#### 2.1 Проверка DashboardComponent
**Задача:** Проверить и обновить [`dashboard.component.ts`](frontend/src/app/features/dashboard/dashboard.component.ts)

**Описание:**
- Проверить, что компонент использует TrackService для получения списка треков
- Проверить, что компонент отображает список треков с пагинацией и сортировкой
- Проверить, что компонент обрабатывает ошибки корректно
- Добавить логирование (logLine) для отслеживания операций
- Проверить, что компонент использует TrackSummaryDto модель

**Контракт:**
```
ANCHOR: DASHBOARD_COMPONENT
PURPOSE: UI компонент для отображения списка треков.

@PreConditions:
- пользователь аутентифицирован
- TrackService инициализирован

@PostConditions:
- список треков отображается в таблице
- пагинация работает корректно
- сортировка работает корректно

@Invariants:
- данные треков никогда не логируются в открытом виде

@SideEffects:
- HTTP запросы к API через TrackService
- обновление состояния компонента

@ForbiddenChanges:
- нельзя убрать проверку аутентификации
- нельзя убрать обработку ошибок

@AllowedRefactorZone:
- можно изменить способ отображения данных
- можно добавить дополнительные фильтры
```

#### 2.2 Проверка TrackDetailComponent
**Задача:** Проверить и обновить [`track-detail.component.ts`](frontend/src/app/features/track-detail/track-detail.component.ts)

**Описание:**
- Проверить, что компонент использует TrackService для получения деталей трека
- Проверить, что компонент отображает детали трека
- Проверить, что компонент обрабатывает ошибки корректно
- Добавить логирование (logLine) для отслеживания операций
- Проверить, что компонент использует TrackDto модель

**Контракт:**
```
ANCHOR: TRACK_DETAIL_COMPONENT
PURPOSE: UI компонент для отображения деталей трека.

@PreConditions:
- пользователь аутентифицирован
- TrackService инициализирован
- trackId валиден

@PostConditions:
- детали трека отображаются
- дерево заметок отображается

@Invariants:
- данные трека никогда не логируются в открытом виде

@SideEffects:
- HTTP запросы к API через TrackService
- обновление состояния компонента

@ForbiddenChanges:
- нельзя убрать проверку прав доступа
- нельзя убрать обработку ошибок

@AllowedRefactorZone:
- можно изменить способ отображения данных
```

#### 2.3 Создание frontend тестов
**Задача:** Создать тесты для TrackService

**Описание:**
- Создать файл `frontend/src/app/core/services/track.service.spec.ts`
- Level 1: Детерминированные тесты
  - Тест 1: Проверка отображения списка треков
  - Тест 2: Проверка создания трека
  - Тест 3: Проверка редактирования трека
  - Тест 4: Проверка удаления трека
  - Тест 5: Проверка пагинации
  - Тест 6: Проверка сортировки
  - Тест 7: Проверка фильтрации
- Level 2: Тесты траектории
  - Тест 1: Проверка ENTRY/EXIT логов в TrackService
  - Тест 2: Проверка STATE_CHANGE логов при создании трека
- Level 3: Интеграционные тесты
  - Тест 1: E2E сценарий создания трека (Cypress)
  - Тест 2: Интеграция с backend API

**Задача:** Создать тесты для DashboardComponent

**Описание:**
- Создать файл `frontend/src/app/features/dashboard/dashboard.component.spec.ts`
- Level 1: Детерминированные тесты
  - Тест 1: Проверка отображения списка треков
  - Тест 2: Проверка создания трека
  - Тест 3: Проверка редактирования трека
  - Тест 4: Проверка удаления трека
  - Тест 5: Проверка пагинации
  - Тест 6: Проверка сортировки
- Level 2: Тесты траектории
  - Тест 1: Проверка ENTRY/EXIT логов в DashboardComponent
  - Тест 2: Проверка STATE_CHANGE логов при создании трека
- Level 3: Интеграционные тесты
  - Тест 1: E2E сценарий работы с треками

### 3. Документация

#### 3.1 Обновление статуса FEAT-002
**Задача:** Обновить статус FEAT-002 в [`plans/features/README.md`](plans/features/README.md)

**Описание:**
- Изменить статус с `planning` на `in_progress`
- Обновить статистику (В работе: 1, В планировании: 4)

#### 3.2 Создание отчета о реализации
**Задача:** Создать отчет о реализации FEAT-002

**Описание:**
- Создать файл `reports/implementation_FEAT-002.md`
- Описать выполненные задачи
- Описать проблемы и решения
- Описать результаты тестирования

## Порядок выполнения

### Фаза 1: Backend (приоритет P0)
1. Создать TrackSummaryDto
2. Обновить TrackService (getUserTracks)
3. Обновить TrackController (getAll)
4. Создать тесты для TrackService
5. Создать тесты для TrackController

### Фаза 2: Frontend (приоритет P0)
1. Проверить и обновить DashboardComponent
2. Проверить и обновить TrackDetailComponent
3. Создать тесты для TrackService
4. Создать тесты для DashboardComponent

### Фаза 3: Документация (приоритет P1)
1. Обновить статус FEAT-002 в индексе фич
2. Создать отчет о реализации

## Критерии завершения

### Backend
- [ ] TrackSummaryDto создан с семантической разметкой
- [ ] TrackService.getUserTracks возвращает Page<TrackSummaryDto>
- [ ] TrackController.getAll возвращает Page<TrackSummaryDto>
- [ ] Все backend тесты проходят (минимум 80% покрытие)
- [ ] Контракты соответствуют спецификации FEAT-002

### Frontend
- [ ] DashboardComponent использует TrackSummaryDto модель
- [ ] TrackDetailComponent использует TrackDto модель
- [ ] Все frontend тесты проходят (минимум 80% покрытие)
- [ ] Контракты соответствуют спецификации FEAT-002

### Документация
- [ ] Статус FEAT-002 обновлен в индексе фич
- [ ] Отчет о реализации создан

## Зависимости
- FEAT-001 (Аутентификация) - требуется валидный JWT для доступа к трекам

## Риски
- **Риск 1:** Проблемы с пагинацией при объединении результатов из двух запросов
  - **Митигация:** Использовать более сложный SQL запрос для объединения результатов
- **Риск 2:** Несоответствие типов DTO между backend и frontend
  - **Митигация:** Проверить соответствие типов перед реализацией
- **Риск 3:** Проблемы с правами доступа при получении списка треков
  - **Митигация:** Добавить проверку прав доступа в TrackService

## Примечания
- Все компоненты должны иметь семантическую разметку (ANCHOR)
- Все методы должны иметь логирование (ENTRY, EXIT, BRANCH, DECISION, ERROR, STATE_CHANGE)
- Все тесты должны следовать TDD подходу (тесты до кода)
- Все изменения должны соответствовать контракту из FEAT-002 спецификации
