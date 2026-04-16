// === CHUNK: TRACK_DTO [DTO] ===
// Описание: DTO для ответа с информацией о треке.
// Dependencies: Lombok

package org.homework.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// [START_TRACK_DTO]
/*
 * ANCHOR: TRACK_DTO
 * PURPOSE: DTO для ответа с информацией о треке.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - DTO готово для сериализации в JSON
 *
 * @Invariants:
 * - id всегда присутствует для существующего трека
 * - myRole всегда присутствует (OWNER/VIEW/EDIT)
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать поле myRole
 * - нельзя изменить тип myRole на не-String
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, color, icon)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackDto {
    private Long id;
    private String title;
    private String description;
    private UserDto owner;
    private String myRole; // OWNER/VIEW/EDIT
    private Instant createdAt;
    private Instant updatedAt;
}
// [END_TRACK_DTO]
// === END_CHUNK: TRACK_DTO ===