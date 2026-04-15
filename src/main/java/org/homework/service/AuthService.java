package org.homework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.homework.dto.request.AuthRequest;
import org.homework.dto.request.RegisterRequest;
import org.homework.dto.response.AuthResponse;
import org.homework.dto.response.UserDto;
import org.homework.exception.UserAlreadyExistsException;
import org.homework.model.User;
import org.homework.repository.UserRepository;
import org.homework.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserDto register(RegisterRequest request) {
        log.info("Attempting to register user with username: {}, email: {}", request.getUsername(), request.getEmail());
        log.debug("Registration request details: username={}, email={}, password length={}",
            request.getUsername(), request.getEmail(), request.getPassword() != null ? request.getPassword().length() : 0);

        // Проверка существования пользователя по username
        boolean usernameExists = userRepository.existsByUsername(request.getUsername());
        log.debug("Username {} exists: {}", request.getUsername(), usernameExists);
        
        if (usernameExists) {
            log.warn("Registration failed: username {} already exists", request.getUsername());
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        // Проверка существования пользователя по email
        boolean emailExists = userRepository.existsByEmail(request.getEmail());
        log.debug("Email {} exists: {}", request.getEmail(), emailExists);
        
        if (emailExists) {
            log.warn("Registration failed: email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        // Создание нового пользователя
        log.info("Creating new user with username: {}", request.getUsername());
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .enabled(true)
                .build();

        try {
            user = userRepository.save(user);
            log.info("User registered successfully with id: {}, username: {}", user.getId(), user.getUsername());
        } catch (Exception e) {
            log.error("Failed to save user to database: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось сохранить пользователя в базе данных", e);
        }

        return mapToUserDto(user);
    }

    public AuthResponse login(AuthRequest request) {
        log.info("Attempting to login user: {}", request.getUsername());

        // Аутентификация пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Получение пользователя из базы данных
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Генерация JWT токенов
        String token = jwtTokenProvider.generateToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateToken(user.getUsername());

        log.info("User logged in successfully: {}", request.getUsername());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    public UserDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return mapToUserDto(user);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}