package org.homework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

// === CHUNK: TRACK_PERMISSION_ENTITY [MODEL] ===
// Описание: JPA-сущность прав доступа к треку.
// Dependencies: Jakarta Persistence, Lombok, Jackson

// [START_TRACK_PERMISSION_ENTITY]
/*
 * ANCHOR: TRACK_PERMISSION_ENTITY
 * PURPOSE: JPA-сущность прав доступа к треку.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - сущность готова к использованию с JPA
 *
 * @Invariants:
 * - уникальная пара (track_id, user_id)
 * - permissionType может быть только VIEW или EDIT
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать уникальное ограничение (track_id, user_id)
 * - нельзя сделать track или user nullable
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, grantedBy)
 */
@Entity
@Table(name = "track_permissions", uniqueConstraints = {@UniqueConstraint(columnNames = {"track_id", "user_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    @JsonIgnore
    private Track track;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "permission_type", nullable = false)
    private String permissionType; // VIEW or EDIT

    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt = Instant.now();
}
// [END_TRACK_PERMISSION_ENTITY]
// === END_CHUNK: TRACK_PERMISSION_ENTITY ===