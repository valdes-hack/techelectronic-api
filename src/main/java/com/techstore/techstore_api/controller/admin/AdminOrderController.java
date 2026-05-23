package com.techstore.techstore_api.controller.admin;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.OrderResponse;
import com.techstore.techstore_api.model.OrderStatus;
import com.techstore.techstore_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 1. VOIR TOUTES LES COMMANDES DU SITE
     * GET /api/v1/admin/orders
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrdersForAdmin();
        return ResponseEntity.ok(ApiResponse.<List<OrderResponse>>builder()
                .status("success")
                .code(200)
                .message("Toutes les commandes récupérées")
                .timestamp(LocalDateTime.now())
                .data(orders)
                .build());
    }

    /**
     * 2. CHANGER LE STATUT D'UNE COMMANDE
     * PATCH /api/v1/admin/orders/{id}/status?status=EXPEDIE
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        
        OrderResponse response = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .status("success")
                .code(200)
                .message("Statut de la commande mis à jour : " + status)
                .timestamp(LocalDateTime.now())
                .data(response)
                .build());
    }
}