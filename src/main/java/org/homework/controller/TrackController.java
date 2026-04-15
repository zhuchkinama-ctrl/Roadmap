package org.homework.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

/**
 * Заглушка контроллера управления треками.
 */
@RestController
@RequestMapping("/api/v1/tracks")
public class TrackController {

    @GetMapping
    public ResponseEntity<?> getAll() {
        // TODO: реализовать получение списка треков
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Object request) {
        // TODO: реализовать создание трека
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        // TODO: реализовать получение трека по id
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Object request) {
        // TODO: реализовать обновление трека
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        // TODO: реализовать удаление трека
        return ResponseEntity.ok().build();
    }
}
