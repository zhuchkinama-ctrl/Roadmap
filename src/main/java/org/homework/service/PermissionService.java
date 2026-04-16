// === CHUNK: PERMISSION_SERVICE [SERVICE] ===
// Описание: Сервис для проверки прав доступа к трекам.
// Dependencies: Spring

package org.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.homework.model.Track;
import org.homework.model.TrackPermission;
import org.homework.model.User;
import org.homework.repository.TrackPermissionRepository;
import org.homework.repository.TrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// [START_PERMISSION_SERVICE]
/*
 * ANCHOR: PERMISSION_SERVICE
 * PURPOSE: Сервис для проверки прав доступа к трекам.
 *
 * @PreConditions:
 * - Spring контекст инициализирован
 * - репозитории доступны
 *
 * @PostConditions:
 * - сервис готов к проверке прав доступа
 *
 * @Invariants:
 * - ADMIN имеет доступ ко всем трекам
 * - OWNER имеет все права на свои треки
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя изменить логику проверки прав без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить кэширование результатов проверки
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {
    
    /**
     * Типы прав доступа.
     */
    public enum Permission {
        VIEW,
        EDIT
    }
    
    private final TrackRepository trackRepository;
    private final TrackPermissionRepository trackPermissionRepository;
    
    /**
     * Проверить, имеет ли пользователь право на трек.
     */
    public boolean hasPermission(Long trackId, Long userId, String requiredPermission) {
        log.info("PERMISSION_SERVICE CHECK_PERMISSION - trackId: {}, userId: {}, requiredPermission: {}", 
                 trackId, userId, requiredPermission);
        
        // Проверка владельца
        boolean isOwner = trackRepository.isOwner(trackId, userId);
        if (isOwner) {
            log.info("PERMISSION_SERVICE CHECK_PERMISSION - user is owner");
            return true;
        }
        
        // Проверка прав доступа
        Optional<TrackPermission> permission = trackPermissionRepository
                .findByTrackIdAndUserId(trackId, userId);
        
        if (permission.isPresent()) {
            TrackPermission perm = permission.get();
            boolean hasAccess = switch (requiredPermission) {
                case "VIEW" -> true; // VIEW и EDIT имеют право на чтение
                case "EDIT" -> "EDIT".equals(perm.getPermissionType());
                default -> false;
            };
            log.info("PERMISSION_SERVICE CHECK_PERMISSION - hasAccess: {}", hasAccess);
            return hasAccess;
        }
        
        log.info("PERMISSION_SERVICE CHECK_PERMISSION - no permission found");
        return false;
    }
    
    /**
     * Проверить, имеет ли пользователь право на трек (с выбросом исключения).
     */
    public void checkPermission(Long trackId, Long userId, Permission requiredPermission) {
        if (!hasPermission(trackId, userId, requiredPermission.name())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "User does not have " + requiredPermission + " permission on track " + trackId);
        }
    }
    
    /**
     * Получить роль пользователя для трека.
     */
    public String getUserRole(Long trackId, Long userId) {
        log.info("PERMISSION_SERVICE GET_USER_ROLE - trackId: {}, userId: {}", trackId, userId);
        
        // Проверка владельца
        if (trackRepository.isOwner(trackId, userId)) {
            log.info("PERMISSION_SERVICE GET_USER_ROLE - user is owner");
            return "OWNER";
        }
        
        // Проверка прав доступа
        Optional<TrackPermission> permission = trackPermissionRepository
                .findByTrackIdAndUserId(trackId, userId);
        
        if (permission.isPresent()) {
            String role = permission.get().getPermissionType();
            log.info("PERMISSION_SERVICE GET_USER_ROLE - role: {}", role);
            return role;
        }
        
        log.info("PERMISSION_SERVICE GET_USER_ROLE - no role found");
        return null;
    }
}
// [END_PERMISSION_SERVICE]
// === END_CHUNK: PERMISSION_SERVICE ===
