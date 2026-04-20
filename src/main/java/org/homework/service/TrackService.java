    // === CHUNK: TRACK_SERVICE [SERVICE] ===
// Описание: Сервис управления треками развития.
// Dependencies: Spring

package org.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.homework.dto.request.CreateTrackRequest;
import org.homework.dto.request.UpdateTrackRequest;
import org.homework.dto.response.TrackDto;
import org.homework.dto.response.TrackSummaryDto;
import org.homework.dto.response.UserDto;
import org.homework.exception.ResourceNotFoundException;
import org.homework.model.Track;
import org.homework.model.User;
import org.homework.repository.TrackRepository;
import org.homework.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

// [START_TRACK_SERVICE]
/*
 * ANCHOR: TRACK_SERVICE
 * PURPOSE: Сервис управления треками развития.
 *
 * @PreConditions:
 * - Spring контекст инициализирован
 * - репозитории доступны
 *
 * @PostConditions:
 * - сервис готов к управлению треками
 *
 * @Invariants:
 * - трек всегда имеет владельца
 * - updatedAt обновляется при каждом изменении
 *
 * @SideEffects:
 * - создание/обновление/удаление треков в БД
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку прав доступа
 * - нельзя разрешить удаление трека не владельцу
 *
 * @AllowedRefactorZone:
 * - можно добавить кэширование
 * - можно добавить дополнительные бизнес-правила
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackService {
    
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    
    /**
     * Получить список треков пользователя (свои + доступные).
     */
    public Page<TrackSummaryDto> getUserTracks(Long userId, Pageable pageable) {
        log.info("TRACK_SERVICE GET_USER_TRACKS ENTRY - userId: {}", userId);
        
        // Получаем треки пользователя
        Page<Track> ownTracks = trackRepository.findByOwnerId(userId, pageable);
        
        // Получаем треки с доступом
        Page<Track> accessibleTracks = trackRepository.findAccessibleTracks(userId, pageable);
        
        // Объединяем результаты (упрощенно - в реальном проекте нужно более сложное объединение)
        List<TrackSummaryDto> allTracks = ownTracks.getContent().stream()
                .map(track -> mapToTrackSummaryDto(track, userId))
                .collect(Collectors.toList());
        
        allTracks.addAll(accessibleTracks.getContent().stream()
                .filter(track -> !track.getOwner().getId().equals(userId))
                .map(track -> mapToTrackSummaryDto(track, userId))
                .collect(Collectors.toList()));
        
        log.info("TRACK_SERVICE GET_USER_TRACKS EXIT - found {} tracks", allTracks.size());
        return new org.springframework.data.domain.PageImpl<>(allTracks, pageable, allTracks.size());
    }
    
    /**
     * Получить трек по ID.
     */
    public TrackDto getTrackById(Long trackId, Long userId) {
        log.info("TRACK_SERVICE GET_TRACK_BY_ID ENTRY - trackId: {}, userId: {}", trackId, userId);
        
        Track track = trackRepository.findByIdWithOwner(trackId)
                .orElseThrow(() -> {
                    log.warn("TRACK_SERVICE GET_TRACK_BY_ID - track not found: {}", trackId);
                    return new ResourceNotFoundException("Track not found with id: " + trackId);
                });
        
        // Проверка прав доступа
        if (!permissionService.hasPermission(trackId, userId, "VIEW")) {
            log.warn("TRACK_SERVICE GET_TRACK_BY_ID - access denied for user: {}", userId);
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        
        TrackDto dto = mapToTrackDto(track, userId);
        log.info("TRACK_SERVICE GET_TRACK_BY_ID EXIT - track found");
        return dto;
    }
    
    /**
     * Создать новый трек.
     */
    @Transactional
    public TrackDto createTrack(CreateTrackRequest request, Long userId) {
        log.info("TRACK_SERVICE CREATE_TRACK ENTRY - userId: {}, title: {}", userId, request.getTitle());
        
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("TRACK_SERVICE CREATE_TRACK - user not found: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });
        
        Track track = Track.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .owner(owner)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        Track savedTrack = trackRepository.save(track);
        
        log.info("TRACK_SERVICE CREATE_TRACK EXIT - track created with id: {}", savedTrack.getId());
        return mapToTrackDto(savedTrack, userId);
    }
    
    /**
     * Обновить трек.
     */
    @Transactional
    public TrackDto updateTrack(Long trackId, UpdateTrackRequest request, Long userId) {
        log.info("TRACK_SERVICE UPDATE_TRACK ENTRY - trackId: {}, userId: {}", trackId, userId);
        
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> {
                    log.warn("TRACK_SERVICE UPDATE_TRACK - track not found: {}", trackId);
                    return new ResourceNotFoundException("Track not found with id: " + trackId);
                });
        
        // Проверка прав доступа (только EDIT или OWNER)
        if (!permissionService.hasPermission(trackId, userId, "EDIT")) {
            log.warn("TRACK_SERVICE UPDATE_TRACK - access denied for user: {}", userId);
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        
        track.setTitle(request.getTitle());
        track.setDescription(request.getDescription());
        track.setUpdatedAt(Instant.now());
        
        Track updatedTrack = trackRepository.save(track);
        
        log.info("TRACK_SERVICE UPDATE_TRACK EXIT - track updated");
        return mapToTrackDto(updatedTrack, userId);
    }
    
    /**
     * Удалить трек.
     */
    @Transactional
    public void deleteTrack(Long trackId, Long userId) {
        log.info("TRACK_SERVICE DELETE_TRACK ENTRY - trackId: {}, userId: {}", trackId, userId);
        
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> {
                    log.warn("TRACK_SERVICE DELETE_TRACK - track not found: {}", trackId);
                    return new ResourceNotFoundException("Track not found with id: " + trackId);
                });
        
        // Проверка прав доступа (только OWNER)
        if (!track.getOwner().getId().equals(userId)) {
            log.warn("TRACK_SERVICE DELETE_TRACK - access denied for user: {}", userId);
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        
        trackRepository.delete(track);
        
        log.info("TRACK_SERVICE DELETE_TRACK EXIT - track deleted");
    }
    
    /**
     * Преобразовать Track в TrackDto.
     */
    private TrackDto mapToTrackDto(Track track, Long userId) {
        UserDto ownerDto = UserDto.builder()
                .id(track.getOwner().getId())
                .username(track.getOwner().getUsername())
                .role(track.getOwner().getRole())
                .enabled(track.getOwner().getEnabled())
                .createdAt(track.getOwner().getCreatedAt())
                .build();
        
        String myRole = permissionService.getUserRole(track.getId(), userId);
        
        return TrackDto.builder()
                .id(track.getId())
                .title(track.getTitle())
                .description(track.getDescription())
                .owner(ownerDto)
                .myRole(myRole)
                .createdAt(track.getCreatedAt())
                .updatedAt(track.getUpdatedAt())
                .build();
    }
    
    /**
     * Преобразовать Track в TrackSummaryDto.
     */
    private TrackSummaryDto mapToTrackSummaryDto(Track track, Long userId) {
        UserDto ownerDto = UserDto.builder()
                .id(track.getOwner().getId())
                .username(track.getOwner().getUsername())
                .role(track.getOwner().getRole())
                .enabled(track.getOwner().getEnabled())
                .createdAt(track.getOwner().getCreatedAt())
                .build();
        
        String myRole = permissionService.getUserRole(track.getId(), userId);
        
        return TrackSummaryDto.builder()
                .id(track.getId())
                .title(track.getTitle())
                .description(track.getDescription())
                .owner(ownerDto)
                .myRole(myRole)
                .updatedAt(track.getUpdatedAt())
                .build();
    }
}
// [END_TRACK_SERVICE]
// === END_CHUNK: TRACK_SERVICE ===
