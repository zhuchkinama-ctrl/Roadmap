package org.homework.repository;

import org.homework.model.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
    
    /**
     * Найти треки по владельцу с пагинацией.
     */
    Page<Track> findByOwnerId(Long ownerId, Pageable pageable);
    
    /**
     * Найти треки, к которым пользователь имеет доступ (через TrackPermission).
     */
    @Query("SELECT DISTINCT t FROM Track t " +
           "JOIN t.permissions p " +
           "WHERE p.user.id = :userId")
    Page<Track> findAccessibleTracks(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Найти трек по ID с загрузкой владельца.
     */
    @Query("SELECT t FROM Track t LEFT JOIN FETCH t.owner WHERE t.id = :id")
    Optional<Track> findByIdWithOwner(@Param("id") Long id);
    
    /**
     * Проверить, является ли пользователь владельцем трека.
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Track t " +
           "WHERE t.id = :trackId AND t.owner.id = :userId")
    boolean isOwner(@Param("trackId") Long trackId, @Param("userId") Long userId);
}
// [END_TRACK_REPOSITORY]
// === END_CHUNK: TRACK_REPOSITORY ===