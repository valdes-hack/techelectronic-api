package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.model.AppSetting;
import com.techstore.techstore_api.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class AppSettingController {

    private final AppSettingService appSettingService;

    // Accessible publiquement pour le Front-End (Navbar, Footer)
    @GetMapping
    public ResponseEntity<ApiResponse<AppSetting>> getSettings() {
        return ResponseEntity.ok(ApiResponse.<AppSetting>builder()
                .status("success")
                .code(200)
                .message("Paramètres de l'application récupérés")
                .data(appSettingService.getSettings())
                .timestamp(LocalDateTime.now())
                .build());
    }

    // Accessible uniquement par l'admin
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppSetting>> updateSettings(@RequestBody AppSetting settings) {
        return ResponseEntity.ok(ApiResponse.<AppSetting>builder()
                .status("success")
                .code(200)
                .message("Paramètres mis à jour avec succès")
                .data(appSettingService.updateSettings(settings))
                .timestamp(LocalDateTime.now())
                .build());
    }
}
