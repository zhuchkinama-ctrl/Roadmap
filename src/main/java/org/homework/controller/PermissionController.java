package org.homework.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

/**
 * Заглушка контроллера управления правами доступа к трекам.
 */
@RestController
@RequestMapping("/api/v1/tracks/{trackId}/permissions")
public class PermissionController {

    @GetMapping
    public ResponseEntity<?> getPermissions(@PathVariable Long trackId) {
        // TODO: вернуть список прав доступа
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> addPermission(@PathVariable Long trackId, @RequestBody Object request) {
        // TODO: добавить право доступа
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updatePermission(@PathVariable Long trackId, @PathVariable Long userId, @RequestBody Object request) {
        // TODO: обновить тип права
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deletePermission(@PathVariable Long trackId, @PathVariable Long userId) {
        // TODO: удалить право доступа
        return ResponseEntity.ok().build();
    }
}