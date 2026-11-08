package com.techstore.techstore_api.controller.admin;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.DashboardStatsResponse;
import com.techstore.techstore_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    /**
     * OBTENIR TOUTES LES STATISTIQUES DU DASHBOARD
     * GET /api/v1/admin/dashboard
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.<DashboardStatsResponse>builder()
                .status("success")
                .code(200)
                .message("Statistiques du dashboard récupérées avec succès")
                .timestamp(LocalDateTime.now())
                .data(stats)
                .build());
    }

    /**
     * OBTENIR TOUS LES PRODUITS EN STOCK CRITIQUE
     * GET /api/v1/admin/dashboard/low-stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<java.util.List<DashboardStatsResponse.LowStockItemDTO>>> getLowStockItems() {
        java.util.List<DashboardStatsResponse.LowStockItemDTO> lowStockItems = dashboardService.getLowStockItems();
        return ResponseEntity.ok(ApiResponse.<java.util.List<DashboardStatsResponse.LowStockItemDTO>>builder()
                .status("success")
                .code(200)
                .message("Produits en stock critique récupérés avec succès")
                .timestamp(LocalDateTime.now())
                .data(lowStockItems)
                .build());
    }
}
