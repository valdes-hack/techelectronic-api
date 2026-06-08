package com.techstore.techstore_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.techstore_api.dto.request.LoginRequest;
import com.techstore.techstore_api.dto.request.RegisterRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.AuthResponse;
import com.techstore.techstore_api.service.AuthService;
import com.techstore.techstore_api.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    /**
     * INSCRIPTION AVEC PHOTO DE PROFIL (Multipart)
     * Retourne dÃ©sormais un AuthResponse (User + Token) pour connexion auto
     */
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestPart("user") String userJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        
        // 1. Convertir le texte JSON en objet RegisterRequest
        RegisterRequest request = objectMapper.readValue(userJson, RegisterRequest.class);

        // 2. Si une photo est fournie, on l'enregistre localement
        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.storeFile(file);
            request.setProfilePictureUrl("http://localhost:8080/uploads/profiles/" + fileName);
        }

        // 3. Appel du service (qui gÃ©nÃ¨re maintenant un Token)
        ApiResponse<AuthResponse> response = authService.registerUser(request);
        
        return ResponseEntity.status(response.getCode()).body(response);
    }

    /**
     * CONNEXION CLASSIQUE
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        ApiResponse<AuthResponse> response = authService.loginUser(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
