# Список задач по реализации недостающего функционала

## Backend

- [ ] Реализовать бизнес‑логику треков в `src/main/java/org/homework/service/TrackService.java`
- [ ] Реализовать CRUD‑операции над треками в `src/main/java/org/homework/controller/TrackController.java`
- [ ] Добавить пагинацию и сортировку в эндпоинт `GET /api/v1/tracks`
- [ ] Реализовать CRUD‑операции над заметками в `src/main/java/org/homework/controller/NoteController.java`
- [ ] Реализовать получение дерева заметок (`GET /api/v1/tracks/{trackId}/notes/tree`)
- [ ] Реализовать перемещение заметки (`PATCH /api/v1/notes/{id}/move`)
- [ ] Реализовать управление правами доступа в `src/main/java/org/homework/controller/PermissionController.java`
- [ ] Добавить `AdminController` с эндпоинтами администрирования пользователей (`src/main/java/org/homework/controller/AdminController.java`)
- [ ] Написать тесты для сервисов и контроллеров (TDD)
- [ ] Реализовать обработку ошибок с использованием `ErrorResponseDto`

## Frontend

- [ ] Реализовать UI‑компоненты для CRUD треков (Dashboard)
- [ ] Добавить пагинацию, сортировку и фильтрацию в таблице треков
- [ ] Реализовать дерево заметок в `src/app/features/track-detail/track-detail.component.ts`
- [ ] Добавить формы создания/редактирования заметок
- [ ] Реализовать диалог «Поделиться» и интеграцию с API прав доступа
- [ ] Реализовать админ‑панель с управлением пользователями
- [ ] Добавить глобальный `AuthInterceptor` для обработки 401/403 и редиректа
- [ ] Написать UI‑тесты (Jasmine/Karma) для проверок отображения/скрытия кнопок и редиректа

## Общие задачи

- [ ] Обновить документацию Swagger
- [ ] Настроить CI/CD (Maven, npm)
- [ ] Провести ревью кода и обеспечить покрытие тестами > 80%
