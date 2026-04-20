package org.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.homework.dto.request.CreateTrackRequest;
import org.homework.dto.request.UpdateTrackRequest;
import org.homework.dto.response.TrackDto;
import org.homework.dto.response.TrackSummaryDto;
import org.homework.exception.ResourceNotFoundException;
import org.homework.service.NoteService;
import org.homework.service.TrackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// === CHUNK: TRACK_CONTROLLER_TEST [TEST] ===
// Описание: Юнит-тесты для TrackController.
// Dependencies: JUnit 5, Spring MVC Test, Mockito

// [START_TRACK_CONTROLLER_TEST]
/*
 * ANCHOR: TRACK_CONTROLLER_TEST
 * PURPOSE: Юнит-тесты для TrackController.
 *
 * @PreConditions:
 * - TrackController инициализирован с моками зависимостей
 *
 * @PostConditions:
 * - все тесты проверяют контракты методов TrackController
 *
 * @Invariants:
 * - все эндпоинты требуют аутентификации
 * - проверка прав доступа обязательна
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку аутентификации
 * - нельзя убрать проверку прав доступа
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тестовые кейсы
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.security.enabled=false"
})
class TrackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrackService trackService;

    @MockBean
    private NoteService noteService;

    private static final Long USER_ID = 100L;
    private static final Long TRACK_ID = 1L;

    private TrackDto testTrackDto;
    private TrackSummaryDto testTrackSummaryDto;
    private CreateTrackRequest createTrackRequest;
    private UpdateTrackRequest updateTrackRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testTrackDto = TrackDto.builder()
                .id(TRACK_ID)
                .title("Test Track")
                .description("Test Description")
                .owner(null)
                .myRole("OWNER")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testTrackSummaryDto = TrackSummaryDto.builder()
                .id(TRACK_ID)
                .title("Test Track")
                .description("Test Description")
                .owner(null)
                .myRole("OWNER")
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

    // [START_TRACK_CONTROLLER_TEST_GET_ALL_SUCCESS]
    /*
     * ANCHOR: TRACK_CONTROLLER_TEST_GET_ALL_SUCCESS
     * PURPOSE: Проверка получения списка треков.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     * - у пользователя есть треки
     *
     * @PostConditions:
     * - возвращается Page с треками
     *
     * @Invariants:
     * - эндпоинт требует аутентификации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку пагинации
     */
    @Test
    void getAll_Success() throws Exception {
        // Arrange
        List<TrackSummaryDto> tracks = Collections.singletonList(testTrackSummaryDto);
        Page<TrackSummaryDto> trackPage = new PageImpl<>(tracks, pageable, 1);

        when(trackService.getUserTracks(eq(USER_ID), any(Pageable.class)))
                .thenReturn(trackPage);

        // Создаем кастомный Authentication с Long principal
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act & Assert
        mockMvc.perform(get("/api/v1/tracks")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "updatedAt,desc")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(TRACK_ID))
                .andExpect(jsonPath("$.content[0].title").value("Test Track"))
                .andExpect(jsonPath("$.content[0].description").value("Test Description"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(trackService, times(1)).getUserTracks(eq(USER_ID), any(Pageable.class));
    }
    // [END_TRACK_CONTROLLER_TEST_GET_ALL_SUCCESS]

    // [START_TRACK_CONTROLLER_TEST_GET_ALL_EMPTY]
    /*
     * ANCHOR: TRACK_CONTROLLER_TEST_GET_ALL_EMPTY
     * PURPOSE: Проверка получения пустого списка треков.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     * - у пользователя нет треков
     *
     * @PostConditions:
     * - возвращается пустой Page
     *
     * @Invariants:
     * - эндпоинт требует аутентификации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку пагинации
     */
    @Test
    void getAll_Empty() throws Exception {
        // Arrange
        Page<TrackSummaryDto> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(trackService.getUserTracks(eq(USER_ID), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Создаем кастомный Authentication с Long principal
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act & Assert
        mockMvc.perform(get("/api/v1/tracks")
                        .param("page", "0")
                        .param("size", "10")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(trackService, times(1)).getUserTracks(eq(USER_ID), any(Pageable.class));
    }
    // [END_TRACK_CONTROLLER_TEST_GET_ALL_EMPTY]

    // [START_TRACK_CONTROLLER_TEST_GET_BY_ID_SUCCESS]
    /*
     * ANCHOR: TRACK_CONTROLLER_TEST_GET_BY_ID_SUCCESS
     * PURPOSE: Проверка получения трека по ID.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     * - трек существует
     * - пользователь имеет право доступа
     *
     * @PostConditions:
     * - возвращается TrackDto
     *
     * @Invariants:
     * - эндпоинт требует аутентификации
     * - проверка прав доступа обязательна
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     * - нельзя убрать проверку прав доступа
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку полей
     */
    @Test
    void getById_Success() throws Exception {
        // Arrange
        when(trackService.getTrackById(eq(TRACK_ID), eq(USER_ID)))
                .thenReturn(testTrackDto);

        // Создаем кастомный Authentication с Long principal
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act & Assert
        mockMvc.perform(get("/api/v1/tracks/{id}", TRACK_ID)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TRACK_ID))
                .andExpect(jsonPath("$.title").value("Test Track"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(trackService, times(1)).getTrackById(eq(TRACK_ID), eq(USER_ID));
    }
    // [END_TRACK_CONTROLLER_TEST_GET_BY_ID_SUCCESS]

    // [START_TRACK_CONTROLLER_TEST_GET_BY_ID_NOT_FOUND]
    /*
     * ANCHOR: TRACK_CONTROLLER_TEST_GET_BY_ID_NOT_FOUND
     * PURPOSE: Проверка получения несуществующего трека.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     * - трек не существует
     *
     * @PostConditions:
     * - возвращается 404 Not Found
     *
     * @Invariants:
     * - эндпоинт требует аутентификации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void getById_NotFound() throws Exception {
        // Arrange
        when(trackService.getTrackById(eq(TRACK_ID), eq(USER_ID)))
                .thenThrow(new ResourceNotFoundException("Track not found"));

        // Создаем кастомный Authentication с Long principal
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act & Assert
        mockMvc.perform(get("/api/v1/tracks/{id}", TRACK_ID)
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound());

        verify(trackService, times(1)).getTrackById(eq(TRACK_ID), eq(USER_ID));
    }
    // [END_TRACK_CONTROLLER_TEST_GET_BY_ID_NOT_FOUND]

    // [START_TRACK_CONTROLLER_TEST_CREATE_SUCCESS]
    /*
     * ANCHOR: TRACK_CONTROLLER_TEST_CREATE_SUCCESS
     * PURPOSE: Проверка создания нового трека.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     * - request валиден
     *
     * @PostConditions:
     * - возвращается созданный TrackDto
     *
     * @Invariants:
     * - эндпоинт требует аутентификации
     *
     * @SideEffects:
     * - трек создается в БД
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     * - нельзя убрать валидацию
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку полей
     */
    @Test
    void create_Success() throws Exception {
        // Arrange
        when(trackService.createTrack(any(CreateTrackRequest.class), eq(USER_ID)))
                .thenReturn(testTrackDto);

        // Создаем кастомный Authentication с Long principal
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/tracks")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTrackRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TRACK_ID))
                .andExpect(jsonPath("$.title").value("Test Track"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(trackService, times(1)).createTrack(any(CreateTrackRequest.class), eq(USER_ID));
    }
    // [END_TRACK_CONTROLLER_TEST_CREATE_SUCCESS]

    // [START_TRACK_CONTROLLER_TEST_CREATE_VALIDATION_ERROR]
    /*
     * ANCHOR: TRACK_CONTROLLER_TEST_CREATE_VALIDATION_ERROR
     * PURPOSE: Проверка валидации при создании трека.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     * - request невалиден
     *
     * @PostConditions:
     * - возвращается 400 Bad Request
     *
     * @Invariants:
     * - эндпоинт требует аутентификации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     * - нельзя убрать валидацию
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщений об ошибке
     */
    @Test
    void create_ValidationError() throws Exception {
        // Arrange
        createTrackRequest.setTitle(""); // Пустой заголовок

        // Создаем кастомный Authentication с Long principal
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/tracks")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTrackRequest)))
                .andExpect(status().isBadRequest());

        verify(trackService, never()).createTrack(any(CreateTrackRequest.class), anyLong());
    }
    // [END_TRACK_CONTROLLER_TEST_CREATE_VALIDATION_ERROR]

    // [START_TRACK_CONTROLLER_TEST_UPDATE_SUCCESS]
    /*
     * ANCHOR: TRACK_CONTROLLER_TEST_UPDATE_SUCCESS
     * PURPOSE: Проверка обновления трека.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     * - трек существует
     * - пользователь имеет право EDIT
     * - request валиден
     *
     * @PostConditions:
     * - возвращается обновленный TrackDto
     *
     * @Invariants:
     * - эндпоинт требует аутентификации
     * - проверка прав доступа обязательна
     *
     * @SideEffects:
     * - трек обновляется в БД
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     * - нельзя убрать проверку прав доступа
     * - нельзя убрать валидацию
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку полей
     */
    @Test
    void update_Success() throws Exception {
        // Arrange
        TrackDto updatedTrack = TrackDto.builder()
                .id(TRACK_ID)
                .title("Updated Track")
                .description("Updated Description")
                .owner(null)
                .myRole("OWNER")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(trackService.updateTrack(eq(TRACK_ID), any(UpdateTrackRequest.class), eq(USER_ID)))
                .thenReturn(updatedTrack);

        // Создаем кастомный Authentication с Long principal
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act & Assert
        mockMvc.perform(put("/api/v1/tracks/{id}", TRACK_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTrackRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TRACK_ID))
                .andExpect(jsonPath("$.title").value("Updated Track"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        verify(trackService, times(1)).updateTrack(eq(TRACK_ID), any(UpdateTrackRequest.class), eq(USER_ID));
    }
    // [END_TRACK_CONTROLLER_TEST_UPDATE_SUCCESS]

    // [START_TRACK_CONTROLLER_TEST_UPDATE_NOT_FOUND]
    /*
     * ANCHOR: TRACK_CONTROLLER_TEST_UPDATE_NOT_FOUND
     * PURPOSE: Проверка обновления несуществующего трека.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     * - трек не существует
     *
     * @PostConditions:
     * - возвращается 404 Not Found
     *
     * @Invariants:
     * - эндпоинт требует аутентификации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void update_NotFound() throws Exception {
        // Arrange
        when(trackService.updateTrack(eq(TRACK_ID), any(UpdateTrackRequest.class), eq(USER_ID)))
                .thenThrow(new ResourceNotFoundException("Track not found"));

        // Создаем кастомный Authentication с Long principal
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act & Assert
        mockMvc.perform(put("/api/v1/tracks/{id}", TRACK_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTrackRequest)))
                .andExpect(status().isNotFound());

        verify(trackService, times(1)).updateTrack(eq(TRACK_ID), any(UpdateTrackRequest.class), eq(USER_ID));
    }
    // [END_TRACK_CONTROLLER_TEST_UPDATE_NOT_FOUND]

    // [START_TRACK_CONTROLLER_TEST_DELETE_SUCCESS]
    /*
     * ANCHOR: TRACK_CONTROLLER_TEST_DELETE_SUCCESS
     * PURPOSE: Проверка удаления трека.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     * - трек существует
     * - пользователь имеет право OWNER
     *
     * @PostConditions:
     * - возвращается 200 OK
     *
     * @Invariants:
     * - эндпоинт требует аутентификации
     * - проверка прав доступа обязательна
     *
     * @SideEffects:
     * - трек удаляется из БД
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     * - нельзя убрать проверку прав доступа
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения
     */
    @Test
    void delete_Success() throws Exception {
        // Arrange
        doNothing().when(trackService).deleteTrack(eq(TRACK_ID), eq(USER_ID));

        // Создаем кастомный Authentication с Long principal
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tracks/{id}", TRACK_ID)
                        .with(authentication(authentication)))
                .andExpect(status().isOk());

        verify(trackService, times(1)).deleteTrack(eq(TRACK_ID), eq(USER_ID));
    }
    // [END_TRACK_CONTROLLER_TEST_DELETE_SUCCESS]

    // [START_TRACK_CONTROLLER_TEST_DELETE_NOT_FOUND]
    /*
     * ANCHOR: TRACK_CONTROLLER_TEST_DELETE_NOT_FOUND
     * PURPOSE: Проверка удаления несуществующего трека.
     *
     * @PreConditions:
     * - пользователь аутентифицирован
     * - трек не существует
     *
     * @PostConditions:
     * - возвращается 404 Not Found
     *
     * @Invariants:
     * - эндпоинт требует аутентификации
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку аутентификации
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку сообщения об ошибке
     */
    @Test
    void delete_NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Track not found"))
                .when(trackService).deleteTrack(eq(TRACK_ID), eq(USER_ID));

        // Создаем кастомный Authentication с Long principal
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tracks/{id}", TRACK_ID)
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound());

        verify(trackService, times(1)).deleteTrack(eq(TRACK_ID), eq(USER_ID));
    }
    // [END_TRACK_CONTROLLER_TEST_DELETE_NOT_FOUND]
}
// [END_TRACK_CONTROLLER_TEST]
// === END_CHUNK: TRACK_CONTROLLER_TEST ===
