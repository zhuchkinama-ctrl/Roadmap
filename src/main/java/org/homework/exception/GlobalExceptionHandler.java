package org.homework.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.Instant;

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
}