# Паттерны ошибок

> Этот файл содержит все выявленные паттерны ошибок ИИ-агента и соответствующие правила.
> **Дата создания**: 2026-04-14
> **Принцип**: Каждая ошибка — повод для правила.

---

## Как пользоваться этим файлом

1. **При ошибке** — найти подходящий паттерн или создать новый
2. **Перед действием** — проверить применимость правил
3. **При ревью** — использовать как чеклист

---

## 1. Контракты и семантика

*(Правила будут добавляться по мере выявления ошибок)*

---

## 2. Логирование

*(Правила будут добавляться по мере выявления ошибок)*

---

## 3. Архитектура

### RULE_ARCH_001: Реализовывать методы контроллеров, а не оставлять заглушки

**Проблема**: [2026-04-16] Метод `update` в [`NoteController`](src/main/java/org/homework/controller/NoteController.java:127) был заглушкой, которая не вызывала сервис. Фронтенд вызывал `PUT /notes/{id}`, но контроллер возвращал только `200 OK` без выполнения бизнес-логики.

**Причина**: Агент не проверил, что метод контроллера полностью реализован и вызывает соответствующий сервис.

**Решение**:
1. При создании или изменении метода контроллера — убедиться, что он вызывает соответствующий метод сервиса
2. Проверить, что типы возвращаемых значений соответствуют ожиданиям фронтенда
3. Убедиться, что все параметры передаются корректно
4. Проверить наличие валидации (`@Valid`) и обработки ошибок

**Пример**:
❌ Неправильно:
```java
@PutMapping("/{id}")
public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Object request) {
    log.info("NOTE_CONTROLLER_UPDATE ENTRY - id: {}", id);
    // TODO: реализовать обновление заметки
    log.info("NOTE_CONTROLLER_UPDATE EXIT - stub - id: {}", id);
    return ResponseEntity.ok().build();
}
```

✅ Правильно:
```java
@PutMapping("/{id}")
public ResponseEntity<NoteDto> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateNoteRequest request,
        Authentication authentication) {
    log.info("NOTE_CONTROLLER_UPDATE ENTRY - id: {}, title: {}", id, request.getTitle());
    
    Long userId = getUserIdFromAuthentication(authentication);
    NoteDto note = noteService.updateNote(id, request, userId);
    
    log.info("NOTE_CONTROLLER_UPDATE EXIT - note updated with id: {}", note.getId());
    return ResponseEntity.ok(note);
}
```

**Применимость**: При создании или изменении любого метода контроллера.

**Дата добавления**: 2026-04-16
**Инцидент**: Не работает редактирование заметки

---

## 4. Код

### RULE_CODE_001: Соответствие типов DTO между фронтендом и бэкендом

**Проблема**: [2026-04-16] Фронтенд ожидал `NoteDto` от `updateNote`, но контроллер возвращал `NoteTreeDto`. Это приводило к ошибкам десериализации на фронтенде.

**Причина**: Агент не проверил, какие типы DTO ожидаются фронтендом и какие возвращает сервис.

**Решение**:
1. При создании метода сервиса — проверить, какой тип DTO ожидает фронтенд
2. Убедиться, что метод сервиса возвращает правильный тип DTO
3. Создать отдельные методы маппинга для разных типов DTO (`mapToNoteDto`, `mapToNoteTreeDto`)
4. Проверить, что все поля DTO соответствуют ожиданиям фронтенда

**Пример**:
❌ Неправильно:
```java
// Сервис возвращает NoteTreeDto, но фронтенд ожидает NoteDto
public NoteTreeDto updateNote(Long noteId, UpdateNoteRequest request, Long userId) {
    // ... логика
    return mapToNoteTreeDto(note);
}
```

✅ Правильно:
```java
// Сервис возвращает NoteDto, как ожидает фронтенд
public NoteDto updateNote(Long noteId, UpdateNoteRequest request, Long userId) {
    // ... логика
    return mapToNoteDto(note);
}

// Отдельный метод для маппинга в NoteTreeDto (для дерева заметок)
private NoteTreeDto mapToNoteTreeDto(Note note) {
    return NoteTreeDto.builder()
            .id(note.getId())
            .title(note.getTitle())
            .content(note.getContent())
            .orderIndex(note.getOrderIndex())
            .createdAt(note.getCreatedAt())
            .updatedAt(note.getUpdatedAt())
            .children(null)
            .build();
}

// Отдельный метод для маппинга в NoteDto (для CRUD операций)
private NoteDto mapToNoteDto(Note note) {
    NoteDto dto = new NoteDto();
    dto.setId(note.getId());
    dto.setTrackId(note.getTrack().getId());
    dto.setParentId(note.getParent() != null ? note.getParent().getId() : null);
    dto.setTitle(note.getTitle());
    dto.setContent(note.getContent());
    dto.setOrderIndex(note.getOrderIndex());
    dto.setCreatedAt(note.getCreatedAt());
    dto.setUpdatedAt(note.getUpdatedAt());
    return dto;
}
```

**Применимость**: При создании или изменении методов сервиса, которые возвращают DTO.

**Дата добавления**: 2026-04-16
**Инцидент**: Не работает редактирование заметки

---

## 5. Работа с данными

*(Правила будут добавляться по мере выявления ошибок)*

---

## 6. Тестирование

### RULE_TEST_001: Проверять соответствие контрактов перед генерацией кода

**Проблема**: [2026-04-17] При реализации FEAT-001 агент мог бы пропустить проверку соответствия контрактов, что привело бы к несоответствию между спецификацией и реализацией.

**Причина**: Агент не проверил соответствие контрактов из Feature Specification с реальной реализацией перед генерацией кода.

**Решение**:
1. Перед генерацией кода прочитать Feature Specification
2. Проверить соответствие ANCHOR_ID в коде с ANCHOR_ID в спецификации
3. Проверить соответствие контрактов (PURPOSE, @PreConditions, @PostConditions, @Invariants, @SideEffects, @ForbiddenChanges, @AllowedRefactorZone)
4. Проверить соответствие DTO между фронтендом и бэкендом
5. Проверить соответствие логирования (ENTRY/EXIT/BRANCH/DECISION/ERROR)

**Пример**:
❌ Неправильно:
```java
// Контракт в коде не соответствует контракту в Feature Specification
// ANCHOR: AUTH_SERVICE_LOGIN в коде имеет другие @PostConditions чем в спецификации
public AuthResponse login(AuthRequest request) {
    // ...
}
```

✅ Правильно:
```java
// [START_AUTH_SERVICE_LOGIN]
/*
 * ANCHOR: AUTH_SERVICE_LOGIN
 * PURPOSE: Аутентификация пользователя.
 *
 * @PreConditions:
 * - request валиден
 * - пользователь существует
 * - пароль верный
 *
 * @PostConditions:
 * - при успехе: возвращается AuthResponse с accessToken и refreshToken
 * - при ошибке: выбрасывается исключение аутентификации
 *
 * @Invariants:
 * - токены генерируются только для аутентифицированных пользователей
 *
 * @SideEffects:
 * - генерация JWT токенов
 *
 * @ForbiddenChanges:
 * - нельзя убрать аутентификацию через AuthenticationManager
 * - нельзя убрать генерацию токенов
 */
public AuthResponse login(AuthRequest request) {
    // ...
}
// [END_AUTH_SERVICE_LOGIN]
```

**Применимость**: При генерации кода для любой фичи.

**Дата добавления**: 2026-04-17
**Инцидент**: FEAT-001-Audit

---

## 7. Файловая структура

## 7. Файловая структура

*(Правила будут добавляться по мере выявления ошибок)*

---

## 8. Коммуникация

*(Правила будут добавляться по мере выявления ошибок)*

---

## Статистика

| Категория | Правил | Последнее обновление |
|-----------|--------|---------------------|
| Контракты | 0 | - |
| Логирование | 0 | - |
| Архитектура | 1 | 2026-04-16 |
| Код | 1 | 2026-04-16 |
| Данные | 0 | - |
| Тестирование | 1 | 2026-04-17 |
| Файлы | 0 | - |
| Коммуникация | 0 | - |
| **Всего** | **3** | **2026-04-17** |

---

## Шаблон для нового правила

```markdown
### RULE_[CATEGORY]_[NUMBER]: [Название]

**Проблема**: [Дата: YYYY-MM-DD] [Описание ошибки]

**Причина**: [Почему агент ошибся — анализ]

**Решение**: 
1. [Шаг 1]
2. [Шаг 2]

**Пример**:
❌ Неправильно:
```язык
# пример ошибки
```

✅ Правильно:
```язык
# как должно быть
```

**Применимость**: [Когда применять]

**Дата добавления**: YYYY-MM-DD
**Инцидент**: [Ссылка на коммит/PR/чат]
```
