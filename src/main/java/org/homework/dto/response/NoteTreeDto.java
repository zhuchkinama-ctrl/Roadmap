// === CHUNK: NOTE_TREE_DTO [DTO] ===
// Описание: DTO для заметки в дереве (с дочерними элементами).
// Dependencies: Lombok, Jackson

package org.homework.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

// [START_NOTE_TREE_DTO]
/*
 * ANCHOR: NOTE_TREE_DTO
 * PURPOSE: DTO для заметки в дереве (с дочерними элементами).
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - DTO готово для сериализации в JSON
 *
 * @Invariants:
 * - children всегда инициализирован (может быть пустым)
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя сделать id nullable
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, color, tags)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoteTreeDto {
    
    private Long id;
    
    private String title;
    
    private String content;
    
    private Integer orderIndex;

    private Boolean completed;

    private Instant createdAt;

    private Instant updatedAt;

    private List<NoteTreeDto> children;
}
// [END_NOTE_TREE_DTO]
// === END_CHUNK: NOTE_TREE_DTO ===
