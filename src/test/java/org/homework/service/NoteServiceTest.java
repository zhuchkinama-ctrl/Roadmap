// === CHUNK: NOTE_SERVICE_TEST [TEST] ===
// Описание: Тесты для NoteService, включая функциональность completed.
// Dependencies: JUnit 5, Spring Test, Mockito

package org.homework.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// [START_NOTE_SERVICE_TEST]
/*
 * ANCHOR: NOTE_SERVICE_TEST
 * PURPOSE: Тесты для NoteService, включая функциональность completed.
 *
 * @PreConditions:
 * - Spring Test контекст инициализирован
 * - моки настроены
 *
 * @PostConditions:
 * - тесты проверяют корректность работы сервиса
 *
 * @Invariants:
 * - каждый тест изолирован
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя изменить логику тестов без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тесты
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private NoteService noteService;

    private User testUser;
    private Track testTrack;
    private Note testNote;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .passwordHash("password")
                .email("test@example.com")
                .role("USER")
                .enabled(true)
                .build();

        testTrack = Track.builder()
                .id(1L)
                .title("Test Track")
                .description("Test Description")
                .owner(testUser)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testNote = Note.builder()
                .id(1L)
                .track(testTrack)
                .author(testUser)
                .parent(null)
                .title("Test Note")
                .content("Test Content")
                .orderIndex(0)
                .completed(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // [START_NOTE_SERVICE_TEST_GET_TREE]
    /*
     * ANCHOR: NOTE_SERVICE_TEST_GET_TREE
     * PURPOSE: Тест получения дерева заметок.
     *
     * @PreConditions:
     * - моки настроены
     *
     * @PostConditions:
     * - проверяется корректность получения дерева
     *
     * @Invariants:
     * - тест изолирован
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя изменить логику теста без согласования
     *
     * @AllowedRefactorZone:
     * - можно изменить формат данных
     */
    @Test
    void testGetNotesTree_Success() {
        // Arrange
        Long trackId = 1L;
        Long userId = 1L;

        Note childNote = Note.builder()
                .id(2L)
                .track(testTrack)
                .author(testUser)
                .parent(testNote)
                .title("Child Note")
                .content("Child Content")
                .orderIndex(0)
                .completed(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(trackRepository.findById(trackId)).thenReturn(Optional.of(testTrack));
        when(noteRepository.findByTrackIdOrderByOrderIndex(trackId))
                .thenReturn(Arrays.asList(testNote, childNote));

        // Act
        List<NoteTreeDto> result = noteService.getNotesTree(trackId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testNote.getId(), result.get(0).getId());
        assertEquals(testNote.getTitle(), result.get(0).getTitle());
        assertNotNull(result.get(0).getChildren());
        assertEquals(1, result.get(0).getChildren().size());
        assertEquals(childNote.getId(), result.get(0).getChildren().get(0).getId());

        verify(permissionService).checkPermission(trackId, userId, PermissionService.Permission.VIEW);
        verify(trackRepository).findById(trackId);
        verify(noteRepository).findByTrackIdOrderByOrderIndex(trackId);
    }
    // [END_NOTE_SERVICE_TEST_GET_TREE]

    // [START_NOTE_SERVICE_TEST_CREATE]
    /*
     * ANCHOR: NOTE_SERVICE_TEST_CREATE
     * PURPOSE: Тест создания заметки.
     *
     * @PreConditions:
     * - моки настроены
     *
     * @PostConditions:
     * - проверяется корректность создания заметки
     *
     * @Invariants:
     * - тест изолирован
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя изменить логику теста без согласования
     *
     * @AllowedRefactorZone:
     * - можно изменить формат данных
     */
    @Test
    void testCreateNote_Success() {
        // Arrange
        Long trackId = 1L;
        Long userId = 1L;
        CreateNoteRequest request = CreateNoteRequest.builder()
                .title("New Note")
                .content("New Content")
                .parentId(null)
                .orderIndex(0)
                .build();

        when(trackRepository.findById(trackId)).thenReturn(Optional.of(testTrack));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            note.setId(2L);
            return note;
        });

        // Act
        NoteDto result = noteService.createNote(trackId, request, userId);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("New Note", result.getTitle());
        assertEquals("New Content", result.getContent());
        assertEquals(trackId, result.getTrackId());

        verify(permissionService).checkPermission(trackId, userId, PermissionService.Permission.EDIT);
        verify(trackRepository).findById(trackId);
        verify(userRepository).findById(userId);
        verify(noteRepository).save(any(Note.class));
    }
    // [END_NOTE_SERVICE_TEST_CREATE]

    // [START_NOTE_SERVICE_TEST_UPDATE]
    /*
     * ANCHOR: NOTE_SERVICE_TEST_UPDATE
     * PURPOSE: Тест обновления заметки.
     *
     * @PreConditions:
     * - моки настроены
     *
     * @PostConditions:
     * - проверяется корректность обновления заметки
     *
     * @Invariants:
     * - тест изолирован
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя изменить логику теста без согласования
     *
     * @AllowedRefactorZone:
     * - можно изменить формат данных
     */
    @Test
    void testUpdateNote_Success() {
        // Arrange
        Long noteId = 1L;
        Long userId = 1L;
        UpdateNoteRequest request = UpdateNoteRequest.builder()
                .title("Updated Note")
                .content("Updated Content")
                .parentId(null)
                .orderIndex(1)
                .build();

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        // Act
        NoteDto result = noteService.updateNote(noteId, request, userId);

        // Assert
        assertNotNull(result);
        assertEquals(noteId, result.getId());

        verify(noteRepository).findById(noteId);
        verify(permissionService).checkPermission(testTrack.getId(), userId, PermissionService.Permission.EDIT);
        verify(noteRepository).save(any(Note.class));
    }
    // [END_NOTE_SERVICE_TEST_UPDATE]

    // [START_NOTE_SERVICE_TEST_DELETE]
    /*
     * ANCHOR: NOTE_SERVICE_TEST_DELETE
     * PURPOSE: Тест удаления заметки.
     *
     * @PreConditions:
     * - моки настроены
     *
     * @PostConditions:
     * - проверяется корректность удаления заметки
     *
     * @Invariants:
     * - тест изолирован
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя изменить логику теста без согласования
     *
     * @AllowedRefactorZone:
     * - можно изменить формат данных
     */
    @Test
    void testDeleteNote_Success() {
        // Arrange
        Long noteId = 1L;
        Long userId = 1L;

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(testNote));
        doNothing().when(noteRepository).delete(any(Note.class));

        // Act
        noteService.deleteNote(noteId, userId);

        // Assert
        verify(noteRepository).findById(noteId);
        verify(permissionService).checkPermission(testTrack.getId(), userId, PermissionService.Permission.EDIT);
        verify(noteRepository).delete(testNote);
    }
    // [END_NOTE_SERVICE_TEST_DELETE]

    // [START_NOTE_SERVICE_TEST_TOGGLE_COMPLETED]
    /*
     * ANCHOR: NOTE_SERVICE_TEST_TOGGLE_COMPLETED
     * PURPOSE: Тест переключения статуса completed заметки.
     *
     * @PreConditions:
     * - моки настроены
     *
     * @PostConditions:
     * - проверяется корректность переключения completed
     *
     * @Invariants:
     * - тест изолирован
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя изменить логику теста без согласования
     *
     * @AllowedRefactorZone:
     * - можно изменить формат данных
     */
    @Test
    void testToggleNoteCompleted_Success() {
        // Arrange
        Long noteId = 1L;
        Long userId = 1L;

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            return note;
        });

        // Act
        NoteDto result = noteService.toggleNoteCompleted(noteId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(noteId, result.getId());
        assertTrue(result.getCompleted());

        verify(noteRepository).findById(noteId);
        verify(permissionService).checkPermission(testTrack.getId(), userId, PermissionService.Permission.EDIT);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void testToggleNoteCompleted_NoteNotFound() {
        // Arrange
        Long noteId = 999L;
        Long userId = 1L;

        when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            noteService.toggleNoteCompleted(noteId, userId);
        });

        verify(noteRepository).findById(noteId);
        verify(permissionService, never()).checkPermission(anyLong(), anyLong(), any());
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void testToggleNoteCompleted_PermissionDenied() {
        // Arrange
        Long noteId = 1L;
        Long userId = 2L;

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(testNote));
        doThrow(new IllegalStateException("Permission denied"))
                .when(permissionService).checkPermission(testTrack.getId(), userId, PermissionService.Permission.EDIT);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            noteService.toggleNoteCompleted(noteId, userId);
        });

        verify(noteRepository).findById(noteId);
        verify(permissionService).checkPermission(testTrack.getId(), userId, PermissionService.Permission.EDIT);
        verify(noteRepository, never()).save(any(Note.class));
    }
    // [END_NOTE_SERVICE_TEST_TOGGLE_COMPLETED]
}
// [END_NOTE_SERVICE_TEST]
// === END_CHUNK: NOTE_SERVICE_TEST ===
