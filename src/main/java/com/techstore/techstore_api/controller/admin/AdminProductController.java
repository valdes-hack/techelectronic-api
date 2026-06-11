package com.techstore.techstore_api.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.techstore_api.dto.request.ProductRequest;
import com.techstore.techstore_api.dto.request.VariantRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.ProductResponse;
import com.techstore.techstore_api.dto.response.ProductVariantResponse;
import com.techstore.techstore_api.service.FileStorageService;
import com.techstore.techstore_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * 1. LISTER TOUS LES PRODUITS (ADMIN)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getAllProducts(pageable); 
        
        return ResponseEntity.ok(ApiResponse.<Page<ProductResponse>>builder()
                .status("success").code(200).message("Inventaire récupéré")
                .timestamp(LocalDateTime.now()).data(products).build());
    }

    /**
     * 2. VOIR LE DÉTAIL D'UN PRODUIT
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .status("success").code(200).data(response).build());
    }

    /**
     * 3. CRÉER UN PRODUIT (MULTIPART : JSON + FILES)
     */
    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestPart("product") String productJson, 
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        try {
            ProductRequest request = objectMapper.readValue(productJson, ProductRequest.class);

            if (files != null && !files.isEmpty()) {
                List<String> imageUrls = files.stream()
                        .map(file -> {
                            String storedUrl = fileStorageService.storeFile(file);
                            // Si l'URL est déjà complète (Cloudinary), l'utiliser telle quelle
                            // Sinon, ajouter le préfixe local
                            return storedUrl.startsWith("http") ? storedUrl : baseUrl + "/uploads/products/" + storedUrl;
                        })
                        .collect(Collectors.toList());
                request.setImageUrls(imageUrls);
            }

            ProductResponse response = productService.createProduct(request);
            return ResponseEntity.status(201).body(ApiResponse.<ProductResponse>builder()
                    .status("success").code(201).message("Produit créé").data(response).build());

        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<ProductResponse>builder()
                    .status("error").code(500).message("Erreur création : " + e.getMessage()).build());
        }
    }

    /**
     * 4. MODIFIER UN PRODUIT (MULTIPART : JSON + FILES)
     */
    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") String productJson, 
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        try {
            ProductRequest request = objectMapper.readValue(productJson, ProductRequest.class);

            if (files != null && !files.isEmpty()) {
                List<String> newUrls = files.stream()
                        .map(file -> {
                            try {
                                String storedUrl = fileStorageService.storeFile(file);
                                // Si l'URL est déjà complète (Cloudinary), l'utiliser telle quelle
                                // Sinon, ajouter le préfixe local
                                return storedUrl.startsWith("http") ? storedUrl : baseUrl + "/uploads/products/" + storedUrl;
                            } catch (Exception e) {
                                throw new RuntimeException("Erreur lors du stockage de l'image: " + e.getMessage(), e);
                            }
                        })
                        .collect(Collectors.toList());
                
                if (request.getImageUrls() == null) request.setImageUrls(new java.util.ArrayList<>());
                request.getImageUrls().addAll(newUrls);
            }

            ProductResponse response = productService.updateProduct(id, request);
            return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                    .status("success").code(200).message("Produit mis à jour").data(response).build());

        } catch (Exception e) {
            e.printStackTrace(); // Pour voir l'erreur complète dans les logs
            return ResponseEntity.status(500).body(ApiResponse.<ProductResponse>builder()
                    .status("error").code(500).message("Erreur mise à jour : " + e.getMessage()).build());
        }
    }

    /**
     * 5. SUPPRIMER UN PRODUIT (SOFT DELETE)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Produit désactivé").build());
    }

    /**
     * 6. AJOUTER UNE VARIANTE
     */
    @PostMapping("/{productId}/variants")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> addVariant(
            @PathVariable Long productId,
            @RequestBody VariantRequest request) {
        
        ProductVariantResponse response = productService.addVariant(productId, request);
        return ResponseEntity.ok(ApiResponse.<ProductVariantResponse>builder()
                .status("success").message("Variante ajoutée").data(response).build());
    }

    /**
     * 7. SUPPRIMER UNE VARIANTE
     */
    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable Long variantId) {
        productService.deleteVariant(variantId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").message("Variante supprimée").build());
    }
}