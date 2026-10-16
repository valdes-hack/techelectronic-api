package com.techstore.techstore_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String skuVariant;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer stockQty;

    @Column(columnDefinition = "JSON")
    private String attributes; // {"Couleur": "Noir", "Taille": "M"}

    @Column
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Alias method for service compatibility
    public void setCostPrice(Long costPrice) {
        // Cost price is not stored in variant, but we accept it for compatibility
    }

    public Long getCostPrice() {
        return null; // Cost price is stored in Product, not Variant
    }
}
