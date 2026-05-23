package com.techstore.techstore_api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_variants")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(exclude = "product")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference // Empêche de remonter vers le produit
    private Product product;

    @Column(unique = true, nullable = false)
    private String skuVariant;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stockQty = 0;
    
    @Column(columnDefinition = "LONGTEXT")
    private String attributes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}