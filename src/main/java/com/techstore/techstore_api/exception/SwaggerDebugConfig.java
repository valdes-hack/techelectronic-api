package com.techstore.techstore_api.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class SwaggerDebugConfig {
    @ExceptionHandler(Exception.class)
    public void handleAll(Exception e, HttpServletRequest request) {
        if (request.getRequestURI().contains("api-docs")) {
            System.err.println("!!! ERREUR CRITIQUE SWAGGER !!!");
            System.err.println("Cause : " + e.getMessage());
            e.printStackTrace(); // Ceci va afficher TOUTE l'erreur dans ton terminal
        }
    }
}