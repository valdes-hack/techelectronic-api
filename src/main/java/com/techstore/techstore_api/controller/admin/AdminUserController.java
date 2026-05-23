package com.techstore.techstore_api.controller.admin;

import com.techstore.techstore_api.dto.request.AdminPasswordResetRequest;
import com.techstore.techstore_api.dto.request.UserUpdateRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.UserResponse;
import com.techstore.techstore_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder()
                .status("success").data(userService.getAllUsers()).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status("success").message("Utilisateur mis à jour").data(userService.updateUser(id, request)).build());
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").message("Statut du compte modifié").build());
    }

    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id, @RequestBody AdminPasswordResetRequest request) {
        userService.resetUserPassword(id, request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").message("Mot de passe réinitialisé avec succès").build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").message("Utilisateur désactivé (Soft Delete)").build());
    }
}