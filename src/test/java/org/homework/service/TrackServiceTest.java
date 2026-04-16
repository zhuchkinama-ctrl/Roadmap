package org.homework.service;

import org.homework.dto.request.CreateTrackRequest;
import org.homework.dto.request.UpdateTrackRequest;
import org.homework.dto.response.TrackDto;
import org.homework.dto.response.UserDto;
import org.homework.exception.ResourceNotFoundException;
import org.homework.model.Track;
import org.homework.model.User;
import org.homework.repository.TrackRepository;
import org.homework.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// === CHUNK: TRACK_SERVICE_TEST [TEST] ===
// Описание: Юнит-тесты для TrackService.
// Dependencies: JUnit 5, Mockito, Spring Data

// [START_TRACK_SERVICE_TEST]
/*
 * ANCHOR: TRACK_SERVICE_TEST
 * PURPOSE: Юнит-тесты для TrackService.
 *
 * @PreConditions:
 * - TrackService инициализирован с моками зависимостей
 *
 * @PostConditions:
 * - все тесты проверяют контракты методов TrackService
 *
 * @Invariants:
 * - трек всегда имеет владельца
 * - updatedAt обновляется при каждом изменении
 * - проверка прав доступа обязательна
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку прав доступа
 * - нельзя разрешить удаление трека не владельцу
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тестовые кейсы
 */
@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private TrackService trackService;

    private static final Long USER_ID = 100L;
    private static final Long OTHER_USER_ID = 200L;
    private static final Long TRACK_ID = 1L;

    private User testUser;
    private Track testTrack;
    private CreateTrackRequest createTrackRequest;
    private UpdateTrackRequest updateTrackRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(USER_ID)
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testTrack = Track.builder()
                .id(TRACK_ID)
                .title("Test Track")
                .description("Test Description")
                .owner(testUser)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        createTrackRequest = new CreateTrackRequest();
        createTrackRequest.setTitle("New Track");
        createTrackRequest.setDescription("New Description");

        updateTrackRequest = new UpdateTrackRequest();
        updateTrackRequest.setTitle("Updated Track");
        updateTrackRequest.setDescription("Updated Description");

        pageable = PageRequest.of(0, 10);
    }

    // [START_TRACK_SERVICE_TEST_GET_USER_TRACKS_SUCCESS]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_GET_USER_TRACKS_SUCCESS
     * PURPOSE: Проверка получения треков пользователя.
     *
     * @PreConditions:
     * - пользователь существует
     * - у пользователя есть треки
     *
     * @PostConditions:
     * - возвращается Page с треками пользователя
     *
     * @Invariants:
     * - треки включают собственные и доступные
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку прав доступа
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку пагинации
     */
    @Test
    void getUserTracks_Success() {
        // Arrange
        Page<Track> ownTracks = new PageImpl<>(List.of(testTrack));
        Page<Track> accessibleTracks = new PageImpl<>(Collections.emptyList());
        
        when(trackRepository.findByOwnerId(USER_ID, pageable)).thenReturn(ownTracks);
        when(trackRepository.findAccessibleTracks(USER_ID, pageable)).thenReturn(accessibleTracks);
        when(permissionService.getUserRole(TRACK_ID, USER_ID)).thenReturn("OWNER");

        // Act
        Page<TrackDto> result = trackService.getUserTracks(USER_ID, pageable);

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(1, result.getTotalElements(), "Должен быть один трек");
        assertEquals("Test Track", result.getContent().get(0).getTitle(), "Название трека должно соответствовать");

        verify(trackRepository).findByOwnerId(USER_ID, pageable);
        verify(trackRepository).findAccessibleTracks(USER_ID, pageable);
        verify(permissionService).getUserRole(TRACK_ID, USER_ID);
    }
    // [END_TRACK_SERVICE_TEST_GET_USER_TRACKS_SUCCESS]

    // [START_TRACK_SERVICE_TEST_GET_USER_TRACKS_EMPTY]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_GET_USER_TRACKS_EMPTY
     * PURPOSE: Проверка получения треков пользователя без треков.
     *
     * @PreConditions:
     * - пользователь существует
     * - у пользователя нет треков
     *
     * @PostConditions:
     * - возвращается пустой Page
     *
     * @Invariants:
     * - пустой результат корректен
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку прав доступа
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку пагинации
     */
    @Test
    void getUserTracks_Empty() {
        // Arrange
        Page<Track> ownTracks = new PageImpl<>(Collections.emptyList());
        Page<Track> accessibleTracks = new PageImpl<>(Collections.emptyList());
        
        when(trackRepository.findByOwnerId(USER_ID, pageable)).thenReturn(ownTracks);
        when(trackRepository.findAccessibleTracks(USER_ID, pageable)).thenReturn(accessibleTracks);

        // Act
        Page<TrackDto> result = trackService.getUserTracks(USER_ID, pageable);

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(0, result.getTotalElements(), "Должен быть пустой результат");
        assertTrue(result.getContent().isEmpty(), "Список треков должен быть пустым");

        verify(trackRepository).findByOwnerId(USER_ID, pageable);
        verify(trackRepository).findAccessibleTracks(USER_ID, pageable);
    }
    // [END_TRACK_SERVICE_TEST_GET_USER_TRACKS_EMPTY]

    // [START_TRACK_SERVICE_TEST_GET_USER_TRACKS_WITH_ACCESSIBLE]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_GET_USER_TRACKS_WITH_ACCESSIBLE
     * PURPOSE: Проверка получения треков с доступными треками.
     *
     * @PreConditions:
     * - пользователь существует
     * - у пользователя есть собственные и доступные треки
     *
     * @PostConditions:
     * - возвращается Page с собственными и доступными треками
     *
     * @Invariants:
     * - доступные треки включаются в результат
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку прав доступа
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку пагинации
     */
    @Test
    void getUserTracks_WithAccessible() {
        // Arrange
        User otherUser = User.builder()
                .id(OTHER_USER_ID)
                .username("otheruser")
                .email("other@example.com")
                .role("USER")
                .enabled(true)
                .build();

        Track accessibleTrack = Track.builder()
                .id(2L)
                .title("Accessible Track")
                .description("Accessible Description")
                .owner(otherUser)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Page<Track> ownTracks = new PageImpl<>(List.of(testTrack));
        Page<Track> accessibleTracks = new PageImpl<>(List.of(accessibleTrack));
        
        when(trackRepository.findByOwnerId(USER_ID, pageable)).thenReturn(ownTracks);
        when(trackRepository.findAccessibleTracks(USER_ID, pageable)).thenReturn(accessibleTracks);
        when(permissionService.getUserRole(TRACK_ID, USER_ID)).thenReturn("OWNER");
        when(permissionService.getUserRole(2L, USER_ID)).thenReturn("VIEW");

        // Act
        Page<TrackDto> result = trackService.getUserTracks(USER_ID, pageable);

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(2, result.getTotalElements(), "Должно быть два трека");

        verify(trackRepository).findByOwnerId(USER_ID, pageable);
        verify(trackRepository).findAccessibleTracks(USER_ID, pageable);
    }
    // [END_TRACK_SERVICE_TEST_GET_USER_TRACKS_WITH_ACCESSIBLE]

    // [START_TRACK_SERVICE_TEST_GET_TRACK_BY_ID_SUCCESS]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_GET_TRACK_BY_ID_SUCCESS
     * PURPOSE: Проверка получения трека по ID с правами.
     *
     * @PreConditions:
     * - трек существует
     * - пользователь имеет права VIEW
     *
     * @PostConditions:
     * - возвращается TrackDto
     *
     * @Invariants:
     * - проверка прав доступа обязательна
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку прав доступа
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку дополнительных полей
     */
    @Test
    void getTrackById_Success() {
        // Arrange
        when(trackRepository.findByIdWithOwner(TRACK_ID)).thenReturn(Optional.of(testTrack));
        when(permissionService.hasPermission(TRACK_ID, USER_ID, "VIEW")).thenReturn(true);
        when(permissionService.getUserRole(TRACK_ID, USER_ID)).thenReturn("OWNER");

        // Act
        TrackDto result = trackService.getTrackById(TRACK_ID, USER_ID);

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(TRACK_ID, result.getId(), "Id должен соответствовать");
        assertEquals("Test Track", result.getTitle(), "Название должно соответствовать");
        assertEquals("OWNER", result.getMyRole(), "Роль должна быть OWNER");

        verify(trackRepository).findByIdWithOwner(TRACK_ID);
        verify(permissionService).hasPermission(TRACK_ID, USER_ID, "VIEW");
        verify(permissionService).getUserRole(TRACK_ID, USER_ID);
    }
    // [END_TRACK_SERVICE_TEST_GET_TRACK_BY_ID_SUCCESS]

    // [START_TRACK_SERVICE_TEST_GET_TRACK_BY_ID_NOT_FOUND]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_GET_TRACK_BY_ID_NOT_FOUND
     * PURPOSE: Проверка получения несуществующего трека.
     *
     * @PreConditions:
     * - трек не существует
     *
     * @PostConditions:
     * - выбрасывается ResourceNotFoundException
     *
     * @Invariants:
     * - несуществующий трек не может быть получен
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования трека
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void getTrackById_NotFound() {
        // Arrange
        when(trackRepository.findByIdWithOwner(TRACK_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trackService.getTrackById(TRACK_ID, USER_ID),
                "Должно выбрасываться ResourceNotFoundException при отсутствии трека");

        verify(trackRepository).findByIdWithOwner(TRACK_ID);
        verify(permissionService, never()).hasPermission(anyLong(), anyLong(), anyString());
    }
    // [END_TRACK_SERVICE_TEST_GET_TRACK_BY_ID_NOT_FOUND]

    // [START_TRACK_SERVICE_TEST_GET_TRACK_BY_ID_ACCESS_DENIED]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_GET_TRACK_BY_ID_ACCESS_DENIED
     * PURPOSE: Проверка получения трека без прав доступа.
     *
     * @PreConditions:
     * - трек существует
     * - пользователь не имеет прав VIEW
     *
     * @PostConditions:
     * - выбрасывается AccessDeniedException
     *
     * @Invariants:
     * - без прав доступа трек не может быть получен
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку прав доступа
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void getTrackById_AccessDenied() {
        // Arrange
        when(trackRepository.findByIdWithOwner(TRACK_ID)).thenReturn(Optional.of(testTrack));
        when(permissionService.hasPermission(TRACK_ID, USER_ID, "VIEW")).thenReturn(false);

        // Act & Assert
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> trackService.getTrackById(TRACK_ID, USER_ID),
                "Должно выбрасываться AccessDeniedException при отсутствии прав");

        verify(trackRepository).findByIdWithOwner(TRACK_ID);
        verify(permissionService).hasPermission(TRACK_ID, USER_ID, "VIEW");
    }
    // [END_TRACK_SERVICE_TEST_GET_TRACK_BY_ID_ACCESS_DENIED]

    // [START_TRACK_SERVICE_TEST_CREATE_TRACK_SUCCESS]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_CREATE_TRACK_SUCCESS
     * PURPOSE: Проверка создания нового трека.
     *
     * @PreConditions:
     * - пользователь существует
     *
     * @PostConditions:
     * - возвращается TrackDto с id нового трека
     *
     * @Invariants:
     * - трек всегда имеет владельца
     * - временные метки установлены
     *
     * @SideEffects:
     * - трек сохраняется в БД
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования пользователя
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку дополнительных полей
     */
    @Test
    void createTrack_Success() {
        // Arrange
        Track newTrack = Track.builder()
                .id(TRACK_ID)
                .title("New Track")
                .description("New Description")
                .owner(testUser)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(trackRepository.save(any(Track.class))).thenReturn(newTrack);
        when(permissionService.getUserRole(TRACK_ID, USER_ID)).thenReturn("OWNER");

        // Act
        TrackDto result = trackService.createTrack(createTrackRequest, USER_ID);

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(TRACK_ID, result.getId(), "Id должен соответствовать");
        assertEquals("New Track", result.getTitle(), "Название должно соответствовать");
        assertEquals("New Description", result.getDescription(), "Описание должно соответствовать");
        assertEquals("OWNER", result.getMyRole(), "Роль должна быть OWNER");

        verify(userRepository).findById(USER_ID);
        verify(trackRepository).save(any(Track.class));
    }
    // [END_TRACK_SERVICE_TEST_CREATE_TRACK_SUCCESS]

    // [START_TRACK_SERVICE_TEST_CREATE_TRACK_USER_NOT_FOUND]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_CREATE_TRACK_USER_NOT_FOUND
     * PURPOSE: Проверка создания трека с несуществующим пользователем.
     *
     * @PreConditions:
     * - пользователь не существует
     *
     * @PostConditions:
     * - выбрасывается ResourceNotFoundException
     *
     * @Invariants:
     * - несуществующий пользователь не может создать трек
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования пользователя
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void createTrack_UserNotFound() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trackService.createTrack(createTrackRequest, USER_ID),
                "Должно выбрасываться ResourceNotFoundException при отсутствии пользователя");

        verify(userRepository).findById(USER_ID);
        verify(trackRepository, never()).save(any(Track.class));
    }
    // [END_TRACK_SERVICE_TEST_CREATE_TRACK_USER_NOT_FOUND]

    // [START_TRACK_SERVICE_TEST_CREATE_TRACK_TIMESTAMPS_SET]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_CREATE_TRACK_TIMESTAMPS_SET
     * PURPOSE: Проверка установки временных меток при создании трека.
     *
     * @PreConditions:
     * - пользователь существует
     *
     * @PostConditions:
     * - createdAt и updatedAt установлены
     *
     * @Invariants:
     * - временные метки всегда установлены
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать установку временных меток
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку значений временных меток
     */
    @Test
    void createTrack_TimestampsSet() {
        // Arrange
        Track newTrack = Track.builder()
                .id(TRACK_ID)
                .title("New Track")
                .description("New Description")
                .owner(testUser)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(trackRepository.save(any(Track.class))).thenReturn(newTrack);
        when(permissionService.getUserRole(TRACK_ID, USER_ID)).thenReturn("OWNER");

        // Act
        TrackDto result = trackService.createTrack(createTrackRequest, USER_ID);

        // Assert
        assertNotNull(result.getCreatedAt(), "CreatedAt должен быть установлен");
        assertNotNull(result.getUpdatedAt(), "UpdatedAt должен быть установлен");

        verify(trackRepository).save(argThat(track ->
                track.getCreatedAt() != null && track.getUpdatedAt() != null));
    }
    // [END_TRACK_SERVICE_TEST_CREATE_TRACK_TIMESTAMPS_SET]

    // [START_TRACK_SERVICE_TEST_UPDATE_TRACK_SUCCESS]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_UPDATE_TRACK_SUCCESS
     * PURPOSE: Проверка обновления трека с правами.
     *
     * @PreConditions:
     * - трек существует
     * - пользователь имеет права EDIT
     *
     * @PostConditions:
     * - возвращается обновленный TrackDto
     *
     * @Invariants:
     * - updatedAt обновляется
     * - проверка прав доступа обязательна
     *
     * @SideEffects:
     * - трек обновляется в БД
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку прав доступа
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку дополнительных полей
     */
    @Test
    void updateTrack_Success() {
        // Arrange
        Track updatedTrack = Track.builder()
                .id(TRACK_ID)
                .title("Updated Track")
                .description("Updated Description")
                .owner(testUser)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(trackRepository.findById(TRACK_ID)).thenReturn(Optional.of(testTrack));
        when(permissionService.hasPermission(TRACK_ID, USER_ID, "EDIT")).thenReturn(true);
        when(trackRepository.save(any(Track.class))).thenReturn(updatedTrack);
        when(permissionService.getUserRole(TRACK_ID, USER_ID)).thenReturn("OWNER");

        // Act
        TrackDto result = trackService.updateTrack(TRACK_ID, updateTrackRequest, USER_ID);

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals("Updated Track", result.getTitle(), "Название должно быть обновлено");
        assertEquals("Updated Description", result.getDescription(), "Описание должно быть обновлено");

        verify(trackRepository).findById(TRACK_ID);
        verify(permissionService).hasPermission(TRACK_ID, USER_ID, "EDIT");
        verify(trackRepository).save(any(Track.class));
    }
    // [END_TRACK_SERVICE_TEST_UPDATE_TRACK_SUCCESS]

    // [START_TRACK_SERVICE_TEST_UPDATE_TRACK_NOT_FOUND]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_UPDATE_TRACK_NOT_FOUND
     * PURPOSE: Проверка обновления несуществующего трека.
     *
     * @PreConditions:
     * - трек не существует
     *
     * @PostConditions:
     * - выбрасывается ResourceNotFoundException
     *
     * @Invariants:
     * - несуществующий трек не может быть обновлён
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования трека
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void updateTrack_NotFound() {
        // Arrange
        when(trackRepository.findById(TRACK_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trackService.updateTrack(TRACK_ID, updateTrackRequest, USER_ID),
                "Должно выбрасываться ResourceNotFoundException при отсутствии трека");

        verify(trackRepository).findById(TRACK_ID);
        verify(permissionService, never()).hasPermission(anyLong(), anyLong(), anyString());
        verify(trackRepository, never()).save(any(Track.class));
    }
    // [END_TRACK_SERVICE_TEST_UPDATE_TRACK_NOT_FOUND]

    // [START_TRACK_SERVICE_TEST_UPDATE_TRACK_ACCESS_DENIED]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_UPDATE_TRACK_ACCESS_DENIED
     * PURPOSE: Проверка обновления трека без прав EDIT.
     *
     * @PreConditions:
     * - трек существует
     * - пользователь не имеет прав EDIT
     *
     * @PostConditions:
     * - выбрасывается AccessDeniedException
     *
     * @Invariants:
     * - без прав EDIT трек не может быть обновлён
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку прав доступа
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void updateTrack_AccessDenied() {
        // Arrange
        when(trackRepository.findById(TRACK_ID)).thenReturn(Optional.of(testTrack));
        when(permissionService.hasPermission(TRACK_ID, USER_ID, "EDIT")).thenReturn(false);

        // Act & Assert
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> trackService.updateTrack(TRACK_ID, updateTrackRequest, USER_ID),
                "Должно выбрасываться AccessDeniedException при отсутствии прав EDIT");

        verify(trackRepository).findById(TRACK_ID);
        verify(permissionService).hasPermission(TRACK_ID, USER_ID, "EDIT");
        verify(trackRepository, never()).save(any(Track.class));
    }
    // [END_TRACK_SERVICE_TEST_UPDATE_TRACK_ACCESS_DENIED]

    // [START_TRACK_SERVICE_TEST_UPDATE_TRACK_UPDATED_AT]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_UPDATE_TRACK_UPDATED_AT
     * PURPOSE: Проверка обновления updatedAt при изменении трека.
     *
     * @PreConditions:
     * - трек существует
     * - пользователь имеет права EDIT
     *
     * @PostConditions:
     * - updatedAt обновлён
     *
     * @Invariants:
     * - updatedAt всегда обновляется при изменении
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать обновление updatedAt
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку значения updatedAt
     */
    @Test
    void updateTrack_UpdatedAt() {
        // Arrange
        Instant oldUpdatedAt = testTrack.getUpdatedAt();
        Track updatedTrack = Track.builder()
                .id(TRACK_ID)
                .title("Updated Track")
                .description("Updated Description")
                .owner(testUser)
                .createdAt(testTrack.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        when(trackRepository.findById(TRACK_ID)).thenReturn(Optional.of(testTrack));
        when(permissionService.hasPermission(TRACK_ID, USER_ID, "EDIT")).thenReturn(true);
        when(trackRepository.save(any(Track.class))).thenReturn(updatedTrack);
        when(permissionService.getUserRole(TRACK_ID, USER_ID)).thenReturn("OWNER");

        // Act
        TrackDto result = trackService.updateTrack(TRACK_ID, updateTrackRequest, USER_ID);

        // Assert
        assertNotNull(result.getUpdatedAt(), "UpdatedAt должен быть установлен");

        verify(trackRepository).save(argThat(track ->
                track.getUpdatedAt() != null));
    }
    // [END_TRACK_SERVICE_TEST_UPDATE_TRACK_UPDATED_AT]

    // [START_TRACK_SERVICE_TEST_DELETE_TRACK_SUCCESS]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_DELETE_TRACK_SUCCESS
     * PURPOSE: Проверка удаления трека владельцем.
     *
     * @PreConditions:
     * - трек существует
     * - пользователь является владельцем
     *
     * @PostConditions:
     * - трек удалён
     *
     * @Invariants:
     * - только владелец может удалить трек
     *
     * @SideEffects:
     * - трек удаляется из БД
     *
     * @ForbiddenChanges:
     * - нельзя разрешить удаление не владельцу
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку каскадного удаления
     */
    @Test
    void deleteTrack_Success() {
        // Arrange
        when(trackRepository.findById(TRACK_ID)).thenReturn(Optional.of(testTrack));

        // Act
        trackService.deleteTrack(TRACK_ID, USER_ID);

        // Assert
        verify(trackRepository).findById(TRACK_ID);
        verify(trackRepository).delete(testTrack);
    }
    // [END_TRACK_SERVICE_TEST_DELETE_TRACK_SUCCESS]

    // [START_TRACK_SERVICE_TEST_DELETE_TRACK_NOT_FOUND]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_DELETE_TRACK_NOT_FOUND
     * PURPOSE: Проверка удаления несуществующего трека.
     *
     * @PreConditions:
     * - трек не существует
     *
     * @PostConditions:
     * - выбрасывается ResourceNotFoundException
     *
     * @Invariants:
     * - несуществующий трек не может быть удалён
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования трека
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void deleteTrack_NotFound() {
        // Arrange
        when(trackRepository.findById(TRACK_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trackService.deleteTrack(TRACK_ID, USER_ID),
                "Должно выбрасываться ResourceNotFoundException при отсутствии трека");

        verify(trackRepository).findById(TRACK_ID);
        verify(trackRepository, never()).delete(any(Track.class));
    }
    // [END_TRACK_SERVICE_TEST_DELETE_TRACK_NOT_FOUND]

    // [START_TRACK_SERVICE_TEST_DELETE_TRACK_ACCESS_DENIED]
    /*
     * ANCHOR: TRACK_SERVICE_TEST_DELETE_TRACK_ACCESS_DENIED
     * PURPOSE: Проверка удаления трека не владельцем.
     *
     * @PreConditions:
     * - трек существует
     * - пользователь не является владельцем
     *
     * @PostConditions:
     * - выбрасывается AccessDeniedException
     *
     * @Invariants:
     * - только владелец может удалить трек
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя разрешить удаление не владельцу
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void deleteTrack_AccessDenied() {
        // Arrange
        User otherUser = User.builder()
                .id(OTHER_USER_ID)
                .username("otheruser")
                .email("other@example.com")
                .role("USER")
                .enabled(true)
                .build();

        Track otherTrack = Track.builder()
                .id(TRACK_ID)
                .title("Other Track")
                .description("Other Description")
                .owner(otherUser)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(trackRepository.findById(TRACK_ID)).thenReturn(Optional.of(otherTrack));

        // Act & Assert
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> trackService.deleteTrack(TRACK_ID, USER_ID),
                "Должно выбрасываться AccessDeniedException при попытке удаления не владельцем");

        verify(trackRepository).findById(TRACK_ID);
        verify(trackRepository, never()).delete(any(Track.class));
    }
    // [END_TRACK_SERVICE_TEST_DELETE_TRACK_ACCESS_DENIED]
}
// [END_TRACK_SERVICE_TEST]
// === END_CHUNK: TRACK_SERVICE_TEST ===
