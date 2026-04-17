# Reports

Эта директория содержит отчёты по верификации и исправлению багов.

## Структура директорий

```
reports/
├── verification-*.md   # Verification Reports
├── fix-*.md           # Fix Reports
└── README.md          # Этот файл
```

## Формат имени файла

- Verification Report: `verification-FEAT-XXX-feature-name.md`
- Fix Report: `fix-BUG-XXX-bug-name.md`

Примеры:
- `verification-FEAT-001-user-authentication.md`
- `fix-BUG-001-login-failure.md`

## Verification Report

Содержит:
- Результаты прогона тестов (3 уровня)
- Проверку log-маркеров
- Анализ соответствия контрактов
- Рекомендации по улучшению

## Fix Report

Содержит:
- Описание бага
- Анализ причины
- Предложенное решение
- Изменённый код с сохранением контрактов
- Результаты повторной верификации
