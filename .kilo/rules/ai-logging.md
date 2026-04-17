# AI-friendly логирование

## Назначение

Логи в этом проекте предназначены не только для человека, но и для автоматического анализа ИИ-агентом. Логи должны помогать ИИ локализовать ошибку, понимать траекторию исполнения и восстанавливать смысл произошедшего без чтения всего кода.

---

## Ключевой принцип

**Логи — это интерфейс самокоррекции. Код проектируется под цикл: ожидание по контракту → выполнение → лог → сравнение ожидания и результата → исправление.**

---

## 1. Требования к логам для ИИ

### 1.1 Обязательные свойства логов

| Требование | Зачем |
|------------|-------|
| Маркировка типа записи (INFO/DEBUG/ERROR) | Быстрая фильтрация |
| Ссылка на ANCHOR | Связь с контрактом и кодом |
| Вход в критичные функции | Трассировка пути исполнения |
| Выход из критичных функций | Подтверждение завершения |
| Логирование условий и ветвлений | Понимание решений |
| Логирование причин отказа | Диагностика ошибок |
| Ключевые входные данные | Воспроизведение контекста |
| Ключевые результаты | Верификация постусловий |
| Идентификаторы сущностей | Трассировка сквозного потока |

### 1.2 Запрещённые практики

- **Пустые логи** — без диагностической информации
- **Декоративные логи** — "Processing...", "Done!" без контекста
- **Логи без anchor** — разрушают связь с контрактом
- **Логи с разными anchor в одном блоке** — нарушают трассировку

---

## 2. Формат логирования

### 2.1 Сигнатура функции логирования

**Java (SLF4J + MDC):**

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogUtil {
    public static void logLine(
        String module,      // Модуль/подсистема
        String level,       // "DEBUG", "INFO", "WARN", "ERROR"
        String functionName,// Имя метода
        String anchor,      // ANCHOR_ID из контракта
        String point,       // Точка: ENTRY, EXIT, BRANCH, DECISION, ERROR
        Map<String, Object> data // Контекстные данные
    ) {
        MDC.put("module", module);
        MDC.put("function", functionName);
        MDC.put("anchor", anchor);
        MDC.put("point", point);

        Logger logger = LoggerFactory.getLogger(module);
        String message = data != null ? data.toString() : "";

        switch (level) {
            case "DEBUG": logger.debug(message); break;
            case "INFO":  logger.info(message); break;
            case "WARN":  logger.warn(message); break;
            case "ERROR": logger.error(message); break;
        }

        MDC.clear();
    }
}
```

### 2.2 Точки логирования

| Point | Когда использовать |
|-------|-------------------|
| `ENTRY` | Вход в функцию |
| `EXIT` | Успешный выход из функции |
| `BRANCH` | Ветвление в логике |
| `DECISION` | Принятие решения |
| `CHECK` | Результат проверки |
| `ERROR` | Ошибка/отказ |
| `RETRY` | Повторная попытка |
| `STATE_CHANGE` | Изменение состояния |

---

## 3. Шаблон логирования в функции

### 3.1 Минимальный каркас

```java
// [START_PROCESS_ORDER]
/*
 * ANCHOR: PROCESS_ORDER
 * PURPOSE: Обработка заказа пользователя
 * ...
 */
public OrderResult processOrder(String orderId, String userId) {
    // ENTRY: фиксируем вход
    Map<String, Object> entryData = new HashMap<>();
    entryData.put("orderId", orderId);
    entryData.put("userId", userId);
    LogUtil.logLine("orders", "DEBUG", "processOrder", "PROCESS_ORDER", "ENTRY", entryData);

    // BRANCH: логируем ветвление
    if (orderId == null || orderId.isEmpty()) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("reason", "missing_order_id");
        LogUtil.logLine("orders", "WARN", "processOrder", "PROCESS_ORDER", "ERROR", errorData);

        Map<String, Object> exitData = new HashMap<>();
        exitData.put("result", "rejected");
        LogUtil.logLine("orders", "DEBUG", "processOrder", "PROCESS_ORDER", "EXIT", exitData);

        return new OrderResult(false, "MISSING_ORDER_ID", null);
    }

    // DECISION: логируем решение
    boolean isValid = validateOrder(orderId);
    Map<String, Object> checkData = new HashMap<>();
    checkData.put("check", "order_validity");
    checkData.put("result", isValid);
    LogUtil.logLine("orders", "DEBUG", "processOrder", "PROCESS_ORDER", "CHECK", checkData);

    if (!isValid) {
        Map<String, Object> decisionData = new HashMap<>();
        decisionData.put("decision", "reject_invalid_order");
        decisionData.put("orderId", orderId);
        LogUtil.logLine("orders", "WARN", "processOrder", "PROCESS_ORDER", "DECISION", decisionData);

        Map<String, Object> exitData = new HashMap<>();
        exitData.put("result", "rejected");
        exitData.put("reason", "invalid_order");
        LogUtil.logLine("orders", "DEBUG", "processOrder", "PROCESS_ORDER", "EXIT", exitData);

        return new OrderResult(false, "INVALID_ORDER", null);
    }

    // STATE_CHANGE: логируем изменение
    OrderData result = executeOrder(orderId);
    Map<String, Object> stateData = new HashMap<>();
    stateData.put("entity", "order");
    stateData.put("id", orderId);
    stateData.put("from", "pending");
    stateData.put("to", "processing");
    LogUtil.logLine("orders", "INFO", "processOrder", "PROCESS_ORDER", "STATE_CHANGE", stateData);

    // EXIT: фиксируем выход
    Map<String, Object> exitData = new HashMap<>();
    exitData.put("result", "success");
    exitData.put("orderId", orderId);
    LogUtil.logLine("orders", "DEBUG", "processOrder", "PROCESS_ORDER", "EXIT", exitData);

    return new OrderResult(true, null, result);
}
// [END_PROCESS_ORDER]
```

---

## 4. Детализация логирования

### 4.1 Уровни детализации по важности

| Уровень | Тип данных для логирования |
|---------|---------------------------|
| Критичный | Все входы/выходы, все решения, все ошибки |
| Важный | Входы/выходы, ключевые решения, ошибки |
| Обычный | Входы/выходы, ошибки |
| Вспомогательный | Только ошибки |

### 4.2 Критерии критичности блока

Блок считается критичным, если:
- Управляет деньгами/ресурсами
- Обеспечивает безопасность
- Является точкой интеграции
- Влияет на данные пользователя
- Выполняется редко (сложно воспроизвести)
- Имеет сложную условную логику

---

## 5. Логи для автоматического дебага

### 5.1 Информация для восстановления контекста

Лог должен содержать достаточно данных, чтобы ИИ мог:
1. Понять, какая ветка была выполнена
2. Понять, почему была выбрана эта ветка
3. Воспроизвести состояние на момент ошибки
4. Сопоставить с контрактом функции

### 5.2 Пример достаточного лог-блока

```java
// Хорошо: достаточно для автоматического анализа
Map<String, Object> errorData = new HashMap<>();
errorData.put("reason", "payment_failed");
errorData.put("orderId", "ORD-12345");
errorData.put("userId", "USR-67890");
errorData.put("amount", 1500.00);
errorData.put("currency", "RUB");
errorData.put("paymentProvider", "stripe");
errorData.put("errorCode", "INSUFFICIENT_FUNDS");
errorData.put("retryCount", 3);
errorData.put("maxRetries", 3);
errorData.put("suggestedAction", "notify_user");
LogUtil.logLine("orders", "ERROR", "processOrder", "PROCESS_ORDER", "ERROR", errorData);

// Плохо: недостаточно для анализа
Map<String, Object> badData = new HashMap<>();
badData.put("message", "Payment failed");
LogUtil.logLine("orders", "ERROR", "processOrder", "PROCESS_ORDER", "ERROR", badData);
```

---

## 6. Связка логов с контрактами

### 6.1 Проверка соответствия

Каждый лог уровня INFO/ERROR должен быть сопоставим с контрактом:

| Лог | Контракт |
|-----|----------|
| `EXIT { result: "success" }` | Соответствует постусловию успеха |
| `EXIT { result: "rejected" }` | Соответствует постусловию ошибки |
| `ERROR { reason: "..." }` | Объяснимо через предусловия |
| `STATE_CHANGE` | Отражён в `@SideEffects` |

### 6.2 Детектор расхождений

Если лог показывает поведение, не описанное в контракте — это признак:
1. Неполного контракта
2. Нарушения контракта
3. Требуется обновление контракта или реализация

---

## 7. Чеклист логирования

### 7.1 Перед сдачей кода

- [ ] Каждая критичная функция имеет ENTRY и EXIT логи
- [ ] Все ветвления имеют BRANCH или DECISION логи
- [ ] Все ошибки имеют ERROR логи с причиной
- [ ] Все логи содержат anchor, совпадающий с контрактом
- [ ] Данные в логах достаточны для воспроизведения контекста
- [ ] Нет декоративных логов без диагностической ценности
- [ ] Побочные эффекты отражены в логах (STATE_CHANGE)

### 7.2 Критерий готовности

Логирование считается готовым, если по логам ИИ может восстановить:
- Что произошло
- Почему произошло
- В каком месте кода произошло
- Какой был контекст
- Какой контракт был нарушен (если была ошибка)

---

## Эталон

- `.kilocode/rules/semantic-markup-examples/example-java.java`
