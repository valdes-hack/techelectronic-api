package com.techstore.techstore_api.exception;

import com.techstore.techstore_api.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConflict(DataIntegrityViolationException e) {
        log.error("❌ [EXCEPTION DB] DataIntegrityViolationException: ", e);
        String realError = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();
        
        return ResponseEntity.status(409).body(
            ApiResponse.<String>builder()
                .status("error")
                .code(409)
                .message("Erreur de base de données : " + realError)
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntime(RuntimeException e) {
        log.error("❌ [EXCEPTION RUNTIME]: ", e);
        return ResponseEntity.status(500).body(
            ApiResponse.<String>builder()
                .status("error")
                .code(500)
                .message("Erreur interne du serveur : " + e.getMessage())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneral(Exception e) {
        log.error("❌ [EXCEPTION GENERAL]: ", e);
        return ResponseEntity.status(500).body(
            ApiResponse.<String>builder()
                .status("error")
                .code(500)
                .message("Erreur serveur : " + e.getMessage())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}