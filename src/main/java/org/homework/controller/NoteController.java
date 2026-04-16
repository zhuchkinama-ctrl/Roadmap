// === CHUNK: NOTE_CONTROLLER [API] ===
// Описание: REST-контроллер для управления заметками.
// Dependencies: Spring Web, NoteService

package org.homework.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.homework.dto.request.UpdateNoteRequest;
import org.homework.dto.response.NoteDto;
import org.homework.service.NoteService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

// [START_NOTE_CONTROLLER]
/*
 * ANCHOR: NOTE_CONTROLLER
 * PURPOSE: REST-контроллер для управления заметками (заглушка).
 *
 * @PreConditions:
 * - Spring контекст инициализирован
 *
 * @PostConditions:
 * - эндпоинты доступны по /api/v1/notes
 *
 * @Invariants:
 * - все методы возвращают ResponseEntity
 *
 * @SideEffects:
 * - нет побочных эффектов (заглушка)
 *
 * @ForbiddenChanges:
 * - нельзя изменить базовый путь /api/v1/notes без согласования
 *
 * @AllowedRefactorZone:
 * - можно реализовать бизнес-логику вместо заглушек
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {
    
    private final NoteService noteService;

    // [START_NOTE_CONTROLLER_GET_TREE]
    /*
     * ANCHOR: NOTE_CONTROLLER_GET_TREE
     * PURPOSE: Получение дерева заметок (заглушка).
     *
     * @PreConditions:
     * - нет нетривиальных предусловий
     *
     * @PostConditions:
     * - возвращается 200 OK (заглушка)
     *
     * @Invariants:
     * - метод всегда возвращает 200 OK (заглушка)
     *
     * @SideEffects:
     * - нет побочных эффектов (заглушка)
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно реализовать бизнес-логику получения дерева заметок
     */
    @GetMapping("/tree")
    public ResponseEntity<?> getTree() {
        log.info("NOTE_CONTROLLER_GET_TREE ENTRY");
        // TODO: реализовать получение дерева заметок
        log.info("NOTE_CONTROLLER_GET_TREE EXIT - stub");
        return ResponseEntity.ok().build();
    }
    // [END_NOTE_CONTROLLER_GET_TREE]

    // [START_NOTE_CONTROLLER_CREATE]
    /*
     * ANCHOR: NOTE_CONTROLLER_CREATE
     * PURPOSE: Создание заметки (заглушка).
     *
     * @PreConditions:
     * - request валиден
     *
     * @PostConditions:
     * - возвращается 200 OK (заглушка)
     *
     * @Invariants:
     * - метод всегда возвращает 200 OK (заглушка)
     *
     * @SideEffects:
     * - нет побочных эффектов (заглушка)
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно реализовать бизнес-логику создания заметки
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Object request) {
        log.info("NOTE_CONTROLLER_CREATE ENTRY");
        // TODO: реализовать создание заметки
        log.info("NOTE_CONTROLLER_CREATE EXIT - stub");
        return ResponseEntity.ok().build();
    }
    // [END_NOTE_CONTROLLER_CREATE]

    // [START_NOTE_CONTROLLER_UPDATE]
    /*
     * ANCHOR: NOTE_CONTROLLER_UPDATE
     * PURPOSE: Обновление заметки.
     *
     * @PreConditions:
     * - id валиден
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
     * - обновление заметки в БДда.lf
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно изменить формат ответа
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoteDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoteRequest request,
            Authentication authentication) {
        log.info("NOTE_CONTROLLER_UPDATE ENTRY - id: {}, title: {}", id, request.getTitle());
        
        Long userId = getUserIdFromAuthentication(authentication);
        NoteDto note = noteService.updateNote(id, request, userId);
        
        log.info("NOTE_CONTROLLER_UPDATE EXIT - note updated with id: {}", note.getId());
        return ResponseEntity.ok(note);
    }
    // [END_NOTE_CONTROLLER_UPDATE]

    // [START_NOTE_CONTROLLER_DELETE]
    /*
     * ANCHOR: NOTE_CONTROLLER_DELETE
     * PURPOSE: Удаление заметки.
     *
     * @PreConditions:
     * - id валиден
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
     * - удаление заметки из БД (каскадное удаление дочерних)
     *
     * @ForbiddenChanges:
     * - нельзя изменить сигнатуру метода без согласования
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные параметры
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        log.info("NOTE_CONTROLLER_DELETE ENTRY - id: {}", id);
        
        Long userId = getUserIdFromAuthentication(authentication);
        noteService.deleteNote(id, userId);
        
        log.info("NOTE_CONTROLLER_DELETE EXIT - note deleted with id: {}", id);
        return ResponseEntity.ok().build();
    }
    // [END_NOTE_CONTROLLER_DELETE]

    // [START_NOTE_CONTROLLER_TOGGLE_COMPLETED]
    /*
     * ANCHOR: NOTE_CONTROLLER_TOGGLE_COMPLETED
     * PURPOSE: Переключение статуса completed заметки.
     *
     * @PreConditions:
     * - id валиден
     * - пользователь аутентифицирован
     * - пользователь имеет право EDIT на трек заметки
     *
     * @PostConditions:
     * - возвращается 200 OK с обновленной заметкой
     *
     * @Invariants:
     * - переключение только если пользователь имеет право EDIT
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
    @PatchMapping("/{id}/toggle-completed")
    public ResponseEntity<NoteDto> toggleCompleted(@PathVariable Long id, Authentication authentication) {
        log.info("NOTE_CONTROLLER_TOGGLE_COMPLETED ENTRY - id: {}", id);

        Long userId = getUserIdFromAuthentication(authentication);
        NoteDto note = noteService.toggleNoteCompleted(id, userId);

        log.info("NOTE_CONTROLLER_TOGGLE_COMPLETED EXIT - note {} completed: {}", id, note.getCompleted());
        return ResponseEntity.ok(note);
    }
    // [END_NOTE_CONTROLLER_TOGGLE_COMPLETED]

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
// [END_NOTE_CONTROLLER]
// === END_CHUNK: NOTE_CONTROLLER ===
