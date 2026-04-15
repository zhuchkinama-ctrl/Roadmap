// === CHUNK: AUTH_RESPONSE_DTO [DTO] ===
// Описание: DTO для ответа аутентификации с JWT токенами.
// Dependencies: Lombok

package org.homework.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// [START_AUTH_RESPONSE_DTO]
/*
 * ANCHOR: AUTH_RESPONSE_DTO
 * PURPOSE: DTO для ответа аутентификации с JWT токенами.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - DTO содержит accessToken и refreshToken
 *
 * @Invariants:
 * - токены генерируются только для аутентифицированных пользователей
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя удалить поля token или refreshToken без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, expiresIn, tokenType)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
}
// [END_AUTH_RESPONSE_DTO]
// === END_CHUNK: AUTH_RESPONSE_DTO ===