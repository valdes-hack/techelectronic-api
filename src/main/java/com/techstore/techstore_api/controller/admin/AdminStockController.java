package com.techstore.techstore_api.controller.admin;

import com.techstore.techstore_api.dto.request.StockRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/stock")
@RequiredArgsConstructor
public class AdminStockController {

    private final StockService stockService;

    @PostMapping("/supply")
    public ResponseEntity<ApiResponse<Void>> supply(@RequestBody StockRequest request) {
        stockService.supplyProduct(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success")
                .code(200)
                .message("Ravitaillement effectué avec succès")
                .timestamp(LocalDateTime.now())
                .build());
    }
}