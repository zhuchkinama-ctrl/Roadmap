# GRACE Skill: Верификация

## Назначение

Этот скилл обеспечивает комплексную верификацию кода, включая прогон тестов, проверку log-маркеров и генерацию отчётов о верификации.

---

## Ключевые принципы

1. **3 уровня верификации** — детерминированные тесты, тесты траектории, интеграционные тесты
2. **Проверка контрактов** — верификация соответствия кода контрактам
3. **Проверка логов** — верификация наличия и корректности log-маркеров
4. **Автоматизация** — все проверки автоматизированы
5. **Отчётность** — детальные отчёты о результатах верификации

---

## Процесс верификации

### Шаг 1: Запуск детерминированных тестов (Level 1)

**Назначение**: Проверка постусловий контрактов.

```bash
# Backend
mvn test

# Frontend
npm test
```

**Проверяем**:
- Все тесты проходят
- Покрытие кода ≥ 80%
- Нет flaky тестов

### Шаг 2: Запуск тестов траектории (Level 2)

**Назначение**: Проверка log-маркеров.

```bash
# Backend
mvn test -Dtest=*MarkersTest

# Frontend
npm test -- --include="**/*markers.spec.ts"
```

**Проверяем**:
- ENTRY логи присутствуют
- EXIT логи присутствуют
- BRANCH/DECISION логи присутствуют
- ERROR логи присутствуют
- STATE_CHANGE логи присутствуют

### Шаг 3: Запуск интеграционных тестов (Level 3)

**Назначение**: Проверка E2E сценариев.

```bash
# Backend
mvn verify

# Frontend
npm run e2e
```

**Проверяем**:
- Сквозной поток работает
- API эндпоинты работают
- UI сценарии работают

### Шаг 4: Валидация контрактов

**Назначение**: Проверка соответствия кода контрактам.

```bash
python .kilocode/scripts/validate_contracts.py
```

**Проверяем**:
- Все контракты имеют обязательные поля
- Все якоря соответствуют друг другу
- Код соответствует контракту

### Шаг 5: Генерация отчёта

**Назначение**: Создание детального отчёта о верификации.

```bash
python .kilocode/scripts/generate_verification_report.py
```

---

## Шаблон отчёта верификации

### Markdown формат

```markdown
# Verification Report

## Metadata
- **Date**: 2026-04-17T06:30:00Z
- **Feature**: FEAT-001 User Authentication
- **Commit**: abc123def456

## Summary
- **Total Tests**: 42
- **Passed**: 38
- **Failed**: 4
- **Skipped**: 0
- **Coverage**: 85%

## Level 1: Deterministic Tests
- **Total**: 20
- **Passed**: 18
- **Failed**: 2

### Failed Tests
1. `testRegisterWithInvalidEmail` - Expected ValidationException, got UserAlreadyExistsException
2. `testLoginWithWrongPassword` - Expected 401, got 500

## Level 2: Trajectory Tests
- **Total**: 15
- **Passed**: 15
- **Failed**: 0

### Log Markers Verification
- ✅ AUTH_REGISTER ENTRY
- ✅ AUTH_REGISTER EXIT
- ✅ AUTH_REGISTER DECISION
- ✅ AUTH_REGISTER ERROR
- ✅ AUTH_REGISTER STATE_CHANGE

## Level 3: Integration Tests
- **Total**: 7
- **Passed**: 5
- **Failed**: 2

### Failed Tests
1. `testE2E_RegisterAndLogin` - Registration succeeds, but login fails
2. `testE2E_AccessProtectedEndpoint` - Token validation fails

## Contract Validation
- **Total Contracts**: 10
- **Valid**: 9
- **Invalid**: 1

### Invalid Contracts
1. `AUTH_LOGIN` - Missing @Invariants field

## Issues

### Critical Issues
1. **[HIGH]** testRegisterWithInvalidEmail fails - Wrong exception type
   - **File**: `src/test/java/org/homework/service/AuthServiceTest.java`
   - **Line**: 45
   - **Fix**: Update test to expect UserAlreadyExistsException

2. **[HIGH]** testE2E_RegisterAndLogin fails - Login after registration fails
   - **File**: `src/test/java/org/homework/integration/AuthIntegrationTest.java`
   - **Line**: 78
   - **Fix**: Investigate token generation

### Warnings
1. **[MEDIUM]** AUTH_LOGIN contract missing @Invariants
   - **File**: `src/main/java/org/homework/service/AuthService.java`
   - **Line**: 23
   - **Fix**: Add @Invariants field to contract

## Recommendations
1. Fix critical issues before merging
2. Add missing @Invariants to AUTH_LOGIN contract
3. Increase test coverage to 90%
4. Add more integration tests for edge cases

## Conclusion
**Status**: FAILED
**Reason**: 2 critical issues found
**Action Required**: Fix issues and re-run verification
```

### XML формат

```xml
<?xml version="1.0" encoding="UTF-8"?>
<verification-report>
  <metadata>
    <timestamp>2026-04-17T06:30:00Z</timestamp>
    <feature>FEAT-001</feature>
    <commit>abc123def456</commit>
  </metadata>
  
  <summary>
    <total-tests>42</total-tests>
    <passed>38</passed>
    <failed>4</failed>
    <skipped>0</skipped>
    <coverage>85</coverage>
  </summary>
  
  <level-1>
    <name>Deterministic Tests</name>
    <total>20</total>
    <passed>18</passed>
    <failed>2</failed>
    
    <failed-tests>
      <test>
        <name>testRegisterWithInvalidEmail</name>
        <reason>Expected ValidationException, got UserAlreadyExistsException</reason>
        <file>src/test/java/org/homework/service/AuthServiceTest.java</file>
        <line>45</line>
      </test>
    </failed-tests>
  </level-1>
  
  <level-2>
    <name>Trajectory Tests</name>
    <total>15</total>
    <passed>15</passed>
    <failed>0</failed>
    
    <log-markers>
      <marker name="AUTH_REGISTER ENTRY" status="PASS"/>
      <marker name="AUTH_REGISTER EXIT" status="PASS"/>
      <marker name="AUTH_REGISTER DECISION" status="PASS"/>
      <marker name="AUTH_REGISTER ERROR" status="PASS"/>
      <marker name="AUTH_REGISTER STATE_CHANGE" status="PASS"/>
    </log-markers>
  </level-2>
  
  <level-3>
    <name>Integration Tests</name>
    <total>7</total>
    <passed>5</passed>
    <failed>2</failed>
  </level-3>
  
  <contract-validation>
    <total>10</total>
    <valid>9</valid>
    <invalid>1</invalid>
  </contract-validation>
  
  <issues>
    <issue id="1" severity="HIGH">
      <type>TEST_FAILURE</type>
      <description>testRegisterWithInvalidEmail fails</description>
      <file>src/test/java/org/homework/service/AuthServiceTest.java</file>
      <line>45</line>
      <fix>Update test to expect UserAlreadyExistsException</fix>
    </issue>
  </issues>
  
  <conclusion>
    <status>FAILED</status>
    <reason>2 critical issues found</reason>
    <action-required>Fix issues and re-run verification</action-required>
  </conclusion>
</verification-report>
```

---

## Критерии верификации

### Уровни серьёзности

| Уровень | Описание | Действие |
|---------|----------|----------|
| `CRITICAL` | Критичная ошибка, блокирующая релиз | Необходимо исправить |
| `HIGH` | Серьёзная ошибка, влияющая на функционал | Необходимо исправить |
| `MEDIUM` | Средняя ошибка, не блокирующая | Желательно исправить |
| `LOW` | Низкая серьёзность, рекомендация | Можно отложить |

### Правила верификации

#### Правило 1: Все тесты должны проходить

**Правило**: Все тесты должны проходить без ошибок.

**Проверка**:
```bash
mvn test
# Exit code: 0 (success)
```

#### Правило 2: Покрытие кода ≥ 80%

**Правило**: Покрытие кода тестами должно быть не менее 80%.

**Проверка**:
```bash
mvn jacoco:report
# Check coverage in target/site/jacoco/index.html
```

#### Правило 3: Все log-маркеры присутствуют

**Правило**: Все критичные функции должны иметь ENTRY и EXIT логи.

**Проверка**:
```bash
python .kilocode/scripts/check_log_markers.py
# All markers present
```

#### Правило 4: Контракты валидны

**Правило**: Все контракты должны иметь обязательные поля.

**Проверка**:
```bash
python .kilocode/scripts/validate_contracts.py
# All contracts valid
```

---

## Интеграция с CI/CD

### GitHub Actions

```yaml
name: Verification

on: [push, pull_request]

jobs:
  verify:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run Level 1 tests
        run: mvn test
      
      - name: Run Level 2 tests
        run: mvn test -Dtest=*MarkersTest
      
      - name: Run Level 3 tests
        run: mvn verify
      
      - name: Validate contracts
        run: |
          python .kilocode/scripts/validate_contracts.py
      
      - name: Generate verification report
        run: |
          python .kilocode/scripts/generate_verification_report.py
      
      - name: Upload verification report
        uses: actions/upload-artifact@v2
        with:
          name: verification-report
          path: reports/verification-report.xml
```

---

## Чеклист верификации

### Перед запуском

- [ ] Все тесты написаны
- [ ] Все контракты созданы
- [ ] Все log-маркеры добавлены
- [ ] Код скомпилирован

### При запуске

- [ ] Level 1 тесты запущены
- [ ] Level 2 тесты запущены
- [ ] Level 3 тесты запущены
- [ ] Контракты валидированы
- [ ] Отчёт сгенерирован

### После запуска

- [ ] Все тесты проходят
- [ ] Покрытие ≥ 80%
- [ ] Все log-маркеры присутствуют
- [ ] Все контракты валидны
- [ ] Нет критичных ошибок

### Критерий готовности

Верификация считается успешной, если:
- Все тесты проходят
- Покрытие ≥ 80%
- Все log-маркеры присутствуют
- Все контракты валидны
- Нет критичных ошибок

---

## Специфика TrackHub

### Backend (Java 17 + Spring Boot)

- **Тесты**: JUnit 5 + Spring Test
- **Покрытие**: JaCoCo
- **Интеграционные тесты**: @SpringBootTest + @AutoConfigureMockMvc
- **Логирование**: Logback с ListAppender для тестов

### Frontend (Angular 17 + TypeScript)

- **Тесты**: Jasmine + Karma
- **Покрытие**: Istanbul
- **E2E тесты**: Cypress
- **Логирование**: spyOn для проверки logLine

---

## Связанные документы

- [Test Generation Skill](./grace-test-generation/SKILL.md) — генерация тестов
- [Contract Validation Skill](./grace-contract-validation/SKILL.md) — валидация контрактов
- [AI Logging Rules](../rules/ai-logging.md) — правила логирования

---

*Создано: 2026-04-17*
*GRACE Skill: Verification*
