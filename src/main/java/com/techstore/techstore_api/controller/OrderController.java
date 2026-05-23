package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.request.OrderRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.OrderResponse;
import com.techstore.techstore_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    /**
     * 1. PASSER UNE COMMANDE
     * Transforme le panier actuel en commande
     */
    @PostMapping
public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
        @RequestBody OrderRequest request, 
        Principal principal) {
    
    String email = (principal != null) ? principal.getName() : null;
    OrderResponse response = orderService.createOrder(request, email);
    
    return ResponseEntity.status(201).body(
        ApiResponse.<OrderResponse>builder()
            .status("success")
            .code(201)
            .message("Commande créée")
            .timestamp(LocalDateTime.now())
            .data(response)
            .build()
    );
}
    /**
     * 2. VOIR MES COMMANDES
     * Liste l'historique des achats du client
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Principal principal) {
        List<OrderResponse> orders = orderService.getMyOrders(principal.getName());
        
        return ResponseEntity.ok(
            ApiResponse.<List<OrderResponse>>builder()
                .status("success")
                .code(200)
                .message("Historique des commandes récupéré")
                .timestamp(LocalDateTime.now())
                .data(orders)
                .build()
        );
    }

    /**
     * 3. VOIR LE DÉTAIL D'UNE COMMANDE
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(
            @PathVariable Long id, 
            Principal principal) {
        
        OrderResponse response = orderService.getOrderDetails(id, principal.getName());
        
        return ResponseEntity.ok(
            ApiResponse.<OrderResponse>builder()
                .status("success")
                .code(200)
                .data(response)
                .build()
        );
    }
    /**
 * SUIVRE UNE COMMANDE SANS COMPTE
 * GET /api/v1/orders/track/A1B2C3D4
 */
@GetMapping("/track/{token}")
public ResponseEntity<ApiResponse<OrderResponse>> trackOrder(@PathVariable String token) {
    OrderResponse response = orderService.getOrderByToken(token);
    
    return ResponseEntity.ok(
        ApiResponse.<OrderResponse>builder()
            .status("success")
            .code(200)
            .message("Informations de suivi récupérées")
            .timestamp(LocalDateTime.now())
            .data(response)
            .build()
    );
}
}