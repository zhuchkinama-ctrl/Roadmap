# Пример Java сервиса с GRACE разметкой

Пример сервиса для TrackHub с полной семантической разметкой, контрактом и AI-friendly логированием.

---

## Пример: TrackService

```java
package org.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.homework.dto.request.CreateTrackRequest;
import org.homework.dto.request.UpdateTrackRequest;
import org.homework.dto.response.TrackDto;
import org.homework.exception.ResourceNotFoundException;
import org.homework.model.Track;
import org.homework.model.User;
import org.homework.repository.TrackRepository;
import org.homework.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// [START_TRACK_SERVICE]
/*
 * ANCHOR: TRACK_SERVICE
 * PURPOSE: Сервис для управления треками развития пользователей.
 *
 * @PreConditions:
 * - Spring контекст инициализирован
 * - все зависимости внедрены
 * - база данных доступна
 *
 * @PostConditions:
 * - методы сервиса возвращают корректные DTO
 * - все изменения сохраняются в БД
 * - логи записаны для всех критичных операций
 *
 * @Invariants:
 * - сервис не меняет глобальное состояние приложения
 * - все операции транзакционны
 * - пользователь всегда существует при операциях с треками
 *
 * @SideEffects:
 * - создание/обновление/удаление треков в БД
 * - запись в журнал через logger
 *
 * @ForbiddenChanges:
 * - нельзя убрать @Transactional на методах изменения данных
 * - нельзя убрать проверку прав доступа
 * - нельзя убрать валидацию входных данных
 *
 * @AllowedRefactorZone:
 * - можно изменить способ получения треков (например, добавить кэширование)
 * - можно изменить формат логирования
 * - можно добавить дополнительные методы
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackService {
    
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    
    // [START_CREATE_TRACK]
    /*
     * ANCHOR: CREATE_TRACK
     * PURPOSE: Создание нового трека для пользователя.
     *
     * @PreConditions:
     * - userId: существующий пользователь
     * - request.title: непустая строка, max 200 символов
     * - request.description: опционально, max 1000 символов
     *
     * @PostConditions:
     * - при успехе: возвращается TrackDto с id нового трека
     * - при ошибке: выбрасывается ResourceNotFoundException или IllegalArgumentException
     *
     * @Invariants:
     * - трек всегда принадлежит указанному пользователю
     * - трек всегда имеет уникальный id
     *
     * @SideEffects:
     * - создаёт запись трека в БД
     * - логирует создание трека
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования пользователя
     * - нельзя убрать валидацию заголовка
     * - нельзя убрать @Transactional
     */
    @Transactional
    public TrackDto createTrack(Long userId, CreateTrackRequest request) {
        log.info("CREATE_TRACK ENTRY - userId: {}, title: {}", userId, request.getTitle());
        
        // Валидация заголовка
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.warn("CREATE_TRACK DECISION - reject_empty_title - userId: {}", userId);
            log.info("CREATE_TRACK EXIT - rejected - reason: empty_title");
            throw new IllegalArgumentException("Track title cannot be empty");
        }
        
        if (request.getTitle().length() > 200) {
            log.warn("CREATE_TRACK DECISION - reject_title_too_long - userId: {}, length: {}", 
                    userId, request.getTitle().length());
            log.info("CREATE_TRACK EXIT - rejected - reason: title_too_long");
            throw new IllegalArgumentException("Track title cannot exceed 200 characters");
        }
        
        // Проверка существования пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("CREATE_TRACK DECISION - reject_user_not_found - userId: {}", userId);
                    log.info("CREATE_TRACK EXIT - rejected - reason: user_not_found");
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });
        
        log.debug("CREATE_TRACK CHECK - user_exists - userId: {}, result: true", userId);
        
        // Создание трека
        Track track = new Track();
        track.setTitle(request.getTitle().trim());
        track.setDescription(request.getDescription() != null ? 
                request.getDescription().trim() : null);
        track.setUser(user);
        track.setOrderIndex(getNextOrderIndex(userId));
        
        Track savedTrack = trackRepository.save(track);
        
        log.info("CREATE_TRACK STATE_CHANGE - entity: track, id: {}, action: created, userId: {}", 
                savedTrack.getId(), userId);
        
        log.info("CREATE_TRACK EXIT - success - trackId: {}, userId: {}", savedTrack.getId(), userId);
        
        return mapToTrackDto(savedTrack);
    }
    // [END_CREATE_TRACK]
    
    // [START_UPDATE_TRACK]
    /*
     * ANCHOR: UPDATE_TRACK
     * PURPOSE: Обновление существующего трека.
     *
     * @PreConditions:
     * - trackId: существующий трек
     * - userId: пользователь с правами OWNER или EDIT
     * - request.title: непустая строка, max 200 символов
     * - request.description: опционально, max 1000 символов
     *
     * @PostConditions:
     * - при успехе: возвращается обновлённый TrackDto
     * - при ошибке: выбрасывается ResourceNotFoundException или SecurityException
     *
     * @Invariants:
     * - трек всегда принадлежит тому же пользователю
     * - id трека не изменяется
     *
     * @SideEffects:
     * - обновляет запись трека в БД
     * - логирует обновление трека
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку прав доступа
     * - нельзя убрать валидацию заголовка
     * - нельзя убрать @Transactional
     */
    @Transactional
    public TrackDto updateTrack(Long trackId, UpdateTrackRequest request, Long userId) {
        log.info("UPDATE_TRACK ENTRY - trackId: {}, userId: {}, title: {}", 
                trackId, userId, request.getTitle());
        
        // Проверка прав доступа
        permissionService.checkPermission(trackId, userId, Permission.EDIT);
        log.debug("UPDATE_TRACK CHECK - permission_granted - trackId: {}, userId: {}", 
                trackId, userId);
        
        // Получение трека
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> {
                    log.warn("UPDATE_TRACK DECISION - reject_track_not_found - trackId: {}", trackId);
                    log.info("UPDATE_TRACK EXIT - rejected - reason: track_not_found");
                    return new ResourceNotFoundException("Track not found with id: " + trackId);
                });
        
        // Валидация заголовка
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.warn("UPDATE_TRACK DECISION - reject_empty_title - trackId: {}", trackId);
            log.info("UPDATE_TRACK EXIT - rejected - reason: empty_title");
            throw new IllegalArgumentException("Track title cannot be empty");
        }
        
        if (request.getTitle().length() > 200) {
            log.warn("UPDATE_TRACK DECISION - reject_title_too_long - trackId: {}, length: {}", 
                    trackId, request.getTitle().length());
            log.info("UPDATE_TRACK EXIT - rejected - reason: title_too_long");
            throw new IllegalArgumentException("Track title cannot exceed 200 characters");
        }
        
        // Обновление трека
        track.setTitle(request.getTitle().trim());
        track.setDescription(request.getDescription() != null ? 
                request.getDescription().trim() : null);
        
        Track updatedTrack = trackRepository.save(track);
        
        log.info("UPDATE_TRACK STATE_CHANGE - entity: track, id: {}, action: updated, userId: {}", 
                updatedTrack.getId(), userId);
        
        log.info("UPDATE_TRACK EXIT - success - trackId: {}, userId: {}", updatedTrack.getId(), userId);
        
        return mapToTrackDto(updatedTrack);
    }
    // [END_UPDATE_TRACK]
    
    // [START_DELETE_TRACK]
    /*
     * ANCHOR: DELETE_TRACK
     * PURPOSE: Удаление трека.
     *
     * @PreConditions:
     * - trackId: существующий трек
     * - userId: пользователь с правами OWNER
     *
     * @PostConditions:
     * - при успехе: трек удалён из БД
     * - при ошибке: выбрасывается ResourceNotFoundException или SecurityException
     *
     * @Invariants:
     * - все заметки трека также удаляются (CASCADE)
     * - все права доступа к треку удаляются
     *
     * @SideEffects:
     * - удаляет трек из БД
     * - удаляет связанные заметки (CASCADE)
     * - удаляет права доступа (CASCADE)
     * - логирует удаление трека
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку прав доступа
     * - нельзя убрать @Transactional
     */
    @Transactional
    public void deleteTrack(Long trackId, Long userId) {
        log.info("DELETE_TRACK ENTRY - trackId: {}, userId: {}", trackId, userId);
        
        // Проверка прав доступа
        permissionService.checkPermission(trackId, userId, Permission.OWNER);
        log.debug("DELETE_TRACK CHECK - permission_granted - trackId: {}, userId: {}", 
                trackId, userId);
        
        // Проверка существования трека
        if (!trackRepository.existsById(trackId)) {
            log.warn("DELETE_TRACK DECISION - reject_track_not_found - trackId: {}", trackId);
            log.info("DELETE_TRACK EXIT - rejected - reason: track_not_found");
            throw new ResourceNotFoundException("Track not found with id: " + trackId);
        }
        
        // Удаление трека
        trackRepository.deleteById(trackId);
        
        log.info("DELETE_TRACK STATE_CHANGE - entity: track, id: {}, action: deleted, userId: {}", 
                trackId, userId);
        
        log.info("DELETE_TRACK EXIT - success - trackId: {}, userId: {}", trackId, userId);
    }
    // [END_DELETE_TRACK]
    
    // [START_GET_TRACK_BY_ID]
    /*
     * ANCHOR: GET_TRACK_BY_ID
     * PURPOSE: Получение трека по ID.
     *
     * @PreConditions:
     * - trackId: существующий трек
     * - userId: пользователь с правами VIEW, EDIT или OWNER
     *
     * @PostConditions:
     * - при успехе: возвращается TrackDto
     * - при ошибке: выбрасывается ResourceNotFoundException или SecurityException
     *
     * @Invariants:
     * - трек всегда принадлежит тому же пользователю
     *
     * @SideEffects:
     * - логирует получение трека
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку прав доступа
     */
    public TrackDto getTrackById(Long trackId, Long userId) {
        log.info("GET_TRACK_BY_ID ENTRY - trackId: {}, userId: {}", trackId, userId);
        
        // Проверка прав доступа
        permissionService.checkPermission(trackId, userId, Permission.VIEW);
        log.debug("GET_TRACK_BY_ID CHECK - permission_granted - trackId: {}, userId: {}", 
                trackId, userId);
        
        // Получение трека
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> {
                    log.warn("GET_TRACK_BY_ID DECISION - reject_track_not_found - trackId: {}", trackId);
                    log.info("GET_TRACK_BY_ID EXIT - rejected - reason: track_not_found");
                    return new ResourceNotFoundException("Track not found with id: " + trackId);
                });
        
        log.info("GET_TRACK_BY_ID EXIT - success - trackId: {}, userId: {}", trackId, userId);
        
        return mapToTrackDto(track);
    }
    // [END_GET_TRACK_BY_ID]
    
    // [START_GET_USER_TRACKS]
    /*
     * ANCHOR: GET_USER_TRACKS
     * PURPOSE: Получение всех треков пользователя.
     *
     * @PreConditions:
     * - userId: существующий пользователь
     *
     * @PostConditions:
     * - при успехе: возвращается список TrackDto
     * - при ошибке: выбрасывается ResourceNotFoundException
     *
     * @Invariants:
     * - список всегда отсортирован по orderIndex
     *
     * @SideEffects:
     * - логирует получение треков
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования пользователя
     */
    public List<TrackDto> getUserTracks(Long userId) {
        log.info("GET_USER_TRACKS ENTRY - userId: {}", userId);
        
        // Проверка существования пользователя
        if (!userRepository.existsById(userId)) {
            log.warn("GET_USER_TRACKS DECISION - reject_user_not_found - userId: {}", userId);
            log.info("GET_USER_TRACKS EXIT - rejected - reason: user_not_found");
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        // Получение треков
        List<Track> tracks = trackRepository.findByUserIdOrderByOrderIndexAsc(userId);
        
        log.debug("GET_USER_TRACKS CHECK - tracks_found - userId: {}, count: {}", 
                userId, tracks.size());
        
        log.info("GET_USER_TRACKS EXIT - success - userId: {}, count: {}", userId, tracks.size());
        
        return tracks.stream()
                .map(this::mapToTrackDto)
                .collect(Collectors.toList());
    }
    // [END_GET_USER_TRACKS]
    
    // [START_GET_NEXT_ORDER_INDEX]
    /*
     * ANCHOR: GET_NEXT_ORDER_INDEX
     * PURPOSE: Получение следующего orderIndex для трека пользователя.
     *
     * @PreConditions:
     * - userId: существующий пользователь
     *
     * @PostConditions:
     * - возвращается целое число >= 0
     *
     * @Invariants:
     * - orderIndex всегда уникален для треков пользователя
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать проверку существования пользователя
     */
    private int getNextOrderIndex(Long userId) {
        log.debug("GET_NEXT_ORDER_INDEX ENTRY - userId: {}", userId);
        
        Integer maxOrderIndex = trackRepository.findMaxOrderIndexByUserId(userId);
        int nextOrderIndex = (maxOrderIndex != null) ? maxOrderIndex + 1 : 0;
        
        log.debug("GET_NEXT_ORDER_INDEX EXIT - userId: {}, nextOrderIndex: {}", 
                userId, nextOrderIndex);
        
        return nextOrderIndex;
    }
    // [END_GET_NEXT_ORDER_INDEX]
    
    // [START_MAP_TO_TRACK_DTO]
    /*
     * ANCHOR: MAP_TO_TRACK_DTO
     * PURPOSE: Маппинг сущности Track в TrackDto.
     *
     * @PreConditions:
     * - track: не null
     *
     * @PostConditions:
     * - возвращается TrackDto с заполненными полями
     *
     * @Invariants:
     * - все поля корректно скопированы
     *
     * @SideEffects:
     * - нет побочных эффектов
     *
     * @ForbiddenChanges:
     * - нельзя убрать маппинг обязательных полей
     */
    private TrackDto mapToTrackDto(Track track) {
        log.debug("MAP_TO_TRACK_DTO ENTRY - trackId: {}", track.getId());
        
        TrackDto dto = new TrackDto();
        dto.setId(track.getId());
        dto.setUserId(track.getUser().getId());
        dto.setTitle(track.getTitle());
        dto.setDescription(track.getDescription());
        dto.setOrderIndex(track.getOrderIndex());
        dto.setCreatedAt(track.getCreatedAt());
        dto.setUpdatedAt(track.getUpdatedAt());
        
        log.debug("MAP_TO_TRACK_DTO EXIT - trackId: {}", track.getId());
        
        return dto;
    }
    // [END_MAP_TO_TRACK_DTO]
}
// [END_TRACK_SERVICE]
```

---

## Ключевые моменты

### 1. Контракт

Каждый метод имеет контракт с полями:
- `ANCHOR` — уникальный идентификатор
- `PURPOSE` — назначение метода
- `@PreConditions` — предусловия
- `@PostConditions` — постусловия
- `@Invariants` — инварианты
- `@SideEffects` — побочные эффекты
- `@ForbiddenChanges` — запрещённые изменения
- `@AllowedRefactorZone` — разрешённая зона рефакторинга

### 2. Логирование

Каждый метод имеет логи в точках:
- `ENTRY` — вход в метод
- `EXIT` — выход из метода
- `CHECK` — проверка условия
- `DECISION` — принятие решения
- `STATE_CHANGE` — изменение состояния
- `ERROR` — ошибка

### 3. Валидация

Все входные данные валидируются:
- Проверка на null/empty
- Проверка длины строк
- Проверка существования сущностей
- Проверка прав доступа

### 4. Транзакционность

Все методы, изменяющие данные, помечены `@Transactional`.

---

*Создано на основе методологии GRACE для TrackHub*
