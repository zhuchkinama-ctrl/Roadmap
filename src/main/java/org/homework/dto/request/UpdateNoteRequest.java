// === CHUNK: UPDATE_NOTE_REQUEST [DTO] ===
// Описание: DTO для обновления заметки.
// Dependencies: Jakarta Validation, Lombok

package org.homework.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// [START_UPDATE_NOTE_REQUEST]
/*
 * ANCHOR: UPDATE_NOTE_REQUEST
 * PURPOSE: DTO для обновления заметки в треке.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - DTO готово для валидации и передачи в сервис
 *
 * @Invariants:
 * - title всегда непустой и не превышает 200 символов
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя сделать title nullable
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, color, tags)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNoteRequest {
    
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;
    
    private String content;
    
    private Long parentId;
    
    private Integer orderIndex;
}
// [END_UPDATE_NOTE_REQUEST]
// === END_CHUNK: UPDATE_NOTE_REQUEST ===
