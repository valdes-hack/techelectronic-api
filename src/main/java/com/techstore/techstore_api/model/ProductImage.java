package com.techstore.techstore_api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(exclude = "product")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference // Empêche de remonter vers le produit
    private Product product;
@Column(nullable = false, columnDefinition = "TEXT")
private String url;
    @Column(nullable = false, length = 500)
    private String altText;
    private boolean isPrimary = false;
    private Integer sortOrder = 0;
}