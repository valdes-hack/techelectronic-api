package com.techstore.techstore_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * COLLECTION (Rayon / Groupe marketing) — Modèle "Shopify-like".
 *
 * Une collection regroupe des produits partageant un point commun :
 *  - Par TYPE de produit  : Poussins, Couveuses, Aliments, Abreuvoirs, Mangeoires, Matériel vétérinaire
 *  - Par USAGE            : Démarrage en aviculture, Élevage porcin, Petit ruminant
 *  - Par PROMOTION        : Nouveautés, Meilleures ventes, Promotions
 *
 * La liaison avec les produits est gérée par l'entité ProductCollection (table de liaison),
 * ce qui permet à un produit d'appartenir à PLUSIEURS collections (ex: une couveuse peut
 * être dans "Couveuses" ET dans "Promotions").
 */
@Entity
@Table(name = "collections")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(exclude = {"links"})
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Emoji ou URL d'image (même logique que les catégories : ex "🐣")
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    // Photo ou image d'arrière-plan de la collection (bannière)
    @Column(name = "banner_url", length = 1000)
    private String bannerUrl;

    /**
     * Type de collection : TYPE (type de produit), USAGE (mode d'élevage), PROMO (marketing).
     * Sert uniquement au front pour grouper l'affichage. Libre, non bloquant.
     */
    @Column(name = "collection_type", length = 50)
    @Builder.Default
    private String collectionType = "TYPE";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Liaisons produits <-> collection. LAZY pour éviter le chargement inutile,
     * jamais sérialisé en JSON (on passe par les DTOs) -> pas de boucle infinie.
     */
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<ProductCollection> links = new java.util.ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return isActive != null && isActive;
    }
}
