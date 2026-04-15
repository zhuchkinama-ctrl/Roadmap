// === CHUNK: AUTH_REQUEST_DTO [DTO] ===
// Описание: DTO для запроса аутентификации (логин).
// Dependencies: Lombok

package org.homework.dto.request;

import lombok.Data;

// [START_AUTH_REQUEST_DTO]
/*
 * ANCHOR: AUTH_REQUEST_DTO
 * PURPOSE: DTO для запроса аутентификации (логин).
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - DTO содержит username и password
 *
 * @Invariants:
 * - username и password могут быть null (валидация на уровне контроллера)
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя удалить поля username или password без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, rememberMe)
 */
@Data
public class AuthRequest {
    private String username;
    private String password;
}
// [END_AUTH_REQUEST_DTO]
// === END_CHUNK: AUTH_REQUEST_DTO ===