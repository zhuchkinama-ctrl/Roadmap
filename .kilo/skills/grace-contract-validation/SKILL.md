# GRACE Skill: Валидация контрактов

## Назначение

Этот скилл обеспечивает проверку соответствия кода контрактам, выявление расхождений и обеспечение соблюдения семантических ограничений.

---

## Ключевые принципы

1. **Контракт-первый** — код должен соответствовать контракту, не наоборот
2. **Автоматическая проверка** — все контракты проверяются автоматически
3. **Детальная диагностика** — каждое нарушение описывается с контекстом
4. **Обратная связь** — результаты валидации используются для улучшения

---

## Обязательные проверки

### 1. Проверка структуры контракта

Каждый контракт должен содержать:

- [ ] `ANCHOR` — уникальный идентификатор
- [ ] `PURPOSE` — назначение блока
- [ ] `@PreConditions` — предусловия
- [ ] `@PostConditions` — постусловия
- [ ] `@Invariants` — инварианты
- [ ] `@SideEffects` — побочные эффекты
- [ ] `@ForbiddenChanges` — запреты на изменения

### 2. Проверка соответствия якорей

- [ ] `ANCHOR_ID` в `// [START_...]` совпадает с контрактом
- [ ] `ANCHOR_ID` в `// [END_...]` совпадает с контрактом
- [ ] `anchor` в `logLine()` совпадает с контрактом
- [ ] Нет дубликатов `ANCHOR_ID` в одном файле

### 3. Проверка соблюдения контракта

#### Предусловия (@PreConditions)

Проверить, что код:
- [ ] Проверяет все предусловия перед выполнением
- [ ] Возвращает ошибку или выбрасывает исключение при нарушении
- [ ] Логирует проверку предусловий

#### Постусловия (@PostConditions)

Проверить, что код:
- [ ] Гарантирует выполнение постусловий при успехе
- [ ] Гарантирует выполнение постусловий при ошибке
- [ ] Логирует результат выполнения

#### Инварианты (@Invariants)

Проверить, что код:
- [ ] Не нарушает инварианты ни при каких условиях
- [ ] Проверяет инварианты после изменений состояния
- [ ] Логирует проверки инвариантов

#### Побочные эффекты (@SideEffects)

Проверить, что код:
- [ ] Выполняет все указанные побочные эффекты
- [ ] Не выполняет неуказанные побочные эффекты
- [ ] Логирует побочные эффекты (STATE_CHANGE)

#### Запреты (@ForbiddenChanges)

Проверить, что код:
- [ ] Не нарушает указанные запреты
- [ ] Сохраняет критичную логику
- [ ] Не меняет семантику без явного согласования

---

## Процесс валидации

### Шаг 1: Сбор контрактов

```bash
# Найти все контракты в проекте
find . -name "*.java" -o -name "*.ts" -o -name "*.py" | \
  xargs grep -l "ANCHOR:" | \
  sort
```

### Шаг 2: Парсинг контрактов

Для каждого файла с контрактом:
1. Извлечь все блоки `[START_...]` ... `[END_...]`
2. Извлечь контракты внутри блоков
3. Проверить соответствие якорей

### Шаг 3: Анализ кода

Для каждого блока с контрактом:
1. Проверить наличие ENTRY/EXIT логов
2. Проверить логирование ветвлений
3. Проверить логирование ошибок
4. Проверить соблюдение предусловий
5. Проверить обеспечение постусловий
6. Проверить сохранение инвариантов
7. Проверить побочные эффекты
8. Проверить соблюдение запретов

### Шаг 4: Генерация отчёта

Создать отчёт в формате:

```markdown
# Contract Validation Report

## Summary
- Total contracts: N
- Valid: M
- Invalid: K
- Warnings: L

## Issues

### 1. Missing ENTRY log
**File**: `src/main/java/org/homework/service/AuthService.java`
**Anchor**: `AUTH_LOGIN`
**Line**: 45
**Severity**: ERROR
**Description**: Function has no ENTRY log

### 2. Invariant violation
**File**: `src/main/java/org/homework/service/TrackService.java`
**Anchor**: `CREATE_TRACK`
**Line**: 78
**Severity**: ERROR
**Description**: Invariant "track always has owner" is violated

### 3. Missing postcondition
**File**: `src/main/java/org/homework/service/NoteService.java`
**Anchor**: `UPDATE_NOTE`
**Line**: 120
**Severity**: WARNING
**Description**: Postcondition "note updated_at is set" is not guaranteed
```

---

## Шаблон отчёта валидации

### XML формат

```xml
<?xml version="1.0" encoding="UTF-8"?>
<validation-report>
  <metadata>
    <timestamp>2026-04-17T06:30:00Z</timestamp>
    <project>TrackHub</project>
    <version>1.0.0</version>
  </metadata>
  
  <summary>
    <total-contracts>42</total-contracts>
    <valid>38</valid>
    <invalid>4</invalid>
    <warnings>7</warnings>
  </summary>
  
  <issues>
    <issue id="1" severity="ERROR">
      <type>MISSING_ENTRY_LOG</type>
      <file>src/main/java/org/homework/service/AuthService.java</file>
      <anchor>AUTH_LOGIN</anchor>
      <line>45</line>
      <description>Function has no ENTRY log</description>
      <suggestion>Add logLine("auth", "DEBUG", "login", "AUTH_LOGIN", "ENTRY", {...}) at the beginning</suggestion>
    </issue>
    
    <issue id="2" severity="ERROR">
      <type>INVARIANT_VIOLATION</type>
      <file>src/main/java/org/homework/service/TrackService.java</file>
      <anchor>CREATE_TRACK</anchor>
      <line>78</line>
      <description>Invariant "track always has owner" is violated</description>
      <suggestion>Ensure track.owner is set before saving</suggestion>
    </issue>
  </issues>
</validation-report>
```

---

## Критерии валидации

### Уровни серьёзности

| Уровень | Описание | Пример |
|---------|----------|--------|
| `ERROR` | Критичное нарушение контракта | Отсутствие ENTRY/EXIT логов, нарушение инвариантов |
| `WARNING` | Потенциальная проблема | Неоптимальное логирование, неполные проверки |
| `INFO` | Информационное сообщение | Рекомендации по улучшению |

### Правила валидации

#### Правило 1: Обязательные логи

**Правило**: Каждая критичная функция должна иметь ENTRY и EXIT логи.

**Проверка**:
```java
// Плохо: нет логов
public Result process(Input input) {
    return doSomething(input);
}

// Хорошо: есть логи
public Result process(Input input) {
    log.info("PROCESS ENTRY - input: {}", input);
    Result result = doSomething(input);
    log.info("PROCESS EXIT - result: {}", result);
    return result;
}
```

#### Правило 2: Соответствие якорей

**Правило**: ANCHOR_ID должен быть одинаковым в START, контракте, END и logLine.

**Проверка**:
```java
// Плохо: разные якоря
// [START_PROCESS]
/*
 * ANCHOR: PROCESS_ORDER
 */
public void process() {
    logLine("module", "DEBUG", "process", "PROCESS_ITEM", "ENTRY", {});
}
// [END_PROCESS]

// Хорошо: одинаковые якоря
// [START_PROCESS_ORDER]
/*
 * ANCHOR: PROCESS_ORDER
 */
public void process() {
    logLine("module", "DEBUG", "process", "PROCESS_ORDER", "ENTRY", {});
}
// [END_PROCESS_ORDER]
```

#### Правило 3: Проверка предусловий

**Правило**: Все предусловия должны быть проверены до выполнения логики.

**Проверка**:
```java
// Плохо: нет проверки предусловий
public void process(String id) {
    // сразу выполняем логику
    doSomething(id);
}

// Хорошо: есть проверка предусловий
public void process(String id) {
    if (id == null || id.isEmpty()) {
        log.warn("PROCESS DECISION - reject_empty_id");
        throw new IllegalArgumentException("ID cannot be empty");
    }
    doSomething(id);
}
```

#### Правило 4: Обеспечение постусловий

**Правило**: Постусловия должны быть гарантированы при всех путях выполнения.

**Проверка**:
```java
// Плохо: постусловие не гарантировано
public Result process() {
    if (condition) {
        return new Result("success");
    }
    // забыли вернуть результат
}

// Хорошо: постусловие гарантировано
public Result process() {
    if (condition) {
        return new Result("success");
    }
    return new Result("error");
}
```

#### Правило 5: Сохранение инвариантов

**Правило**: Инварианты не должны нарушаться ни при каких условиях.

**Проверка**:
```java
// Плохо: инвариант нарушается
public void updateBalance(Account account, double amount) {
    account.setBalance(account.getBalance() + amount);
    // баланс может стать отрицательным
}

// Хорошо: инвариант сохраняется
public void updateBalance(Account account, double amount) {
    double newBalance = account.getBalance() + amount;
    if (newBalance < 0) {
        throw new IllegalArgumentException("Balance cannot be negative");
    }
    account.setBalance(newBalance);
}
```

---

## Интеграция с CI/CD

### GitHub Actions

```yaml
name: Contract Validation

on: [push, pull_request]

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Validate contracts
        run: |
          python .kilocode/scripts/validate_contracts.py
      - name: Upload report
        uses: actions/upload-artifact@v2
        with:
          name: validation-report
          path: reports/contract-validation-report.xml
```

### Maven (Backend)

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-antrun-plugin</artifactId>
  <executions>
    <execution>
      <phase>test</phase>
      <configuration>
        <target>
          <exec executable="python">
            <arg value=".kilocode/scripts/validate_contracts.py"/>
          </exec>
        </target>
      </configuration>
      <goals>
        <goal>run</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

### npm (Frontend)

```json
{
  "scripts": {
    "validate-contracts": "node .kilocode/scripts/validate-contracts.js"
  }
}
```

---

## Чеклист валидации

### Перед коммитом

- [ ] Все новые функции имеют контракты
- [ ] Все контракты имеют обязательные поля
- [ ] Все якоря соответствуют друг другу
- [ ] Все критичные функции имеют ENTRY/EXIT логи
- [ ] Все предусловия проверены
- [ ] Все постусловия обеспечены
- [ ] Все инварианты сохранены
- [ ] Все побочные эффекты логированы
- [ ] Все запреты соблюдены

### Критерий готовности

Код считается готовым к коммиту, если:
- Нет ошибок уровня ERROR
- Предупреждения уровня WARNING обоснованы
- Все контракты соответствуют коду

---

## Связанные документы

- [Semantic Markup Rules](../rules/semantic-code-markup.md) — правила разметки
- [AI Logging Rules](../rules/ai-logging.md) — правила логирования
- [Error Patterns](../rules/error-patterns.md) — паттерны ошибок

---

*Создано: 2026-04-17*
*GRACE Skill: Contract Validation*
