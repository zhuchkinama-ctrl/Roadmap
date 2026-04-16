package org.homework.repository;

import org.homework.model.TrackPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackPermissionRepository extends JpaRepository<TrackPermission, Long> {
    
    /**
     * Найти разрешение по треку и пользователю.
     */
    Optional<TrackPermission> findByTrackIdAndUserId(Long trackId, Long userId);
}