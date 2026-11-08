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
import org.springframework.http.MediaType;
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
     * INSCRIPTION CLASSIQUE (JSON) - utilisée par la page /register du frontend.
     * Retourne un AuthResponse (User + Token) pour connexion automatique.
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AuthResponse>> registerJson(@Valid @RequestBody RegisterRequest request) {
        ApiResponse<AuthResponse> response = authService.registerUser(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    /**
     * INSCRIPTION AVEC PHOTO DE PROFIL (Multipart : partie "user" en JSON + partie "file").
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AuthResponse>> registerMultipart(
            @RequestPart("user") String userJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {

        // 1. Convertir le texte JSON en objet RegisterRequest
        RegisterRequest request = objectMapper.readValue(userJson, RegisterRequest.class);

        // 2. Si une photo est fournie, on la stocke (Cloudinary ou disque) et on garde l'URL publique
        if (file != null && !file.isEmpty()) {
            String stored = fileStorageService.storeFile(file);
            request.setProfilePictureUrl(fileStorageService.resolvePublicUrl(stored));
        }

        // 3. Appel du service (qui génère un Token)
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
