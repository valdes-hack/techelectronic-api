package com.techstore.techstore_api.dto.request;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long productId;
    private Long variantId; // Peut être null
    private Integer quantity;
}