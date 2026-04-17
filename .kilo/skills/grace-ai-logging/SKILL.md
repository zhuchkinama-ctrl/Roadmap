# GRACE Skill: AI-friendly логирование

## Назначение

Этот скилл обеспечивает создание структурированных логов, которые помогают ИИ-агенту локализовать ошибки, понимать траекторию исполнения и восстанавливать смысл произошедшего без чтения всего кода.

---

## Ключевой принцип

**Логи — это интерфейс самокоррекции. Код проектируется под цикл: ожидание по контракту → выполнение → лог → сравнение ожидания и результата → исправление.**

---

## Обязательные свойства логов

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

---

## Запрещённые практики

- **Пустые логи** — без диагностической информации
- **Декоративные логи** — "Processing...", "Done!" без контекста
- **Логи без anchor** — разрушают связь с контрактом
- **Логи с разными anchor в одном блоке** — нарушают трассировку

---

## Формат логирования

### Сигнатура функции логирования

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

**TypeScript:**

```typescript
export function logLine(
  module: string,
  level: "DEBUG" | "INFO" | "WARN" | "ERROR",
  functionName: string,
  anchor: string,
  point: string,
  data: Record<string, unknown>
): void {
  const timestamp = new Date().toISOString();
  const logEntry = {
    timestamp,
    module,
    level,
    function: functionName,
    anchor,
    point,
    data
  };
  
  console.log(JSON.stringify(logEntry));
}
```

**Python:**

```python
import logging
from typing import Dict, Any

def log_line(
    module: str,
    level: str,
    function_name: str,
    anchor: str,
    point: str,
    data: Dict[str, Any]
) -> None:
    logger = logging.getLogger(module)
    log_data = {
        "module": module,
        "function": function_name,
        "anchor": anchor,
        "point": point,
        "data": data
    }
    
    if level == "DEBUG":
        logger.debug(log_data)
    elif level == "INFO":
        logger.info(log_data)
    elif level == "WARN":
        logger.warning(log_data)
    elif level == "ERROR":
        logger.error(log_data)
```

---

## Точки логирования

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

## Шаблон логирования в функции

### Минимальный каркас

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

## Детализация логирования

### Уровни детализации по важности

| Уровень | Тип данных для логирования |
|---------|---------------------------|
| Критичный | Все входы/выходы, все решения, все ошибки |
| Важный | Входы/выходы, ключевые решения, ошибки |
| Обычный | Входы/выходы, ошибки |
| Вспомогательный | Только ошибки |

### Критерии критичности блока

Блок считается критичным, если:
- Управляет деньгами/ресурсами
- Обеспечивает безопасность
- Является точкой интеграции
- Влияет на данные пользователя
- Выполняется редко (сложно воспроизвести)
- Имеет сложную условную логику

---

## Логи для автоматического дебага

### Информация для восстановления контекста

Лог должен содержать достаточно данных, чтобы ИИ мог:
1. Понять, какая ветка была выполнена
2. Понять, почему была выбрана эта ветка
3. Воспроизвести состояние на момент ошибки
4. Сопоставить с контрактом функции

### Пример достаточного лог-блока

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

## Связка логов с контрактами

### Проверка соответствия

Каждый лог уровня INFO/ERROR должен быть сопоставим с контрактом:

| Лог | Контракт |
|-----|----------|
| `EXIT { result: "success" }` | Соответствует постусловию успеха |
| `EXIT { result: "rejected" }` | Соответствует постусловию ошибки |
| `ERROR { reason: "..." }` | Объяснимо через предусловия |
| `STATE_CHANGE` | Отражён в `@SideEffects` |

### Детектор расхождений

Если лог показывает поведение, не описанное в контракте — это признак:
1. Неполного контракта
2. Нарушения контракта
3. Требуется обновление контракта или реализация

---

## Чеклист логирования

### Перед сдачей кода

- [ ] Каждая критичная функция имеет ENTRY и EXIT логи
- [ ] Все ветвления имеют BRANCH или DECISION логи
- [ ] Все ошибки имеют ERROR логи с причиной
- [ ] Все логи содержат anchor, совпадающий с контрактом
- [ ] Данные в логах достаточны для воспроизведения контекста
- [ ] Нет декоративных логов без диагностической ценности
- [ ] Побочные эффекты отражены в логах (STATE_CHANGE)

### Критерий готовности

Логирование считается готовым, если по логам ИИ может восстановить:
- Что произошло
- Почему произошло
- В каком месте кода произошло
- Какой был контекст
- Какой контракт был нарушен (если была ошибка)

---

## Специфика TrackHub

### Backend (Java 17 + Spring Boot)

- **Логирование**: SLF4J + Logback с MDC для структурированных логов
- **Уровни**: DEBUG для детализации, INFO для важных событий, WARN для предупреждений, ERROR для ошибок
- **Формат**: JSON-структурированные логи с полями module, function, anchor, point, data

### Frontend (Angular 17 + TypeScript)

- **Логирование**: кастомная функция `logLine` из `core/lib/log.ts`
- **Уровни**: DEBUG, INFO, WARN, ERROR
- **Формат**: JSON-структурированные логи в консоль браузера

---

## Связанные документы

- [AI Logging Rules](../rules/ai-logging.md) — подробные правила логирования
- [Semantic Markup Rules](../rules/semantic-code-markup.md) — правила разметки
- [Примеры](../rules/semantic-markup-examples/) — эталонные примеры

---

*Создано: 2026-04-17*
*GRACE Skill: AI-friendly Logging*
