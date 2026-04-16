// === CHUNK: TRACK_CONTROLLER [API] ===
// Описание: REST-контроллер для управления треками.
// Dependencies: Spring Web

package org.homework.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.homework.dto.request.CreateNoteRequest;
import org.homework.dto.request.CreateTrackRequest;
import org.homework.dto.request.UpdateNoteRequest;
import org.homework.dto.request.UpdateTrackRequest;
import org.homework.dto.response.NoteDto;
import org.homework.dto.response.NoteTreeDto;
import org.homework.dto.response.TrackDto;
import org.homework.service.NoteService;
import org.homework.service.TrackService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// [START_TRACK_CONTROLLER]
/*
 * ANCHOR: TRACK_CONTROLLER
 * PURPOSE: REST-контроллер для управления треками.
 *
 * @PreConditions:
 * - Spring контекст инициализирован
 * - пользователь аутентифицирован
 *
 * @PostConditions:
 * - эндпоинты доступны по /api/v1/tracks
 *
 * @Invariants:
 * - все методы возвращают ResponseEntity
 * - userId извлекается из Authentication
 *
 * @SideEffects:
 * - создание/обновление/удаление треков через TrackService
 *
 * @ForbiddenChanges:
 * - нельзя изменить базовый путь /api/v1/tracks без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные эндпоинты
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tracks")
@RequiredArgsConstructor
public class TrackController {
    
    private final TrackService trackService;
    private final NoteService noteService;
    
    // [START_TRACK_CONTROLLER_GET_ALL]
    /*
     * ANCHOR: TRACK_CONTROLLER_GET_ALL
     * PURPOSE: Получение списка треков текущего пользователя + треки с доступом.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     *
     * @PostConditions:
     * - возвращается 200 OK со списком треков
     *
     * @Invariants:
     * - всегда возвращается Page<TrackDto>
     *
     * @SideEffects:
     * - чтение треков из БД
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные фильтры
     */
    @GetMapping
    public ResponseEntity<Page<TrackDto>> getAll(
            Authentication authentication,
            Pageable pageable) {
        log.info("TRACK_CONTROLLER_GET_ALL ENTRY");
        
        Long userId = getUserIdFromAuthentication(authentication);
        Page<TrackDto> tracks = trackService.getUserTracks(userId, pageable);
        
        log.info("TRACK_CONTROLLER_GET_ALL EXIT - found {} tracks", tracks.getTotalElements());
        return ResponseEntity.ok(tracks);
    }
    // [END_TRACK_CONTROLLER_GET_ALL]
    
    // [START_TRACK_CONTROLLER_CREATE]
    /*
     * ANCHOR: TRACK_CONTROLLER_CREATE
     * PURPOSE: Создание трека.
     *
     * @PreConditions:
     * - request валиден
     * - пользователь аутентифицирован
     *
     * @PostConditions:
     * - возвращается 200 OK с созданным треком
     *
     * @Invariants:
     * - созданный трек имеет владельца - текущего пользователя
     *
     * @SideEffects:
     * - создание трека в БД
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные поля в запрос
     */
    @PostMapping
    public ResponseEntity<TrackDto> create(
            @Valid @RequestBody CreateTrackRequest request,
            Authentication authentication) {
        log.info("TRACK_CONTROLLER_CREATE ENTRY - title: {}", request.getTitle());
        
        Long userId = getUserIdFromAuthentication(authentication);
        TrackDto track = trackService.createTrack(request, userId);
        
        log.info("TRACK_CONTROLLER_CREATE EXIT - track created with id: {}", track.getId());
        return ResponseEntity.ok(track);
    }
    // [END_TRACK_CONTROLLER_CREATE]
    
    // [START_TRACK_CONTROLLER_GET_BY_ID]
    /*
     * ANCHOR: TRACK_CONTROLLER_GET_BY_ID
     * PURPOSE: Получение трека по id.
     *
     * @PreConditions:
     * - id валиден
     * - пользователь аутентифицирован
     * - пользователь имеет право VIEW на трек
     *
     * @PostConditions:
     * - возвращается 200 OK с треком
     *
     * @Invariants:
     * - возвращается только если пользователь имеет доступ
     *
     * @SideEffects:
     * - чтение трека из БД
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные данные в ответ
     */
    @GetMapping("/{id}")
    public ResponseEntity<TrackDto> getById(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("TRACK_CONTROLLER_GET_BY_ID ENTRY - id: {}", id);
        
        Long userId = getUserIdFromAuthentication(authentication);
        TrackDto track = trackService.getTrackById(id, userId);
        
        log.info("TRACK_CONTROLLER_GET_BY_ID EXIT - track found");
        return ResponseEntity.ok(track);
    }
    // [END_TRACK_CONTROLLER_GET_BY_ID]
    
    // [START_TRACK_CONTROLLER_GET_NOTES_TREE]
    /*
     * ANCHOR: TRACK_CONTROLLER_GET_NOTES_TREE
     * PURPOSE: Получение дерева заметок для трека.
     *
     * @PreConditions:
     * - id валиден
     * - пользователь аутентифицирован
     * - пользователь имеет доступ VIEW или EDIT
     *
     * @PostConditions:
     * - возвращается 200 OK с деревом заметок
     *
     * @Invariants:
     * - возвращается только если пользователь имеет доступ
     *
     * @SideEffects:
     * - чтение заметок из БД
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно изменить формат ответа
     */
    @GetMapping("/{id}/notes/tree")
    public ResponseEntity<java.util.List<NoteTreeDto>> getNotesTree(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("TRACK_CONTROLLER_GET_NOTES_TREE ENTRY - id: {}", id);
        
        Long userId = getUserIdFromAuthentication(authentication);
        java.util.List<NoteTreeDto> notes = noteService.getNotesTree(id, userId);
        
        log.info("TRACK_CONTROLLER_GET_NOTES_TREE EXIT - found {} root notes", notes.size());
        return ResponseEntity.ok(notes);
    }
    // [END_TRACK_CONTROLLER_GET_NOTES_TREE]
    
    // [START_TRACK_CONTROLLER_CREATE_NOTE]
    /*
     * ANCHOR: TRACK_CONTROLLER_CREATE_NOTE
     * PURPOSE: Создание заметки в треке.
     *
     * @PreConditions:
     * - trackId валиден
     * - request валиден
     * - пользователь аутентифицирован
     * - пользователь имеет право EDIT на трек
     *
     * @PostConditions:
     * - возвращается 200 OK с созданной заметкой
     *
     * @Invariants:
     * - создание только если пользователь имеет право EDIT
     *
     * @SideEffects:
     * - создание заметки в БД
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно изменить формат ответа
     */
    @PostMapping("/{trackId}/notes")
    public ResponseEntity<NoteDto> createNote(
            @PathVariable Long trackId,
            @Valid @RequestBody CreateNoteRequest request,
            Authentication authentication) {
        log.info("TRACK_CONTROLLER_CREATE_NOTE ENTRY - trackId: {}, title: {}", trackId, request.getTitle());
        
        Long userId = getUserIdFromAuthentication(authentication);
        NoteDto note = noteService.createNote(trackId, request, userId);
        
        log.info("TRACK_CONTROLLER_CREATE_NOTE EXIT - note created with id: {}", note.getId());
        return ResponseEntity.ok(note);
    }
    // [END_TRACK_CONTROLLER_CREATE_NOTE]
    
    // [START_TRACK_CONTROLLER_UPDATE]
    /*
     * ANCHOR: TRACK_CONTROLLER_UPDATE
     * PURPOSE: Обновление трека.
     *
     * @PreConditions:
     * - id валиден
     * - request валиден
     * - пользователь аутентифицирован
     * - пользователь имеет право EDIT на трек
     *
     * @PostConditions:
     * - возвращается 200 OK с обновленным треком
     *
     * @Invariants:
     * - обновление только если пользователь имеет право EDIT
     *
     * @SideEffects:
     * - обновление трека в БД
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные поля в запрос
     */
    @PutMapping("/{id}")
    public ResponseEntity<TrackDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTrackRequest request,
            Authentication authentication) {
        log.info("TRACK_CONTROLLER_UPDATE ENTRY - id: {}", id);
        
        Long userId = getUserIdFromAuthentication(authentication);
        TrackDto track = trackService.updateTrack(id, request, userId);
        
        log.info("TRACK_CONTROLLER_UPDATE EXIT - track updated");
        return ResponseEntity.ok(track);
    }
    // [END_TRACK_CONTROLLER_UPDATE]
    
    // [START_TRACK_CONTROLLER_UPDATE_NOTE]
    /*
     * ANCHOR: TRACK_CONTROLLER_UPDATE_NOTE
     * PURPOSE: Обновление заметки.
     *
     * @PreConditions:
     * - noteId валиден
     * - request валиден
     * - пользователь аутентифицирован
     * - пользователь имеет право EDIT на трек заметки
     *
     * @PostConditions:
     * - возвращается 200 OK с обновленной заметкой
     *
     * @Invariants:
     * - обновление только если пользователь имеет право EDIT
     *
     * @SideEffects:
     * - обновление заметки в БД
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно изменить формат ответа
     */
    @PutMapping("/{trackId}/notes/{noteId}")
    public ResponseEntity<NoteDto> updateNote(
            @PathVariable Long trackId,
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateNoteRequest request,
            Authentication authentication) {
        log.info("TRACK_CONTROLLER_UPDATE_NOTE ENTRY - trackId: {}, noteId: {}, title: {}",
                trackId, noteId, request.getTitle());
        
        Long userId = getUserIdFromAuthentication(authentication);
        NoteDto note = noteService.updateNote(noteId, request, userId);
        
        log.info("TRACK_CONTROLLER_UPDATE_NOTE EXIT - note updated with id: {}", note.getId());
        return ResponseEntity.ok(note);
    }
    // [END_TRACK_CONTROLLER_UPDATE_NOTE]
    
    // [START_TRACK_CONTROLLER_DELETE_NOTE]
    /*
     * ANCHOR: TRACK_CONTROLLER_DELETE_NOTE
     * PURPOSE: Удаление заметки.
     *
     * @PreConditions:
     * - noteId валиден
     * - пользователь аутентифицирован
     * - пользователь имеет право EDIT на трек заметки
     *
     * @PostConditions:
     * - возвращается 200 OK
     *
     * @Invariants:
     * - удаление только если пользователь имеет право EDIT
     *
     * @SideEffects:
     * - удаление заметки из БД (каскадное)
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные параметры
     */
    @DeleteMapping("/{trackId}/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long trackId,
            @PathVariable Long noteId,
            Authentication authentication) {
        log.info("TRACK_CONTROLLER_DELETE_NOTE ENTRY - trackId: {}, noteId: {}", trackId, noteId);
        
        Long userId = getUserIdFromAuthentication(authentication);
        noteService.deleteNote(noteId, userId);
        
        log.info("TRACK_CONTROLLER_DELETE_NOTE EXIT - note deleted with id: {}", noteId);
        return ResponseEntity.ok().build();
    }
    // [END_TRACK_CONTROLLER_DELETE_NOTE]
    
    // [START_TRACK_CONTROLLER_DELETE]
    /*
     * ANCHOR: TRACK_CONTROLLER_DELETE
     * PURPOSE: Удаление трека.
     *
     * @PreConditions:
     * - id валиден
     * - пользователь аутентифицирован
     * - пользователь является владельцем трека (OWNER)
     *
     * @PostConditions:
     * - возвращается 200 OK
     *
     * @Invariants:
     * - удаление только если пользователь является владельцем
     *
     * @SideEffects:
     * - удаление трека из БД (каскадное)
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные параметры
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("TRACK_CONTROLLER_DELETE ENTRY - id: {}", id);
        
        Long userId = getUserIdFromAuthentication(authentication);
        trackService.deleteTrack(id, userId);
        
        log.info("TRACK_CONTROLLER_DELETE EXIT - track deleted");
        return ResponseEntity.ok().build();
    }
    // [END_TRACK_CONTROLLER_DELETE]
    
    /**
     * Извлечь userId из Authentication.
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Principal теперь содержит userId (Long) из JWT токена
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof String) {
            return Long.parseLong((String) principal);
        } else {
            throw new IllegalArgumentException("Invalid principal type: " + principal.getClass());
        }
    }
}
// [END_TRACK_CONTROLLER]
// === END_CHUNK: TRACK_CONTROLLER ===
