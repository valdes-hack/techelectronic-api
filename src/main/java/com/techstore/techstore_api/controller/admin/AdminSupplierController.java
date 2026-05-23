package com.techstore.techstore_api.controller.admin;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.model.Supplier;
import com.techstore.techstore_api.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/suppliers")
@RequiredArgsConstructor
public class AdminSupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Supplier>>> getAll() {
        return ResponseEntity.ok(ApiResponse.<List<Supplier>>builder()
                .status("success").data(supplierService.getAllSuppliers()).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Supplier>> create(@RequestBody Supplier supplier) {
        return ResponseEntity.ok(ApiResponse.<Supplier>builder()
                .status("success").data(supplierService.createSupplier(supplier)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().status("success").build());
    }
    /**
     * 1. VOIR LE DÉTAIL D'UN FOURNISSEUR
     * GET /api/v1/admin/suppliers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> getById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(ApiResponse.<Supplier>builder()
                .status("success")
                .data(supplier)
                .build());
    }

    /**
     * 2. MODIFIER UN FOURNISSEUR
     * PUT /api/v1/admin/suppliers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> update(
            @PathVariable Long id, 
            @RequestBody Supplier supplier) {
        
        Supplier updated = supplierService.updateSupplier(id, supplier);
        return ResponseEntity.ok(ApiResponse.<Supplier>builder()
                .status("success")
                .message("Fournisseur mis à jour avec succès")
                .data(updated)
                .build());
    }
}