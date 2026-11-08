package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.Role;
import com.techstore.techstore_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Pour vérifier si un email existe déjà lors de l'inscription
    Optional<User> findByEmail(String email);
    
    // Pour vérifier si un email est déjà utilisé (retourne un boiléen)
    Boolean existsByEmail(String email);

    // Compter les clients actifs pour le dashboard
    long countByRoleAndIsDeletedFalseAndIsGuestFalse(Role role);
}