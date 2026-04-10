package com.example.project.handlers;

import com.example.project.services.VoiceService.VoiceLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VoiceLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleVoiceLimit(VoiceLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.of(429, ex.getMessage(), "VOICE_LIMIT_EXCEEDED"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(401, "Неверный email или пароль", "BAD_CREDENTIALS"));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(401, ex.getMessage(), "USER_NOT_FOUND"));
    }

    // Невалидный тип параметра в URL/query (например невалидный UUID) → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName  = ex.getPropertyName();
        Object paramValue = ex.getValue();
        String msg = "Некорректное значение параметра '%s': %s".formatted(paramName, paramValue);
        log.debug("Type mismatch: {}", msg);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, msg, "INVALID_PARAMETER"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegal(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, ex.getMessage(), "BAD_REQUEST"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e ->
                fields.put(((FieldError) e).getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400, "error", "VALIDATION_ERROR",
                "fields", fields, "timestamp", LocalDateTime.now().toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "Внутренняя ошибка сервера", "INTERNAL_ERROR"));
    }

    public record ErrorResponse(int status, String message, String code, String timestamp) {
        public static ErrorResponse of(int status, String message, String code) {
            return new ErrorResponse(status, message, code, LocalDateTime.now().toString());
        }
    }
}