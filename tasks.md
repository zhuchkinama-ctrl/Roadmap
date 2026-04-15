# План задач для внедрения Liquibase

## 1. Подготовка среды
- [ ] Добавить `liquibase-maven-plugin` в `pom.xml` (выполнено).
- [ ] Создать директорию `src/main/resources/db/changelog/` (выполнено).

## 2. Реализация changelog‑файлов
- [ ] `001-create-users-table.xml` (выполнено).
- [ ] `002-create-tracks-table.xml` (выполнено).
- [ ] `003-create-notes-table.xml` (выполнено).
- [ ] `004-create-track-permissions-table.xml` (выполнено).
- [ ] Обновить `db.changelog-master.xml` для включения всех файлов (выполнено).

## 3. Конфигурация Liquibase
- [ ] Добавить свойства подключения к БД в `application.properties` (при необходимости).
- [ ] Проверить, что `liquibase:update` работает в режиме тестовой БД.

## 4. Тестирование миграций
- [ ] Запустить `mvn liquibase:update` и убедиться, что все таблицы созданы.
- [ ] Выполнить `mvn liquibase:rollback -Dliquibase.rollbackCount=1` и проверить откат последнего changeset.

## 5. Документация
- [ ] Обновить `README.md` с инструкциями по запуску миграций.
- [ ] Добавить раздел в `architecture.md` о стратегии миграций.

## 6. Приёмка
- [ ] Пройти критерии приёмки (см. `acceptance_criteria.md`).
