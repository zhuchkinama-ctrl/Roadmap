package org.homework.repository;

import org.homework.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// === CHUNK: USER_REPOSITORY [PERSISTENCE] ===
// Описание: JPA-репозиторий для работы с сущностью User.
// Dependencies: Spring Data JPA

// [START_USER_REPOSITORY]
/*
 * ANCHOR: USER_REPOSITORY
 * PURPOSE: JPA-репозиторий для работы с сущностью User.
 *
 * @PreConditions:
 * - нет нетривиальных предусловий
 *
 * @PostConditions:
 * - репозиторий предоставляет CRUD операции и кастомные запросы
 *
 * @Invariants:
 * - findByUsername возвращает Optional<User>
 * - existsByUsername возвращает boolean
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя удалить методы findByUsername, existsByUsername, existsByEmail
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные методы запросов
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
// [END_USER_REPOSITORY]
// === END_CHUNK: USER_REPOSITORY ===