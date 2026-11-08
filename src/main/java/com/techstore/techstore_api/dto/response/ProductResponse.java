package com.techstore.techstore_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String slug;
    private String description;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private Double ratingAvg;
    private Integer stockQty;
    private Boolean isActive;
    private String specifications;
    
    // Listes liées
    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> variants;
}