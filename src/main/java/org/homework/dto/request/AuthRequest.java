// === CHUNK: AUTH_REQUEST_DTO [DTO] ===
// Описание: DTO для запроса аутентификации (логин).
// Dependencies: Lombok

package org.homework.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// [START_AUTH_REQUEST_DTO]
/*
 * ANCHOR: AUTH_REQUEST_DTO
 * PURPOSE: DTO для запроса аутентификации (логин).
 *
 * @PreConditions:
 * - username: непустая строка, 3-50 символов
 * - password: непустая строка, min 8 символов
 *
 * @PostConditions:
 * - DTO содержит username и password
 *
 * @Invariants:
 * - username и password не null после валидации
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя удалить поля username или password без согласования
 * - нельзя убрать валидацию полей
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, rememberMe)
 */
@Data
public class AuthRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
// [END_AUTH_REQUEST_DTO]
// === END_CHUNK: AUTH_REQUEST_DTO ===