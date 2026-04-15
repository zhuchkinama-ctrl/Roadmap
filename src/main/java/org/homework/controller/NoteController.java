package org.homework.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

/**
 * Заглушка контроллера управления заметками.
 */
@RestController
@RequestMapping("/api/v1/notes")
public class NoteController {

    @GetMapping("/tree")
    public ResponseEntity<?> getTree() {
        // TODO: реализовать получение дерева заметок
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Object request) {
        // TODO: реализовать создание заметки
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Object request) {
        // TODO: реализовать обновление заметки
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        // TODO: реализовать удаление заметки
        return ResponseEntity.ok().build();
    }
}
