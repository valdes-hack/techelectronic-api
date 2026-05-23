package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.model.ShippingZone;
import com.techstore.techstore_api.service.ShippingZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping-zones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShippingZoneController {

    private final ShippingZoneService shippingZoneService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShippingZone>>> getZones() {
        List<ShippingZone> zones = shippingZoneService.getAllActiveZones();
        return ResponseEntity.ok(ApiResponse.<List<ShippingZone>>builder()
                .status("success")
                .code(200)
                .message("Zones de livraison récupérées")
                .timestamp(LocalDateTime.now())
                .data(zones)
                .build());
    }
}