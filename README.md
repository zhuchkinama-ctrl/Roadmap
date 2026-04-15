# TrackHub – Треки Развития

## О проекте

Веб‑приложение **TrackHub** предназначено для управления личным и профессиональным развитием. Пользователи могут создавать иерархические треки, вести заметки, а также делиться доступом к трекам с другими пользователями.

## Технологический стек

- **Backend**: Java 17, Spring Boot 3.3.x, Spring Security (JWT), Spring Data JPA (Hibernate), PostgreSQL 16, Lombok, SpringDoc OpenAPI, Bean Validation, JUnit 5.
- **Frontend**: Angular 17+, TypeScript 5.x, PrimeNG 17+, PrimeFlex, RxJS / Signals, Angular HttpClient.

## Запуск backend

1. Установите JDK 17.
2. Склонируйте репозиторий и перейдите в корень проекта.
3. Настройте базу данных PostgreSQL (порт 5416, база `trackhub`, пользователь `postgres`).
4. Выполните миграции базы данных с помощью Liquibase:
   ```cmd
   mvn liquibase:update
   ```
   Дополнительные команды Liquibase:
   - `mvn liquibase:dropAll` – удалить все объекты базы данных
   - `mvn liquibase:status` – проверить статус миграций
   - `mvn liquibase:rollback` – откатить последнюю миграцию
5. Выполните сборку и запуск:
   ```cmd
   mvn clean install
   mvn spring-boot:run
   ```
6. Приложение будет доступно по `http://localhost:8080`.

## Подготовка frontend

1. Установите Node.js ≥ 18 и Angular CLI:
   ```bash
   npm install -g @angular/cli
   ```
2. Перейдите в каталог `frontend` (будет создан в дальнейшем) и инициализируйте Angular‑проект:
   ```bash
   ng new trackhub-frontend --routing --style=scss
   cd trackhub-frontend
   npm install primeng@17 primeflex@latest primeicons@latest
   ```
3. Скопируйте созданные директории `core`, `features`, `shared`, `assets`, `styles` в `src/app` проекта.
4. Запустите приложение:
   ```bash
   ng serve
   ```
   Доступно по `http://localhost:4200`.

## Тестирование

- **Backend**: `mvn test` (JUnit 5, Spring Test).
- **Frontend**: `ng test` (Karma + Jasmine).

## Дальнейшие шаги

- Реализовать бизнес‑логику в сервисах.
- Добавить маппинг DTO (MapStruct/ModelMapper).
- Реализовать Angular‑компоненты и сервисы.
- Настроить CI/CD (GitHub Actions.
