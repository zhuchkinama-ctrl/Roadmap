// === CHUNK: NOTE_ENTITY [MODEL] ===
// Описание: JPA-сущность заметки в треке.
// Dependencies: Jakarta Persistence, Lombok, Jackson

package org.homework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

// [START_NOTE_ENTITY]
/*
 * ANCHOR: NOTE_ENTITY
 * PURPOSE: JPA-сущность заметки в треке.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - сущность готова к использованию с JPA
 *
 * @Invariants:
 * - заметка всегда принадлежит треку (track)
 * - parent может быть null (корневая заметка)
 * - createdAt не обновляется после создания
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя сделать track nullable
 * - нельзя убрать @JsonIgnore от track и parent
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, color, tags)
 */
@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    @JsonIgnore
    private Track track;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Note parent;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(nullable = false)
    private Boolean completed = false;
}
// [END_NOTE_ENTITY]
// === END_CHUNK: NOTE_ENTITY ===