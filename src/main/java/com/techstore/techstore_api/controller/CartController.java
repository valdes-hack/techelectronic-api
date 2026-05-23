package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.request.CartItemRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.CartResponse;
import com.techstore.techstore_api.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    /**
     * 1. VOIR LE PANIER (Public ou Privé)
     * Utilise soit le Token (Principal), soit le Header X-Session-Id
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            Principal principal) {
        
        boolean isUser = (principal != null);
        String identifier = isUser ? principal.getName() : sessionId;

        if (identifier == null) {
            return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                    .status("success").code(200).message("Panier vide (pas de session)")
                    .timestamp(LocalDateTime.now()).build());
        }

        CartResponse response = cartService.getMyCart(identifier, isUser);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .status("success").code(200).message("Panier récupéré")
                .timestamp(LocalDateTime.now()).data(response).build());
    }

    /**
     * 2. AJOUTER UN PRODUIT AU PANIER
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestBody CartItemRequest request,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            Principal principal) {
        
        boolean isUser = (principal != null);
        String identifier = isUser ? principal.getName() : sessionId;

        if (identifier == null) {
            throw new RuntimeException("Identifiant de session (X-Session-Id) manquant pour le visiteur");
        }

        CartResponse response = cartService.addToCart(request, identifier, isUser);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .status("success").code(200).message("Produit ajouté au panier")
                .timestamp(LocalDateTime.now()).data(response).build());
    }

    /**
     * 3. MODIFIER LA QUANTITÉ D'UN ARTICLE
     */
    @PutMapping("/update/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        
        CartResponse response = cartService.updateQuantity(itemId, quantity);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .status("success").code(200).message("Quantité mise à jour")
                .timestamp(LocalDateTime.now()).data(response).build());
    }

    /**
     * 4. SUPPRIMER UN ARTICLE DU PANIER
     */
    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            Principal principal) {
        
        boolean isUser = (principal != null);
        String identifier = isUser ? principal.getName() : sessionId;

        CartResponse response = cartService.removeFromCart(itemId, identifier, isUser);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .status("success").code(200).message("Article retiré du panier")
                .timestamp(LocalDateTime.now()).data(response).build());
    }

    /**
     * 5. VIDER LE PANIER
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            Principal principal) {
        
        boolean isUser = (principal != null);
        String identifier = isUser ? principal.getName() : sessionId;

        cartService.clearCart(identifier, isUser);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Panier vidé avec succès")
                .timestamp(LocalDateTime.now()).build());
    }

    /**
     * 6. FUSIONNER LE PANIER (Appelé par React après le Login)
     */
    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<CartResponse>> mergeCart(
            @RequestParam String sessionId,
            Principal principal) {
        
        if (principal == null) {
            throw new RuntimeException("Vous devez être connecté pour fusionner un panier");
        }

        CartResponse response = cartService.mergeCart(sessionId, principal.getName());
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .status("success").code(200).message("Paniers fusionnés avec succès")
                .timestamp(LocalDateTime.now()).data(response).build());
    }
}