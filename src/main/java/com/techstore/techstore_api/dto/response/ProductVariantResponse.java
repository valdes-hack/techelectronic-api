package com.techstore.techstore_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private String skuVariant;
    private BigDecimal price;
    private Integer stockQty;
    private String attributes; // C'est ici que React lira le JSON (Couleur, RAM, etc.)
}