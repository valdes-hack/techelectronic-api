package com.techstore.techstore_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * TABLE DE LIAISON (Many-to-Many) entre {@link Collection} et {@link Product}.
 *
 * Un produit peut appartenir à plusieurs collections et une collection
 * contient plusieurs produits. Le champ sortOrder permet de contrôler
 * l'ordre d'affichage des produits DANS une collection donnée.
 *
 * L'unicité (collection_id, product_id) est garantie par la base de données.
 */
@Entity
@Table(name = "product_collections",
        uniqueConstraints = @UniqueConstraint(name = "uk_collection_product", columnNames = {"collection_id", "product_id"}),
        indexes = {
                @Index(name = "idx_pc_collection", columnList = "collection_id"),
                @Index(name = "idx_pc_product", columnList = "product_id")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pc_collection"))
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pc_product"))
    private Product product;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
