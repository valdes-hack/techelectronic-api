package com.techstore.techstore_api.service;

import com.techstore.techstore_api.dto.request.AdminPasswordResetRequest;
import com.techstore.techstore_api.dto.request.UserUpdateRequest;
import com.techstore.techstore_api.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    /**
     * --- FONCTIONNALITÉS ADMINISTRATEUR ---
     */
    
    // Lister tous les utilisateurs de la plateforme
    List<UserResponse> getAllUsers();

    // Modifier les informations d'un utilisateur (nom, rôle, etc.)
    UserResponse updateUser(Long id, UserUpdateRequest request);

    // Activer ou désactiver le compte d'un utilisateur
    void toggleUserStatus(Long id);

    // Réinitialiser le mot de passe d'un utilisateur
    void resetUserPassword(Long id, AdminPasswordResetRequest request);

    // Supprimer un utilisateur (Soft Delete via is_deleted)
    void deleteUser(Long id);


    /**
     * --- FONCTIONNALITÉS CLIENT (PROFIL) ---
     */

    // Récupérer les informations de l'utilisateur connecté (via son email/token)
    UserResponse getMyInfo(String email);

    // Mettre à jour la photo de profil de l'utilisateur connecté
    UserResponse updateProfilePicture(String email, String url);
}