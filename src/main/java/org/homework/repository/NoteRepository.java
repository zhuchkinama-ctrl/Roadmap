// === CHUNK: NOTE_REPOSITORY [PERSISTENCE] ===
// Описание: JPA-репозиторий для заметок.
// Dependencies: Spring Data JPA

package org.homework.repository;

import org.homework.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// [START_NOTE_REPOSITORY]
/*
 * ANCHOR: NOTE_REPOSITORY
 * PURPOSE: JPA-репозиторий для заметок.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - репозиторий готов для работы с БД
 *
 * @Invariants:
 * - все методы возвращают null если запись не найдена (для Optional)
 *
 * @SideEffects:
 * - чтение/запись в БД
 *
 * @ForbiddenChanges:
 * - нельзя изменить сигнатуру методов без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные query методы
 */
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    /**
     * Найти все заметки по треку.
     */
    List<Note> findByTrackId(Long trackId);
    
    /**
     * Найти корневые заметки (без родителя) по треку.
     */
    List<Note> findByTrackIdAndParentIsNull(Long trackId);
    
    /**
     * Найти дочерние заметки по родительской заметке.
     */
    List<Note> findByParentId(Long parentId);
    
    /**
     * Найти дочерние заметки по родительской заметке с сортировкой по orderIndex.
     */
    List<Note> findByParentIdOrderByOrderIndexAsc(Long parentId);
    
    /**
     * Найти все заметки по треку с сортировкой по orderIndex.
     */
    @Query("SELECT n FROM Note n WHERE n.track.id = :trackId ORDER BY n.orderIndex ASC")
    List<Note> findByTrackIdOrderByOrderIndex(@Param("trackId") Long trackId);
}
// [END_NOTE_REPOSITORY]
// === END_CHUNK: NOTE_REPOSITORY ===