# Project Context – TrackHub (Треки Развития)

## 1. Общий обзор
- **Наименование**: Веб‑приложение «Треки Развития» (TrackHub)
- **Версия**: 1.0.0
- **Назначение**: Инструмент для управления личным и профессиональным развитием, позволяющий пользователям создавать иерархические треки и заметки, а также делиться ими с другими пользователями.

## 2. Архитектура
### 2.1. Общая схема
```
+----------------+        +----------------------+        +-------------------+
|   Angular SPA  | <--HTTPS/REST--> |   API Gateway (Spring Boot) | <--JPA/Hibernate--> |   PostgreSQL DB   |
+----------------+        +----------------------+        +-------------------+
```
- **Frontend** – одностраничное приложение (SPA) на Angular 17+, использующее PrimeNG/PrimeFlex для UI, RxJS/Signals для управления состоянием и Angular HttpClient для взаимодействия с API.
- **API Gateway** – слой Spring Boot, объединяющий все REST‑эндпоинты, реализует маршрутизацию, глобальный `AuthInterceptor`, обработку ошибок и кросс‑доменные запросы (CORS).
- **Auth Service** – отдельный модуль внутри Spring Boot, отвечает за регистрацию, аутентификацию, выдачу JWT и refresh‑токенов, а также за их валидацию.
- **Business Service Layer** – сервисный слой, реализующий бизнес‑логику (управление треками, заметками, правами доступа) и использующий `PermissionService` для проверки ролей (`OWNER`, `EDIT`, `VIEW`).
- **Data Access Layer** – репозитории Spring Data JPA (Hibernate) для доступа к сущностям `User`, `Track`, `Note`, `TrackPermission`.
- **База данных** – PostgreSQL 16, схемы `users`, `tracks`, `notes`, `track_permissions` с внешними ключами `ON DELETE CASCADE` и индексами для ускорения запросов.
- **Security** – Spring Security + JWT, глобальный фильтр `JwtAuthenticationFilter`, `@PreAuthorize` на контроллерах, защита от CSRF и ограниченный CORS.

## 3. Технологический стек
| Слой | Технология | Версия |
|------|------------|--------|
| Backend | Java | 17 |
|  | Spring Boot | 3.3.x |
|  | Maven | – |
|  | Spring Data JPA (Hibernate) | – |
|  | PostgreSQL | 16 |
|  | Spring Security + JWT | – |
|  | SpringDoc OpenAPI (Swagger) | – |
| Frontend | Angular | 17+ |
|  | TypeScript | 5.x |
|  | PrimeNG | 17+ |
|  | PrimeFlex | – |
|  | RxJS / Signals | – |
|  | Angular HttpClient | – |
| CI/CD | Git, Maven, npm, Docker (optional) | – |

## 4. Стиль кода и правила
### 4.1. Java (Backend)
- **Код‑стайл**: Google Java Style Guide, `@Slf4j` для логирования, `final` где возможно.
- **Валидация**: Bean Validation (`@Valid`, `@Size`, `@Pattern`, `@NotBlank`).
- **Безопасность**: `@PreAuthorize` и кастомный сервис для проверки прав (`OWNER`, `EDIT`, `VIEW`).
- **Логирование**: Structured JSON, уровень INFO в продакшене, ротация файлов.
- **Тесты**: JUnit 5 + Spring Test, TDD – тесты пишутся до кода.

### 4.2. TypeScript / Angular (Frontend)
- **Код‑стайл**: Angular Style Guide, ESLint + Prettier, строгие типы.
- **Формы**: Reactive Forms с клиентской валидацией, зеркалирование серверных ограничений.
- **HTTP**: `HttpClient` с глобальным `AuthInterceptor` (добавление Bearer‑токена, обработка 401/403).
- **Тесты**: Jasmine/Karma, UI‑тесты через Protractor или Cypress.

## 5. Структура проекта
### 5.1. Backend (src/main/java/org/homework)
```
controller/   – REST‑контроллеры (Auth, Track, Note, Permission, Admin)
service/      – бизнес‑логика
repository/   – JPA‑репозитории
model/        – JPA‑сущности (User, Track, Note, TrackPermission)
dto/          – запрос/ответ DTO (request/, response/)
security/     – JWT‑фильтры, провайдер, конфигурация
exception/    – глобальный обработчик, кастомные исключения
```
### 5.2. Frontend (src/app)
```
core/          – сервисы (auth, track, note, admin), interceptors
features/      – модули UI (login, dashboard, track-detail, admin-panel)
shared/        – UI‑компоненты, модели, утилиты
assets/        – статические файлы, иконки
styles/        – глобальные стили (PrimeFlex)
```

## 6. База данных (PostgreSQL)
- Таблицы: `users`, `tracks`, `notes`, `track_permissions`.
- Индексы для ускорения запросов по `track_id`, `parent_id`, `user_id`.
- Ограничения: уникальный `username`, уникальный `(track_id, user_id)` в `track_permissions`.
- Внешние ключи с `ON DELETE CASCADE` для автоматического удаления зависимостей.
- Управление схемой БД осуществляется с помощью Liquibase (XML/SQL миграции).

## 7. Безопасность и авторизация
- **JWT**: access‑token (короткоживущий) + refresh‑token.
- **Spring Security**: конфигурация ролей `USER`, `ADMIN`.
- **Контроль доступа**: `@PreAuthorize` использует сервис `PermissionService` для проверки `OWNER`, `EDIT`, `VIEW`.
- **CSRF**: отключён для API (токен‑базированная аутентификация).
- **CORS**: разрешён только домен фронтенда.

## 8. API (REST)
- Полный набор эндпоинтов описан в ТЗ (см. раздел 6). Все ответы используют DTO, ошибки – `ErrorResponseDto`.
- Пагинация, сортировка, фильтрация реализованы через Spring Data `Pageable`.
- Дерево заметок возвращается одним запросом (`WITH RECURSIVE`).

## 9. UI / UX
- **Главная страница**: Header с логотипом и кнопкой выхода, Sidebar с навигацией (Мои треки, Доступ мне, Админ‑панель).
- **Dashboard**: таблица треков (PrimeNG `p-table`) с пагинацией, сортировкой, фильтрацией.
- **Track Detail**: слева `p-tree` с деревом заметок, справа редактор (будущий Markdown‑editor).
- **Sharing**: диалог выбора пользователя и роли (`VIEW`/`EDIT`).
- **Admin‑панель**: таблица пользователей, блокировка/удаление, изменение роли.

## 10. Нефункциональные требования
- **Производительность**: ≤300 мс ответ API для 95 % запросов, поддержка до 100 одновременных пользователей.
- **Валидация**: серверная (Bean Validation) + клиентская (Angular Reactive Forms).
- **Логирование**: структурированные JSON‑логи, ротация, уровень INFO в продакшене.
- **Обработка ошибок**: единый формат `ErrorResponseDto`.
- **Безопасность**: BCrypt для паролей, JWT с истечением, защита от CSRF, CORS.

## 11. Стиль документации
- **API**: Swagger UI (SpringDoc OpenAPI) доступен по `/swagger-ui.html`.
- **Код**: Javadoc для публичных классов, комментарии к бизнес‑логике.
- **README**: инструкции по запуску (Docker/Local), сборке (`mvn clean install`, `npm install`), тестированию.

---
*Этот документ служит единой точкой описания границ проекта и будет использоваться всеми участниками команды для согласования архитектурных решений и стиля разработки.*