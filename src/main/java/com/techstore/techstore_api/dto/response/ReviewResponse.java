package com.techstore.techstore_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Avis produit expose au public : aucune donnee interne (utilisateur, commande) n'est serialisee.
 */
@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long productId;
    private Integer rating;
    private String title;
    private String body;
    private boolean verified;
    /** Nom affichable de l'auteur, ex : "Valdes H." */
    private String authorName;
    private LocalDateTime createdAt;
}
