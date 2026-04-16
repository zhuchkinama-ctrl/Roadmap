// === CHUNK: TRACK_ENTITY [MODEL] ===
// Описание: JPA-сущность трека развития.
// Dependencies: Jakarta Persistence, Lombok, Jackson

package org.homework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// [START_TRACK_ENTITY]
/*
 * ANCHOR: TRACK_ENTITY
 * PURPOSE: JPA-сущность трека развития.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - сущность готова к использованию с JPA
 *
 * @Invariants:
 * - трек всегда имеет владельца (owner)
 * - createdAt не обновляется после создания
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя сделать owner nullable
 * - нельзя убрать @JsonIgnore от owner
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, color, icon)
 */
@Entity
@Table(name = "tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnore
    private User owner;
    
    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TrackPermission> permissions = new ArrayList<>();
    
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}
// [END_TRACK_ENTITY]
// === END_CHUNK: TRACK_ENTITY ===