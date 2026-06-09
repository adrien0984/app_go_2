package com.appgo.shared.error;

import com.appgo.games.exception.GameNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestionnaire global des exceptions pour l'API.
 * Transforme les exceptions en réponses d'erreur cohérentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        
        String requestId = UUID.randomUUID().toString();
        log.warn("Validation error: {}", requestId, ex);

        var errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.ErrorDetail(
                        error.getField(),
                        ErrorCode.BAD_REQUEST,
                        error.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse response = new ErrorResponse(
                ErrorCode.BAD_REQUEST,
                "Validation failed",
                LocalDateTime.now(),
                errors,
                requestId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        String requestId = UUID.randomUUID().toString();
        log.warn("Authentication error: {}", requestId, ex);

        ErrorResponse response = new ErrorResponse(
                ErrorCode.UNAUTHORIZED,
                ex.getMessage() == null ? "Authentication failed" : ex.getMessage(),
                LocalDateTime.now(),
                null,
                requestId);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(GameNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleGameNotFoundException(GameNotFoundException ex) {
        String requestId = UUID.randomUUID().toString();
        log.warn("Resource not found: {}", requestId, ex);

        ErrorResponse response = new ErrorResponse(
                ErrorCode.NOT_FOUND,
                ex.getMessage(),
                LocalDateTime.now(),
                null,
                requestId);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        String requestId = UUID.randomUUID().toString();
        log.error("Unexpected error: {}", requestId, ex);

        ErrorResponse response = new ErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                LocalDateTime.now(),
                null,
                requestId);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
