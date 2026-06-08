package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.UserResponse;
import com.techstore.techstore_api.service.FileStorageService;
import com.techstore.techstore_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    /**
     * 1. RÃ‰CUPÃ‰RER MON PROFIL (Pour afficher le nom et la photo sur React)
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Principal principal) {
        UserResponse response = userService.getMyInfo(principal.getName());
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status("success")
                .code(200)
                .message("Profil rÃ©cupÃ©rÃ©")
                .timestamp(LocalDateTime.now())
                .data(response)
                .build());
    }

    /**
     * 2. METTRE Ã€ JOUR MA PHOTO DE PROFIL
     * POST /api/v1/users/profile-picture
     */
    /**
     * METTRE Ã€ JOUR MA PHOTO DE PROFIL
     * On ajoute (consumes = "multipart/form-data") pour que Swagger affiche le bouton d'upload
     */
    @PostMapping(value = "/profile-picture", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UserResponse>> uploadProfilePicture(
            @RequestPart("file") MultipartFile file, // On utilise @RequestPart pour les fichiers
            Principal principal) {
        
        // 1. Enregistrer le fichier
        String fileName = fileStorageService.storeFile(file);
        
        // 2. CrÃ©er l'URL
        String url = "http://localhost:8080/uploads/profiles/" + fileName;
        
        // 3. Mettre Ã  jour en BD
        UserResponse response = userService.updateProfilePicture(principal.getName(), url);
        
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status("success")
                .code(200)
                .message("Photo de profil mise Ã  jour")
                .timestamp(LocalDateTime.now())
                .data(response)
                .build());
    }
}
