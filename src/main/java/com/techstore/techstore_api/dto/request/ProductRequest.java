package com.techstore.techstore_api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequest {
    private Long id;
    private String name;
    private String sku;
    private String slug;
    private String description;
    private String brand;
    private Long categoryId;
    private Long defaultSupplierId;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private BigDecimal costPrice;
    private Integer stockQty;
    private String specifications;

    // LISTE DES LIENS DE PHOTOS
    private List<String> imageUrls; 
    
    private List<VariantRequest> variants;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VariantRequest {
        private Long id;
        private String skuVariant;
        private BigDecimal price;
        private BigDecimal costPrice;
        private Integer stockQty;
        private String attributes;
    }
}