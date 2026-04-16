package org.homework.service;

import org.homework.model.TrackPermission;
import org.homework.repository.TrackPermissionRepository;
import org.homework.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// === CHUNK: PERMISSION_SERVICE_TEST [TEST] ===
// Описание: Юнит-тесты для PermissionService.
// Dependencies: JUnit 5, Mockito

// [START_PERMISSION_SERVICE_TEST]
/*
 * ANCHOR: PERMISSION_SERVICE_TEST
 * PURPOSE: Юнит-тесты для PermissionService.
 *
 * @PreConditions:
 * - PermissionService инициализирован с моками репозиториев
 *
 * @PostConditions:
 * - все тесты проверяют контракты методов PermissionService
 *
 * @Invariants:
 * - владелец имеет все права
 * - VIEW доступен для VIEW и EDIT
 * - EDIT доступен только для EDIT
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку прав владельца
 * - нельзя убрать проверку прав доступа
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные тестовые кейсы
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private TrackPermissionRepository trackPermissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    private static final Long TRACK_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long OTHER_USER_ID = 200L;

    // [START_PERMISSION_SERVICE_TEST_HAS_PERMISSION_OWNER]
    /*
     * ANCHOR: PERMISSION_SERVICE_TEST_HAS_PERMISSION_OWNER
     * PURPOSE: Проверка прав владельца трека.
     *
     * @PreConditions:
     * - пользователь является владельцем трека
     *
     * @PostConditions:
     * - возвращается true для любого права
     *
     * @Invariants:
     * - владелец имеет все права
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку прав владельца
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных прав
     */
    @Test
    void hasPermission_Owner() {
        // Arrange
        when(trackRepository.isOwner(TRACK_ID, USER_ID)).thenReturn(true);

        // Act
        boolean hasViewPermission = permissionService.hasPermission(TRACK_ID, USER_ID, "VIEW");
        boolean hasEditPermission = permissionService.hasPermission(TRACK_ID, USER_ID, "EDIT");

        // Assert
        assertTrue(hasViewPermission, "Владелец должен иметь право VIEW");
        assertTrue(hasEditPermission, "Владелец должен иметь право EDIT");

        verify(trackRepository, times(2)).isOwner(TRACK_ID, USER_ID);
        verify(trackPermissionRepository, never()).findByTrackIdAndUserId(anyLong(), anyLong());
    }
    // [END_PERMISSION_SERVICE_TEST_HAS_PERMISSION_OWNER]

    // [START_PERMISSION_SERVICE_TEST_HAS_PERMISSION_VIEW_PERMISSION]
    /*
     * ANCHOR: PERMISSION_SERVICE_TEST_HAS_PERMISSION_VIEW_PERMISSION
     * PURPOSE: Проверка прав пользователя с правом VIEW.
     *
     * @PreConditions:
     * - пользователь имеет право VIEW на трек
     *
     * @PostConditions:
     * - возвращается true для VIEW
     * - возвращается false для EDIT
     *
     * @Invariants:
     * - VIEW доступен только для чтения
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку права VIEW
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для других прав
     */
    @Test
    void hasPermission_ViewPermission() {
        // Arrange
        when(trackRepository.isOwner(TRACK_ID, OTHER_USER_ID)).thenReturn(false);
        
        TrackPermission viewPermission = TrackPermission.builder()
                .id(1L)
                .permissionType("VIEW")
                .build();
        when(trackPermissionRepository.findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID))
                .thenReturn(Optional.of(viewPermission));

        // Act
        boolean hasViewPermission = permissionService.hasPermission(TRACK_ID, OTHER_USER_ID, "VIEW");
        boolean hasEditPermission = permissionService.hasPermission(TRACK_ID, OTHER_USER_ID, "EDIT");

        // Assert
        assertTrue(hasViewPermission, "Пользователь с правом VIEW должен иметь право VIEW");
        assertFalse(hasEditPermission, "Пользователь с правом VIEW не должен иметь право EDIT");

        verify(trackRepository, times(2)).isOwner(TRACK_ID, OTHER_USER_ID);
        verify(trackPermissionRepository, times(2)).findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID);
    }
    // [END_PERMISSION_SERVICE_TEST_HAS_PERMISSION_VIEW_PERMISSION]

    // [START_PERMISSION_SERVICE_TEST_HAS_PERMISSION_EDIT_PERMISSION]
    /*
     * ANCHOR: PERMISSION_SERVICE_TEST_HAS_PERMISSION_EDIT_PERMISSION
     * PURPOSE: Проверка прав пользователя с правом EDIT.
     *
     * @PreConditions:
     * - пользователь имеет право EDIT на трек
     *
     * @PostConditions:
     * - возвращается true для VIEW
     * - возвращается true для EDIT
     *
     * @Invariants:
     * - EDIT включает право VIEW
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку права EDIT
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для других прав
     */
    @Test
    void hasPermission_EditPermission() {
        // Arrange
        when(trackRepository.isOwner(TRACK_ID, OTHER_USER_ID)).thenReturn(false);
        
        TrackPermission editPermission = TrackPermission.builder()
                .id(1L)
                .permissionType("EDIT")
                .build();
        when(trackPermissionRepository.findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID))
                .thenReturn(Optional.of(editPermission));

        // Act
        boolean hasViewPermission = permissionService.hasPermission(TRACK_ID, OTHER_USER_ID, "VIEW");
        boolean hasEditPermission = permissionService.hasPermission(TRACK_ID, OTHER_USER_ID, "EDIT");

        // Assert
        assertTrue(hasViewPermission, "Пользователь с правом EDIT должен иметь право VIEW");
        assertTrue(hasEditPermission, "Пользователь с правом EDIT должен иметь право EDIT");

        verify(trackRepository, times(2)).isOwner(TRACK_ID, OTHER_USER_ID);
        verify(trackPermissionRepository, times(2)).findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID);
    }
    // [END_PERMISSION_SERVICE_TEST_HAS_PERMISSION_EDIT_PERMISSION]

    // [START_PERMISSION_SERVICE_TEST_HAS_PERMISSION_NO_PERMISSION]
    /*
     * ANCHOR: PERMISSION_SERVICE_TEST_HAS_PERMISSION_NO_PERMISSION
     * PURPOSE: Проверка прав пользователя без прав на трек.
     *
     * @PreConditions:
     * - пользователь не является владельцем
     * - пользователь не имеет прав на трек
     *
     * @PostConditions:
     * - возвращается false для любого права
     *
     * @Invariants:
     * - без прав нет доступа
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку отсутствия прав
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных прав
     */
    @Test
    void hasPermission_NoPermission() {
        // Arrange
        when(trackRepository.isOwner(TRACK_ID, OTHER_USER_ID)).thenReturn(false);
        when(trackPermissionRepository.findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID))
                .thenReturn(Optional.empty());

        // Act
        boolean hasViewPermission = permissionService.hasPermission(TRACK_ID, OTHER_USER_ID, "VIEW");
        boolean hasEditPermission = permissionService.hasPermission(TRACK_ID, OTHER_USER_ID, "EDIT");

        // Assert
        assertFalse(hasViewPermission, "Пользователь без прав не должен иметь право VIEW");
        assertFalse(hasEditPermission, "Пользователь без прав не должен иметь право EDIT");

        verify(trackRepository, times(2)).isOwner(TRACK_ID, OTHER_USER_ID);
        verify(trackPermissionRepository, times(2)).findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID);
    }
    // [END_PERMISSION_SERVICE_TEST_HAS_PERMISSION_NO_PERMISSION]

    // [START_PERMISSION_SERVICE_TEST_HAS_PERMISSION_INVALID_PERMISSION]
    /*
     * ANCHOR: PERMISSION_SERVICE_TEST_HAS_PERMISSION_INVALID_PERMISSION
     * PURPOSE: Проверка прав с неверным типом права.
     *
     * @PreConditions:
     * - передан неверный тип права
     *
     * @PostConditions:
     * - возвращается false
     *
     * @Invariants:
     * - неверное право не даёт доступ
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку неверного права
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для других неверных прав
     */
    @Test
    void hasPermission_InvalidPermission() {
        // Arrange
        when(trackRepository.isOwner(TRACK_ID, OTHER_USER_ID)).thenReturn(false);
        
        TrackPermission viewPermission = TrackPermission.builder()
                .id(1L)
                .permissionType("VIEW")
                .build();
        when(trackPermissionRepository.findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID))
                .thenReturn(Optional.of(viewPermission));

        // Act
        boolean hasInvalidPermission = permissionService.hasPermission(TRACK_ID, OTHER_USER_ID, "INVALID");

        // Assert
        assertFalse(hasInvalidPermission, "Неверное право не должно давать доступ");

        verify(trackRepository).isOwner(TRACK_ID, OTHER_USER_ID);
        verify(trackPermissionRepository).findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID);
    }
    // [END_PERMISSION_SERVICE_TEST_HAS_PERMISSION_INVALID_PERMISSION]

    // [START_PERMISSION_SERVICE_TEST_GET_USER_ROLE_OWNER]
    /*
     * ANCHOR: PERMISSION_SERVICE_TEST_GET_USER_ROLE_OWNER
     * PURPOSE: Проверка получения роли владельца трека.
     *
     * @PreConditions:
     * - пользователь является владельцем трека
     *
     * @PostConditions:
     * - возвращается "OWNER"
     *
     * @Invariants:
     * - владелец всегда имеет роль OWNER
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку роли владельца
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных пользователей
     */
    @Test
    void getUserRole_Owner() {
        // Arrange
        when(trackRepository.isOwner(TRACK_ID, USER_ID)).thenReturn(true);

        // Act
        String role = permissionService.getUserRole(TRACK_ID, USER_ID);

        // Assert
        assertEquals("OWNER", role, "Владелец должен иметь роль OWNER");

        verify(trackRepository).isOwner(TRACK_ID, USER_ID);
        verify(trackPermissionRepository, never()).findByTrackIdAndUserId(anyLong(), anyLong());
    }
    // [END_PERMISSION_SERVICE_TEST_GET_USER_ROLE_OWNER]

    // [START_PERMISSION_SERVICE_TEST_GET_USER_ROLE_VIEW_PERMISSION]
    /*
     * ANCHOR: PERMISSION_SERVICE_TEST_GET_USER_ROLE_VIEW_PERMISSION
     * PURPOSE: Проверка получения роли пользователя с правом VIEW.
     *
     * @PreConditions:
     * - пользователь имеет право VIEW на трек
     *
     * @PostConditions:
     * - возвращается "VIEW"
     *
     * @Invariants:
     * - роль соответствует типу права
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку роли VIEW
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для других ролей
     */
    @Test
    void getUserRole_ViewPermission() {
        // Arrange
        when(trackRepository.isOwner(TRACK_ID, OTHER_USER_ID)).thenReturn(false);
        
        TrackPermission viewPermission = TrackPermission.builder()
                .id(1L)
                .permissionType("VIEW")
                .build();
        when(trackPermissionRepository.findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID))
                .thenReturn(Optional.of(viewPermission));

        // Act
        String role = permissionService.getUserRole(TRACK_ID, OTHER_USER_ID);

        // Assert
        assertEquals("VIEW", role, "Пользователь должен иметь роль VIEW");

        verify(trackRepository).isOwner(TRACK_ID, OTHER_USER_ID);
        verify(trackPermissionRepository).findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID);
    }
    // [END_PERMISSION_SERVICE_TEST_GET_USER_ROLE_VIEW_PERMISSION]

    // [START_PERMISSION_SERVICE_TEST_GET_USER_ROLE_EDIT_PERMISSION]
    /*
     * ANCHOR: PERMISSION_SERVICE_TEST_GET_USER_ROLE_EDIT_PERMISSION
     * PURPOSE: Проверка получения роли пользователя с правом EDIT.
     *
     * @PreConditions:
     * - пользователь имеет право EDIT на трек
     *
     * @PostConditions:
     * - возвращается "EDIT"
     *
     * @Invariants:
     * - роль соответствует типу права
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку роли EDIT
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для других ролей
     */
    @Test
    void getUserRole_EditPermission() {
        // Arrange
        when(trackRepository.isOwner(TRACK_ID, OTHER_USER_ID)).thenReturn(false);
        
        TrackPermission editPermission = TrackPermission.builder()
                .id(1L)
                .permissionType("EDIT")
                .build();
        when(trackPermissionRepository.findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID))
                .thenReturn(Optional.of(editPermission));

        // Act
        String role = permissionService.getUserRole(TRACK_ID, OTHER_USER_ID);

        // Assert
        assertEquals("EDIT", role, "Пользователь должен иметь роль EDIT");

        verify(trackRepository).isOwner(TRACK_ID, OTHER_USER_ID);
        verify(trackPermissionRepository).findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID);
    }
    // [END_PERMISSION_SERVICE_TEST_GET_USER_ROLE_EDIT_PERMISSION]

    // [START_PERMISSION_SERVICE_TEST_GET_USER_ROLE_NO_PERMISSION]
    /*
     * ANCHOR: PERMISSION_SERVICE_TEST_GET_USER_ROLE_NO_PERMISSION
     * PURPOSE: Проверка получения роли пользователя без прав на трек.
     *
     * @PreConditions:
     * - пользователь не является владельцем
     * - пользователь не имеет прав на трек
     *
     * @PostConditions:
     * - возвращается null
     *
     * @Invariants:
     * - без прав нет роли
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку отсутствия роли
     *
     * @AllowedRefactorZone:
     * - можно добавить проверку для разных пользователей
     */
    @Test
    void getUserRole_NoPermission() {
        // Arrange
        when(trackRepository.isOwner(TRACK_ID, OTHER_USER_ID)).thenReturn(false);
        when(trackPermissionRepository.findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID))
                .thenReturn(Optional.empty());

        // Act
        String role = permissionService.getUserRole(TRACK_ID, OTHER_USER_ID);

        // Assert
        assertNull(role, "Пользователь без прав не должен иметь роли");

        verify(trackRepository).isOwner(TRACK_ID, OTHER_USER_ID);
        verify(trackPermissionRepository).findByTrackIdAndUserId(TRACK_ID, OTHER_USER_ID);
    }
    // [END_PERMISSION_SERVICE_TEST_GET_USER_ROLE_NO_PERMISSION]
}
// [END_PERMISSION_SERVICE_TEST]
// === END_CHUNK: PERMISSION_SERVICE_TEST ===
