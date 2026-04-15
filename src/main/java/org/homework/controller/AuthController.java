package org.homework.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.homework.dto.request.AuthRequest;
import org.homework.dto.request.RegisterRequest;
import org.homework.dto.response.AuthResponse;
import org.homework.dto.response.UserDto;
import org.homework.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received - username: {}, email: {}", request.getUsername(), request.getEmail());
        log.debug("Request validation passed for username: {}", request.getUsername());
        
        try {
            UserDto userDto = authService.register(request);
            log.info("Registration successful for username: {}, userId: {}", request.getUsername(), userDto.getId());
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            log.error("Registration failed for username: {}, error: {}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("Login request for username: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
