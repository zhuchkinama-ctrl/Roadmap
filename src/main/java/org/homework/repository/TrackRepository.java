package org.homework.repository;

import org.homework.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

// === CHUNK: TRACK_REPOSITORY [PERSISTENCE] ===
// Описание: JPA-репозиторий для работы с сущностью Track.
// Dependencies: Spring Data JPA

// [START_TRACK_REPOSITORY]
/*
 * ANCHOR: TRACK_REPOSITORY
 * PURPOSE: JPA-репозиторий для работы с сущностью Track.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - репозиторий предоставляет CRUD операции
 *
 * @Invariants:
 * - все операции транзакционны
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя удалить базовые CRUD операции
 *
 * @AllowedRefactorZone:
 * - можно добавить методы для поиска по владельцу
 */
public interface TrackRepository extends JpaRepository<Track, Long> {
    // Additional query methods can be defined here
}
// [END_TRACK_REPOSITORY]
// === END_CHUNK: TRACK_REPOSITORY ===