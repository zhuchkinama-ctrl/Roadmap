/**
 * Пример семантической разметки для Java сервиса.
 * Файл находится в каталоге .kilocode/rules/semantic-markup-examples и служит справочником.
 */

package org.homework.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// === CHUNK: EXAMPLE_JAVA_SERVICE [SERVICE] ===
// Описание: Простой сервис, возвращающий список задач.
// Dependencies: Spring Framework, SLF4J

// [START_EXAMPLE_JAVA_SERVICE]
/*
 * ANCHOR: EXAMPLE_JAVA_SERVICE
 * PURPOSE: Демонстрация семантической разметки в Java сервисе.
 *
 * @PreConditions:
 * - Spring контекст инициализирован
 * - все зависимости внедрены
 *
 * @PostConditions:
 * - метод getTasks() возвращает непустой список задач
 * - в лог записан ENTRY и EXIT
 *
 * @Invariants:
 * - список задач всегда неизменяемый после возврата
 * - сервис не меняет глобальное состояние приложения
 *
 * @SideEffects:
 * - запись в журнал через logger (нет побочных эффектов в бизнес‑логике)
 *
 * @ForbiddenChanges:
 * - нельзя удалить вызов logger.info, т.к. он фиксирует поведение
 * - нельзя изменить структуру ANCHOR, иначе нарушится связь с графом
 *
 * @AllowedRefactorZone:
 * - можно изменить способ получения задач (например, заменить статический список на репозиторий)
 */
public class ExampleJavaService {
    private static final Logger logger = LoggerFactory.getLogger(ExampleJavaService.class);

    public java.util.List<String> getTasks() {
        logger.info("EXAMPLE_JAVA_SERVICE ENTRY");
        // Симуляция получения данных
        java.util.List<String> tasks = java.util.Arrays.asList("Задача 1", "Задача 2");
        logger.info("EXAMPLE_JAVA_SERVICE EXIT", java.util.Collections.singletonMap("loaded", true));
        return tasks;
    }
}
// [END_EXAMPLE_JAVA_SERVICE]
// === END_CHUNK: EXAMPLE_JAVA_SERVICE ===