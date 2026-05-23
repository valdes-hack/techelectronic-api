package com.techstore.techstore_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String variantAttributes; 
    private String imageUrl; // <--- INDISPENSABLE pour React
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
}