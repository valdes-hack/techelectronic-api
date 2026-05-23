package com.techstore.techstore_api.exception;

import com.techstore.techstore_api.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConflict(DataIntegrityViolationException e) {
        // On récupère le message réel (ex: Duplicate entry 'email@test.com')
        String realError = e.getMostSpecificCause().getMessage();
        
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
        return ResponseEntity.status(400).body(
            ApiResponse.<String>builder()
                .status("error")
                .code(400)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build()
        );
    }
}