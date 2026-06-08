package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.request.AddressRequest;
import com.techstore.techstore_api.dto.response.AddressResponse;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> add(@RequestBody AddressRequest request, Principal principal) {
        AddressResponse response = addressService.addAddress(request, principal.getName());
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .status("success").code(201).message("Adresse ajoutÃ©e")
                .timestamp(LocalDateTime.now()).data(response).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses(Principal principal) {
        List<AddressResponse> addresses = addressService.getMyAddresses(principal.getName());
        return ResponseEntity.ok(ApiResponse.<List<AddressResponse>>builder()
                .status("success").code(200).data(addresses).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> update(@PathVariable Long id, @RequestBody AddressRequest request, Principal principal) {
        AddressResponse response = addressService.updateAddress(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .status("success").code(200).message("Adresse mise Ã  jour").data(response).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Principal principal) {
        addressService.deleteAddress(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Adresse supprimÃ©e").build());
    }
}
