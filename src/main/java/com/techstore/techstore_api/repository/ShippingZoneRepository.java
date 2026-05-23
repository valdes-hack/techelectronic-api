package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.ShippingZone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShippingZoneRepository extends JpaRepository<ShippingZone, Long> {
    // Récupérer uniquement les zones actives
    List<ShippingZone> findByIsActiveTrue();
}