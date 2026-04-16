// === CHUNK: RESOURCE_NOT_FOUND_EXCEPTION [EXCEPTION] ===
// Описание: Исключение для ресурсов, не найденных в системе.
// Dependencies: Spring

package org.homework.exception;

// [START_RESOURCE_NOT_FOUND_EXCEPTION]
/*
 * ANCHOR: RESOURCE_NOT_FOUND_EXCEPTION
 * PURPOSE: Исключение для ресурсов, не найденных в системе.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - исключение готово к использованию
 *
 * @Invariants:
 * - сообщение всегда содержит описание отсутствующего ресурса
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя изменить наследование от RuntimeException
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, код ошибки)
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
// [END_RESOURCE_NOT_FOUND_EXCEPTION]
// === END_CHUNK: RESOURCE_NOT_FOUND_EXCEPTION ===
