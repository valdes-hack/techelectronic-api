package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Lister les avis d'un produit
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    // Vérifier si l'utilisateur a déjà noté ce produit pour cette commande
    boolean existsByUserIdAndOrderIdAndProductId(Long userId, Long orderId, Long productId);

    // ✨ APPEL DE TA PROCÉDURE SQL ✨
    @Procedure(procedureName = "update_product_rating")
    void updateProductAverageRating(@Param("p_product_id") Long productId);
}