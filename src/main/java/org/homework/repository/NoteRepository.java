package org.homework.repository;

import org.homework.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
    // Additional query methods can be defined here
}