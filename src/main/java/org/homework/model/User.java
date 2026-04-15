// === CHUNK: USER_ENTITY [MODEL] ===
// Описание: JPA-сущность пользователя.
// Dependencies: Jakarta Persistence, Lombok

package org.homework.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

// [START_USER_ENTITY]
/*
 * ANCHOR: USER_ENTITY
 * PURPOSE: JPA-сущность пользователя.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - сущность готова к использованию с JPA
 *
 * @Invariants:
 * - username и email уникальны
 * - пароль всегда хеширован
 * - createdAt не обновляется после создания
 *
 * @SideEffects:
 * - автоматическое обновление updatedAt при @PreUpdate
 *
 * @ForbiddenChanges:
 * - нельзя удалить уникальность username или email
 * - нельзя сделать createdAt обновляемым
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные поля (например, firstName, lastName)
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role = "USER";

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // [START_USER_ENTITY_ON_CREATE]
    /*
     * ANCHOR: USER_ENTITY_ON_CREATE
     * PURPOSE: Инициализация временных меток при создании.
     *
     * @PreConditions:
     * - сущность сохраняется в БД
     *
     * @PostConditions:
     * - createdAt и updatedAt установлены в текущее время
     *
     * @Invariants:
     * - createdAt устанавливается только один раз
     *
     * @SideEffects:
     * - установка временных меток
     *
     * @ForbiddenChanges:
     * - нельзя убрать установку createdAt
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные действия при создании
     */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    // [END_USER_ENTITY_ON_CREATE]

    // [START_USER_ENTITY_ON_UPDATE]
    /*
     * ANCHOR: USER_ENTITY_ON_UPDATE
     * PURPOSE: Обновление временной метки при изменении.
     *
     * @PreConditions:
     * - сущность обновляется в БД
     *
     * @PostConditions:
     * - updatedAt обновлён до текущего времени
     *
     * @Invariants:
     * - createdAt не изменяется
     *
     * @SideEffects:
     * - обновление временной метки
     *
     * @ForbiddenChanges:
     * - нельзя убрать обновление updatedAt
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные действия при обновлении
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    // [END_USER_ENTITY_ON_UPDATE]
}
// [END_USER_ENTITY]
// === END_CHUNK: USER_ENTITY ===