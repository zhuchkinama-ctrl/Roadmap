package org.homework.exception;

// === CHUNK: USER_ALREADY_EXISTS_EXCEPTION [EXCEPTION] ===
// Описание: Исключение при попытке регистрации с существующим username или email.
// Dependencies: (none)

// [START_USER_ALREADY_EXISTS_EXCEPTION]
/*
 * ANCHOR: USER_ALREADY_EXISTS_EXCEPTION
 * PURPOSE: Исключение при попытке регистрации с существующим username или email.
 *
 * @PreConditions:
 * - пользователь с таким username или email уже существует
 *
 * @PostConditions:
 * - исключение выбрасывается с описательным сообщением
 *
 * @Invariants:
 * - сообщение всегда содержит причину (username или email)
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя изменить тип исключения без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, field, value)
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
// [END_USER_ALREADY_EXISTS_EXCEPTION]
// === END_CHUNK: USER_ALREADY_EXISTS_EXCEPTION ===