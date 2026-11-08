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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * INSCRIPTION D'UN NOUVEL UTILISATEUR
     * Génère un token immédiatement après l'inscription pour le frontend.
     *
     * SÉCURITÉ : si un compte "réel" (avec mot de passe) existe déjà pour cet email, on refuse (409).
     * Seul un profil invité créé par un achat sans compte (sans mot de passe) peut être activé.
     */
    @Override
    @Transactional
    public ApiResponse<AuthResponse> registerUser(RegisterRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        if (email == null || email.isEmpty() || request.getPassword() == null || request.getPassword().isBlank()) {
            return error(400, "L'email et le mot de passe sont obligatoires.");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            boolean activatableGuest = user.isGuest()
                    && (user.getPassword() == null || user.getPassword().isBlank());
            if (!activatableGuest) {
                return error(409, "Un compte existe déjà avec cet email. Connectez-vous.");
            }
            // Activation d'un profil invité (issu d'une commande sans compte)
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName() != null ? request.getLastName() : "Client");
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setGuest(false);
            user.setVerified(true);
            if (request.getProfilePictureUrl() != null) {
                user.setProfilePictureUrl(request.getProfilePictureUrl());
            }
        } else {
            user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName() != null ? request.getLastName() : "Client")
                    .email(email)
                    .phone(request.getPhone())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .profilePictureUrl(request.getProfilePictureUrl())
                    .role(Role.CLIENT)
                    .isGuest(false)
                    .isVerified(true)
                    .build();
        }
        user = userRepository.save(user);

        String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());
        AuthResponse authResponse = new AuthResponse(jwt, mapToUserResponse(user));

        return ApiResponse.<AuthResponse>builder()
                .status("success")
                .code(200)
                .message("Compte créé avec succès")
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
            return error(401, "Email ou mot de passe incorrect.");
        } catch (Exception e) {
            e.printStackTrace();
            return error(500, "Erreur interne lors de la connexion.");
        }
    }

    private ApiResponse<AuthResponse> error(int code, String message) {
        return ApiResponse.<AuthResponse>builder()
                .status("error")
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
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
