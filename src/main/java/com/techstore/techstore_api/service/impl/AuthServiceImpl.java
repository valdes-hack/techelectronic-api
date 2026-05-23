package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.config.JwtUtils;
import com.techstore.techstore_api.dto.request.LoginRequest;
import com.techstore.techstore_api.dto.request.RegisterRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.AuthResponse;
import com.techstore.techstore_api.dto.response.UserResponse;
import com.techstore.techstore_api.model.Role;
import com.techstore.techstore_api.model.User;
import com.techstore.techstore_api.repository.UserRepository;
import com.techstore.techstore_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * INSCRIPTION D'UN NOUVEL UTILISATEUR
     * Génère un Token immédiatement après l'inscription pour le Frontend
     */
@Override
@Transactional
public ApiResponse<AuthResponse> registerUser(RegisterRequest request) {
    // 1. Chercher si l'utilisateur existe déjà ✨
    User user = userRepository.findByEmail(request.getEmail()).orElse(null);

    if (user == null) {
        // 2. S'il n'existe pas, on le crée (Logique Invité)
        user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName() != null ? request.getLastName() : "Client")
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT)
                .isGuest(true)
                .isVerified(true)
                .build();
        user = userRepository.save(user);
    }

    // 3. ✨ GÉNÉRATION DU TOKEN (Utilise la nouvelle méthode sécurisée) ✨
    String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());

    // 4. Réponse
    AuthResponse authResponse = new AuthResponse(jwt, mapToUserResponse(user));

    return ApiResponse.<AuthResponse>builder()
            .status("success")
            .code(200)
            .message(user.isGuest() ? "Utilisateur reconnu" : "Compte créé")
            .timestamp(LocalDateTime.now())
            .data(authResponse)
            .build();
}

    /**
     * CONNEXION (LOGIN)
     */
    @Override
    public ApiResponse<AuthResponse> loginUser(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            AuthResponse authResponse = new AuthResponse(jwt, mapToUserResponse(user));

            return ApiResponse.<AuthResponse>builder()
                    .status("success")
                    .code(200)
                    .message("Connexion réussie")
                    .timestamp(LocalDateTime.now())
                    .data(authResponse)
                    .build();

        } catch (BadCredentialsException e) {
            return ApiResponse.<AuthResponse>builder()
                    .status("error")
                    .code(401)
                    .message("Email ou mot de passe incorrect.")
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.<AuthResponse>builder()
                    .status("error")
                    .code(500)
                    .message("Erreur interne lors de la connexion.")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * MAPPING DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .profilePictureUrl(user.getProfilePictureUrl())
                .isVerified(user.isVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}