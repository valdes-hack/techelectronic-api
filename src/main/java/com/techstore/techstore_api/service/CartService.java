package com.techstore.techstore_api.service;

import com.techstore.techstore_api.dto.request.CartItemRequest;
import com.techstore.techstore_api.dto.response.CartResponse;

public interface CartService {
    CartResponse addToCart(CartItemRequest request, String identifier, boolean isUser);
    CartResponse getMyCart(String identifier, boolean isUser);
    CartResponse updateQuantity(Long itemId, Integer quantity);
    CartResponse removeFromCart(Long itemId, String identifier, boolean isUser);
    void clearCart(String identifier, boolean isUser);
    CartResponse mergeCart(String sessionId, String userEmail);
}