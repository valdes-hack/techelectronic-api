package com.techstore.techstore_api.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockRequest {
    private Long productId;
    private Long variantId;   // Optionnel
    private Long supplierId;  // Optionnel (ID 1 par défaut)
    private Integer quantity;
    private BigDecimal purchasePrice;
}