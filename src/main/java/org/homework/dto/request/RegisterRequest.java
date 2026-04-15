// === CHUNK: REGISTER_REQUEST_DTO [DTO] ===
// Описание: DTO для запроса регистрации пользователя.
// Dependencies: Lombok, Jakarta Validation

package org.homework.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

// [START_REGISTER_REQUEST_DTO]
/*
 * ANCHOR: REGISTER_REQUEST_DTO
 * PURPOSE: DTO для запроса регистрации пользователя.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - DTO содержит username, email и password
 *
 * @Invariants:
 * - все поля проходят валидацию Bean Validation
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя удалить валидацию @NotBlank, @Size, @Pattern без согласования
 * - нельзя ослабить требования к паролю (минимум 8 символов, цифра, заглавная, спецсимвол)
 *
 * @AllowedRefactorZone:
 * - можно изменить ограничения валидации (например, увеличить минимальную длину)
 * - можно добавить дополнительные поля (например, confirmPassword)
 */
@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    private String username;
    
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;
    
    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*]).+$",
            message = "Password must contain at least one digit, one uppercase letter and one special character")
    private String password;
}
// [END_REGISTER_REQUEST_DTO]
// === END_CHUNK: REGISTER_REQUEST_DTO ===