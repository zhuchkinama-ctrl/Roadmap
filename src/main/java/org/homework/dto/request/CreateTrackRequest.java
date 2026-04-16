// === CHUNK: CREATE_TRACK_REQUEST [DTO] ===
// Описание: DTO для создания трека развития.
// Dependencies: Jakarta Validation, Lombok

package org.homework.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// [START_CREATE_TRACK_REQUEST]
/*
 * ANCHOR: CREATE_TRACK_REQUEST
 * PURPOSE: DTO для создания трека развития.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - DTO готово для валидации через Bean Validation
 *
 * @Invariants:
 * - title всегда валиден после валидации
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать @NotBlank с title
 * - нельзя убрать @Size(max = 200) с title
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, color, icon)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTrackRequest {
    
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    private String description;
}
// [END_CREATE_TRACK_REQUEST]
// === END_CHUNK: CREATE_TRACK_REQUEST ===
