package org.homework.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

// === CHUNK: GLOBAL_EXCEPTION_HANDLER [EXCEPTION] ===
// Описание: Глобальный обработчик исключений для REST API.
// Dependencies: Spring Web, SLF4J

// [START_GLOBAL_EXCEPTION_HANDLER]
/*
 * ANCHOR: GLOBAL_EXCEPTION_HANDLER
 * PURPOSE: Глобальный обработчик исключений для REST API.
 *
 * @PreConditions:
 * - Spring контекст инициализирован
 *
 * @PostConditions:
 * - все исключения обрабатываются и возвращаются в едином формате ErrorResponse
 *
 * @Invariants:
 * - ErrorResponse всегда содержит timestamp, status, error, message, path
 *
 * @SideEffects:
 * - запись в лог при обработке исключений
 *
 * @ForbiddenChanges:
 * - нельзя изменить формат ErrorResponse без согласования
 *
 * @AllowedRefactorZone:
 * - можно добавить дополнительные обработчики исключений
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.error("GLOBAL_EXCEPTION_HANDLER ERROR - UserAlreadyExistsException: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.CONFLICT.value());
        error.setError("Conflict");
        error.setMessage(ex.getMessage());
        error.setPath("/api/v1/auth/register");
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.error("GLOBAL_EXCEPTION_HANDLER ERROR - AuthenticationException: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.UNAUTHORIZED.value());
        error.setError("Unauthorized");
        error.setMessage(ex.getMessage());
        error.setPath("/api/v1/auth/login");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("GLOBAL_EXCEPTION_HANDLER ERROR - MethodArgumentNotValidException: validation failed");
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("Validation Error");
        error.setMessage("Validation failed for request fields");
        error.setPath("/api/v1/auth/register");
        error.setValidationErrors(validationErrors);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("GLOBAL_EXCEPTION_HANDLER ERROR - ConstraintViolationException: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("Validation Error");
        error.setMessage(ex.getMessage());
        error.setPath("/api/v1/auth");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("GLOBAL_EXCEPTION_HANDLER ERROR - Exception: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        // Дополнительное логирование для StackOverflowError
        if (ex instanceof jakarta.servlet.ServletException && ex.getCause() instanceof StackOverflowError) {
            log.error("GLOBAL_EXCEPTION_HANDLER ERROR - StackOverflowError detected - likely caused by circular reference in JSON serialization");
            log.error("GLOBAL_EXCEPTION_HANDLER ERROR - This usually happens when JPA entities with bidirectional relationships are serialized without @JsonIgnore");
            log.error("GLOBAL_EXCEPTION_HANDLER ERROR - Check entities: User, Track, Note, TrackPermission for missing @JsonIgnore annotations");
        }

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(Instant.now());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError("Internal Server Error");
        error.setMessage(ex.getMessage());
        error.setPath("/api"); // placeholder
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
// [END_GLOBAL_EXCEPTION_HANDLER]
// === END_CHUNK: GLOBAL_EXCEPTION_HANDLER ===

/**
 * Simple error response DTO matching the specification.
 */
class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private java.util.Map<String, String> validationErrors;

    // getters and setters
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public java.util.Map<String, String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(java.util.Map<String, String> validationErrors) { this.validationErrors = validationErrors; }
}