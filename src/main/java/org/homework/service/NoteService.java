// === CHUNK: NOTE_SERVICE [SERVICE] ===
// Описание: Сервис для управления заметками.
// Dependencies: Spring, JPA, Lombok

package org.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.homework.dto.request.CreateNoteRequest;
import org.homework.dto.request.UpdateNoteRequest;
import org.homework.dto.response.NoteDto;
import org.homework.dto.response.NoteTreeDto;
import org.homework.exception.ResourceNotFoundException;
import org.homework.model.Note;
import org.homework.model.Track;
import org.homework.model.User;
import org.homework.repository.NoteRepository;
import org.homework.repository.TrackRepository;
import org.homework.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// [START_NOTE_SERVICE]
/*
 * ANCHOR: NOTE_SERVICE
 * PURPOSE: Сервис для управления заметками.
 *
 * @PreConditions:
 * - Spring контекст инициализирован
 * - репозитории доступны
 *
 * @PostConditions:
 * - сервис готов для работы с заметками
 *
 * @Invariants:
 * - все операции транзакционные
 * - заметка всегда принадлежит треку
 *
 * @SideEffects:
 * - создание/обновление/удаление заметок в БД
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку прав доступа
 * - нельзя сделать операции нетранзакционными
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные методы
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {
    
    private final NoteRepository noteRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    
    // [START_NOTE_SERVICE_GET_TREE]
    /*
     * ANCHOR: NOTE_SERVICE_GET_TREE
     * PURPOSE: Получение дерева заметок для трека.
     *
     * @PreConditions:
     * - trackId валиден
     * - userId валиден
     * - пользователь имеет право VIEW или EDIT на трек
     *
     * @PostConditions:
     * - возвращается дерево заметок
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
    @Transactional(readOnly = true)
    public List<NoteTreeDto> getNotesTree(Long trackId, Long userId) {
        log.info("NOTE_SERVICE_GET_TREE ENTRY - trackId: {}, userId: {}", trackId, userId);
        
        // Проверка прав доступа
        permissionService.checkPermission(trackId, userId, PermissionService.Permission.VIEW);
        
        // Получение трека
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));
        
        // Получение всех заметок трека
        List<Note> allNotes = noteRepository.findByTrackIdOrderByOrderIndex(trackId);
        
        // Построение дерева
        List<NoteTreeDto> tree = buildNoteTree(allNotes);
        
        log.info("NOTE_SERVICE_GET_TREE EXIT - found {} root notes", tree.size());
        return tree;
    }
    // [END_NOTE_SERVICE_GET_TREE]
    
    // [START_NOTE_SERVICE_CREATE]
    /*
     * ANCHOR: NOTE_SERVICE_CREATE
     * PURPOSE: Создание заметки в треке.
     *
     * @PreConditions:
     * - trackId валиден
     * - request валиден
     * - userId валиден
     * - пользователь имеет право EDIT на трек
     *
     * @PostConditions:
     * - возвращается созданная заметка
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
    @Transactional
    public NoteDto createNote(Long trackId, CreateNoteRequest request, Long userId) {
        log.info("NOTE_SERVICE_CREATE ENTRY - trackId: {}, userId: {}, title: {}",
                trackId, userId, request.getTitle());
        
        // Проверка прав доступа
        permissionService.checkPermission(trackId, userId, PermissionService.Permission.EDIT);
        
        // Получение трека
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with id: " + trackId));
        
        // Получение автора
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Проверка родительской заметки (если указана)
        Note parent = null;
        if (request.getParentId() != null) {
            parent = noteRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent note not found with id: " + request.getParentId()));
            
            // Проверка, что родительская заметка принадлежит тому же треку
            if (!parent.getTrack().getId().equals(trackId)) {
                throw new IllegalArgumentException("Parent note must belong to the same track");
            }
        }
        
        // Создание заметки
        Note note = Note.builder()
                .track(track)
                .author(author)
                .parent(parent)
                .title(request.getTitle())
                .content(request.getContent())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        note = noteRepository.save(note);
        
        log.info("NOTE_SERVICE_CREATE EXIT - note created with id: {}", note.getId());
        return mapToNoteDto(note);
    }
    // [END_NOTE_SERVICE_CREATE]
    
    // [START_NOTE_SERVICE_UPDATE]
    /*
     * ANCHOR: NOTE_SERVICE_UPDATE
     * PURPOSE: Обновление заметки.
     *
     * @PreConditions:
     * - noteId валиден
     * - request валиден
     * - userId валиден
     * - пользователь имеет право EDIT на трек заметки
     *
     * @PostConditions:
     * - возвращается обновленная заметка
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
    @Transactional
    public NoteDto updateNote(Long noteId, UpdateNoteRequest request, Long userId) {
        log.info("NOTE_SERVICE_UPDATE ENTRY - noteId: {}, userId: {}, title: {}",
                noteId, userId, request.getTitle());
        
        // Получение заметки
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));
        
        // Проверка прав доступа
        permissionService.checkPermission(note.getTrack().getId(), userId, PermissionService.Permission.EDIT);
        
        // Проверка родительской заметки (если указана)
        if (request.getParentId() != null) {
            Note parent = noteRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent note not found with id: " + request.getParentId()));
            
            // Проверка, что родительская заметка принадлежит тому же треку
            if (!parent.getTrack().getId().equals(note.getTrack().getId())) {
                throw new IllegalArgumentException("Parent note must belong to the same track");
            }
            
            // Проверка, что не создается циклическая ссылка
            if (request.getParentId().equals(noteId)) {
                throw new IllegalArgumentException("Note cannot be its own parent");
            }
            
            note.setParent(parent);
        } else {
            note.setParent(null);
        }
        
        // Обновление полей
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        note.setUpdatedAt(Instant.now());
        
        note = noteRepository.save(note);
        
        log.info("NOTE_SERVICE_UPDATE EXIT - note updated with id: {}", note.getId());
        return mapToNoteDto(note);
    }
    // [END_NOTE_SERVICE_UPDATE]
    
    // [START_NOTE_SERVICE_DELETE]
    /*
     * ANCHOR: NOTE_SERVICE_DELETE
     * PURPOSE: Удаление заметки.
     *
     * @PreConditions:
     * - noteId валиден
     * - userId валиден
     * - пользователь имеет право EDIT на трек заметки
     *
     * @PostConditions:
     * - заметка удалена
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
    @Transactional
    public void deleteNote(Long noteId, Long userId) {
        log.info("NOTE_SERVICE_DELETE ENTRY - noteId: {}, userId: {}", noteId, userId);
        
        // Получение заметки
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));
        
        // Проверка прав доступа
        permissionService.checkPermission(note.getTrack().getId(), userId, PermissionService.Permission.EDIT);
        
        // Удаление заметки (каскадное удаление дочерних настроено в БД)
        noteRepository.delete(note);
        
        log.info("NOTE_SERVICE_DELETE EXIT - note deleted with id: {}", noteId);
    }
    // [END_NOTE_SERVICE_DELETE]

    // [START_NOTE_SERVICE_TOGGLE_COMPLETED]
    /*
     * ANCHOR: NOTE_SERVICE_TOGGLE_COMPLETED
     * PURPOSE: Переключение статуса completed заметки.
     *
     * @PreConditions:
     * - noteId валиден
     * - userId валиден
     * - пользователь имеет право EDIT на трек заметки
     *
     * @PostConditions:
     * - возвращается обновленная заметка с инвертированным completed
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
    @Transactional
    public NoteDto toggleNoteCompleted(Long noteId, Long userId) {
        log.info("NOTE_SERVICE_TOGGLE_COMPLETED ENTRY - noteId: {}, userId: {}", noteId, userId);

        // Получение заметки
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));

        // Проверка прав доступа
        permissionService.checkPermission(note.getTrack().getId(), userId, PermissionService.Permission.EDIT);

        // Переключение статуса completed
        note.setCompleted(!note.getCompleted());
        note.setUpdatedAt(Instant.now());

        note = noteRepository.save(note);

        log.info("NOTE_SERVICE_TOGGLE_COMPLETED EXIT - note {} completed: {}", noteId, note.getCompleted());
        return mapToNoteDto(note);
    }
    // [END_NOTE_SERVICE_TOGGLE_COMPLETED]

    // [START_NOTE_SERVICE_BUILD_TREE]
    /*
     * ANCHOR: NOTE_SERVICE_BUILD_TREE
     * PURPOSE: Построение дерева заметок из плоского списка.
     *
     * @PreConditions:
     * - allNotes содержит все заметки трека
     *
     * @PostConditions:
     * - возвращается дерево заметок
     *
     * @Invariants:
     * - все заметки включены в дерево
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя изменить логику построения дерева без согласования
     *
     * @AllowedRefactorZone:
     * - можно оптимизировать алгоритм
     */
    private List<NoteTreeDto> buildNoteTree(List<Note> allNotes) {
        // Создаем мапу для быстрого доступа
        Map<Long, NoteTreeDto> noteMap = allNotes.stream()
                .collect(Collectors.toMap(Note::getId, this::mapToNoteTreeDto));
        
        // Строим дерево
        List<NoteTreeDto> rootNotes = new ArrayList<>();
        for (Note note : allNotes) {
            NoteTreeDto noteDto = noteMap.get(note.getId());
            
            if (note.getParent() == null) {
                // Это корневая заметка (без родителя)
                rootNotes.add(noteDto);
            } else {
                // Ищем родителя
                NoteTreeDto parentDto = noteMap.get(note.getParent().getId());
                if (parentDto != null) {
                    if (parentDto.getChildren() == null) {
                        parentDto.setChildren(new ArrayList<>());
                    }
                    parentDto.getChildren().add(noteDto);
                }
            }
        }
        
        return rootNotes;
    }
    // [END_NOTE_SERVICE_BUILD_TREE]
    
    // [START_NOTE_SERVICE_MAP_TO_TREE_DTO]
    /*
     * ANCHOR: NOTE_SERVICE_MAP_TO_TREE_DTO
     * PURPOSE: Преобразование сущности Note в NoteTreeDto.
     *
     * @PreConditions:
     * - note не null
     *
     * @PostConditions:
     * - возвращается DTO с данными заметки
     *
     * @Invariants:
     * - все поля скопированы корректно
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя изменить логику маппинга без согласования
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные поля
     */
    private NoteTreeDto mapToNoteTreeDto(Note note) {
        return NoteTreeDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .orderIndex(note.getOrderIndex())
                .completed(note.getCompleted())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .children(null) // Дочерние элементы будут заполнены при построении дерева
                .build();
    }
    // [END_NOTE_SERVICE_MAP_TO_TREE_DTO]
    
    // [START_NOTE_SERVICE_MAP_TO_DTO]
    /*
     * ANCHOR: NOTE_SERVICE_MAP_TO_DTO
     * PURPOSE: Преобразование сущности Note в NoteDto.
     *
     * @PreConditions:
     * - note не null
     *
     * @PostConditions:
     * - возвращается DTO с данными заметки
     *
     * @Invariants:
     * - все поля скопированы корректно
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя изменить логику маппинга без согласования
     *
     * @AllowedRefactorZone:
     * - можно добавить дополнительные поля
     */
    private NoteDto mapToNoteDto(Note note) {
        NoteDto dto = new NoteDto();
        dto.setId(note.getId());
        dto.setTrackId(note.getTrack().getId());
        dto.setParentId(note.getParent() != null ? note.getParent().getId() : null);
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setOrderIndex(note.getOrderIndex());
        dto.setCompleted(note.getCompleted());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        return dto;
    }
    // [END_NOTE_SERVICE_MAP_TO_DTO]
}
// [END_NOTE_SERVICE]
// === END_CHUNK: NOTE_SERVICE ===
