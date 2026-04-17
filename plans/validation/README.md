# Validation Reports

Эта директория содержит Validation Reports для всех фич проекта.

## Структура директорий

```
plans/
├── features/           # Feature Specifications
│   ├── FEAT-XXX-feature-name.md
│   └── README.md
├── validation/         # Validation Reports
│   ├── V-FEAT-XXX-feature-name.xml
│   └── README.md       # Этот файл
└── tasks.md           # Список задач
```

## Формат имени файла

- Feature Spec: `FEAT-XXX-feature-name.md`
- Validation Report: `V-FEAT-XXX-feature-name.xml`

Примеры:
- `FEAT-001-user-authentication.md`
- `V-FEAT-001-user-authentication.xml`

## Шаблон Validation Report

См. `.kilocode/templates/validation-report-template.xml`

## Назначение

Validation Report содержит:
- Тест-сценарии 3 уровней (детерминированные, траекторные, интеграционные)
- Критерии проверки
- Ожидаемые результаты
- Зависимости между тестами
