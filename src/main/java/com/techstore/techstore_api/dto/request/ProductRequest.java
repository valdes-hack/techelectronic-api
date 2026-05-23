package com.techstore.techstore_api.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String sku;
    private String slug;
    private String description;
    private String brand;
    private Long categoryId;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private BigDecimal costPrice;
    private Integer stockQty;
    private String specifications;

    // LISTE DES LIENS DE PHOTOS
    private List<String> imageUrls; 
    
    private List<VariantRequest> variants;

    @Data
    public static class VariantRequest {
        private String skuVariant;
        private BigDecimal price;
        private BigDecimal costPrice;
        private Integer stockQty;
        private String attributes;
    }
}