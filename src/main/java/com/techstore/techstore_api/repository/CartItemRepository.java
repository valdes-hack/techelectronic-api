package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // On pourra ajouter des méthodes ici si besoin
}