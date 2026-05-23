package com.techstore.techstore_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(exclude = {"images", "variants"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String brand;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "discount_price")
    private BigDecimal discountPrice;

    @JsonIgnore // Cache le prix d'achat
    @Column(name = "cost_price")
    private BigDecimal costPrice;

    @Column(name = "stock_qty")
    private Integer stockQty = 0;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "rating_avg")
    private Double ratingAvg = 0.0;

    @Column(name = "views_count")
    private Integer viewsCount = 0;

    @Column(columnDefinition = "LONGTEXT")
    private String specifications;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonManagedReference // Pour Swagger : Parent vers Enfants
    private List<ProductImage> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonManagedReference // Pour Swagger : Parent vers Enfants
    private List<ProductVariant> variants = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
}