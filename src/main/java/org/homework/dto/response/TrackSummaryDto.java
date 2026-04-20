// === CHUNK: TRACK_SUMMARY_DTO [DTO] ===
// Описание: DTO для краткой информации о треке (используется в списках).
// Dependencies: Lombok

package org.homework.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// [START_TRACK_SUMMARY_DTO]
/*
 * ANCHOR: TRACK_SUMMARY_DTO
 * PURPOSE: DTO для краткой информации о треке (используется в списках).
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
public class TrackSummaryDto {
    private Long id;
    private String title;
    private String description;
    private UserDto owner;
    private String myRole; // OWNER/VIEW/EDIT
    private Instant updatedAt;
}
// [END_TRACK_SUMMARY_DTO]
// === END_CHUNK: TRACK_SUMMARY_DTO ===
