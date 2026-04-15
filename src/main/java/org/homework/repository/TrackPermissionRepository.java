package org.homework.repository;

import org.homework.model.TrackPermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackPermissionRepository extends JpaRepository<TrackPermission, Long> {
    // Additional query methods can be defined here
}