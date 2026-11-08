package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    // Trouver toutes les adresses d'un utilisateur
    List<Address> findByUserId(Long userId);

    // Trouver l'adresse par défaut d'un utilisateur
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}