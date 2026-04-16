package org.homework.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

// === CHUNK: CUSTOM_USER_DETAILS_SERVICE [SECURITY] ===
// Описание: Сервис загрузки пользовательских данных для Spring Security.
// Dependencies: Spring Security, UserRepository

// [START_CUSTOM_USER_DETAILS_SERVICE]
/*
 * ANCHOR: CUSTOM_USER_DETAILS_SERVICE
 * PURPOSE: Сервис загрузки пользовательских данных для Spring Security.
 *
 * @PreConditions:
 * - UserRepository доступен через DI
 *
 * @PostConditions:
 * - UserDetails загружается по username
 *
 * @Invariants:
 * - пароль никогда не логируется
 * - роль пользователя всегда префиксируется "ROLE_"
 *
 * @SideEffects:
 * - нет побочных эффектов
 *
 * @ForbiddenChanges:
 * - нельзя убрать проверку существования пользователя
 * - нельзя изменить формат роли без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные authorities
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final org.homework.repository.UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("CUSTOM_USER_DETAILS_SERVICE ENTRY - loadUserByUsername - username: {}", username);

        org.homework.model.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("CUSTOM_USER_DETAILS_SERVICE ERROR - User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.debug("CUSTOM_USER_DETAILS_SERVICE CHECK - user found - username: {}, role: {}", user.getUsername(), user.getRole());

        // Create UserDetails with only the necessary information
        // This avoids loading JPA entities and prevents StackOverflowError
        UserDetails userDetails = User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
                .accountLocked(!user.getEnabled())
                .build();
        
        log.debug("CUSTOM_USER_DETAILS_SERVICE EXIT - loadUserByUsername - success - username: {}", username);
        return userDetails;
    }
}
// [END_CUSTOM_USER_DETAILS_SERVICE]
// === END_CHUNK: CUSTOM_USER_DETAILS_SERVICE ===
