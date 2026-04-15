package org.homework.repository;

import org.homework.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, Long> {
    // Additional query methods can be defined here
}