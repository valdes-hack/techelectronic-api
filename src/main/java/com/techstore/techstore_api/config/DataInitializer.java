package com.techstore.techstore_api.config;

import com.techstore.techstore_api.model.Role;
import com.techstore.techstore_api.model.User;
import com.techstore.techstore_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// @Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Vérifie si l'admin existe déjà dans la base Aiven
        if (userRepository.findByEmail("admin@techstore.com").isEmpty()) {
            
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("Valdes")
                    .email("admin@techstore.com")
                    .phone("+237670000000")
                    // Spring Boot va s'occuper du hachage parfait ici
                    .password(passwordEncoder.encode("Admin1234")) 
                    .role(Role.ADMIN)
                    .isGuest(false)
                    .isVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            System.out.println("🚀 [INIT] Compte Administrateur créé avec succès sur Aiven !");
        } else {
            System.out.println("ℹ️ [INIT] L'administrateur existe déjà.");
        }
    }
}