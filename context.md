# Context

## Технологический стек
- **Backend**: Java 17, Spring Boot 3.3.x, Maven
- **База данных**: PostgreSQL 16
- **ORM**: Spring Data JPA (Hibernate)
- **Миграции**: Liquibase 4.x (будет добавлен в проект)

## Текущая структура проекта
- Исходный код находится в `src/main/java/org/homework/`
- Конфигурация Spring находится в `src/main/resources/application.properties`
- Существующие сущности: `User`, `Track`, `Note`, `TrackPermission`

## Требования к миграциям
- Все изменения схемы БД должны быть описаны в Liquibase changelog‑файлах.
- Каждый changelog‑файл должен быть атомарным и иметь уникальный `id` и `author`.
- Необходимо обеспечить возможность отката (rollback) для каждого changeset.
- Мастер‑файл `db.changelog-master.xml` будет включать все отдельные changelog‑файлы.

## Ограничения
- Проект использует Maven, поэтому Liquibase будет интегрирован через плагин `liquibase-maven-plugin`.
- Все скрипты должны быть совместимы с PostgreSQL 16.
