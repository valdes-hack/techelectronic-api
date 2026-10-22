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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        Page<ProductResponse> products = productService.getAllProductsForAdmin(pageable); 
        
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
            @RequestPart("product") Object productInput, 
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        try {
            ProductRequest request = parseProductRequest(productInput);

            if (files != null && !files.isEmpty()) {
                List<String> imageUrls = files.stream()
                        .map(file -> {
                            String storedUrl = fileStorageService.storeFile(file);
                            return storedUrl.startsWith("http") ? storedUrl : baseUrl + "/uploads/products/" + storedUrl;
                        })
                        .collect(Collectors.toList());
                request.setImageUrls(imageUrls);
            }

            ProductResponse response = productService.createProduct(request);
            return ResponseEntity.status(201).body(ApiResponse.<ProductResponse>builder()
                    .status("success").code(201).message("Produit créé").data(response).build());

        } catch (Exception e) {
            log.error("❌ Erreur lors de la création du produit : ", e);
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
            @RequestPart("product") Object productInput, 
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        try {
            ProductRequest request = parseProductRequest(productInput);

            if (files != null && !files.isEmpty()) {
                List<String> newUrls = files.stream()
                        .map(file -> {
                            try {
                                String storedUrl = fileStorageService.storeFile(file);
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
            log.error("❌ Erreur lors de la mise à jour du produit {} : ", id, e);
            return ResponseEntity.status(500).body(ApiResponse.<ProductResponse>builder()
                    .status("error").code(500).message("Erreur mise à jour : " + e.getMessage()).build());
        }
    }

    private ProductRequest parseProductRequest(Object productInput) throws Exception {
        if (productInput == null) {
            return new ProductRequest();
        }
        if (productInput instanceof ProductRequest pr) {
            return pr;
        } else if (productInput instanceof String str) {
            return objectMapper.readValue(str, ProductRequest.class);
        } else if (productInput instanceof MultipartFile mf) {
            return objectMapper.readValue(mf.getInputStream(), ProductRequest.class);
        } else if (productInput instanceof byte[] bytes) {
            return objectMapper.readValue(bytes, ProductRequest.class);
        } else {
            return objectMapper.convertValue(productInput, ProductRequest.class);
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
     * SUPPRESSION DÉFINITIVE D'UN PRODUIT (HARD DELETE)
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDelete(@PathVariable Long id) {
        productService.hardDeleteProduct(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Produit supprimé définitivement").build());
    }

    /**
     * PURGE DE TOUS LES PRODUITS
     */
    @DeleteMapping("/purge-all")
    public ResponseEntity<ApiResponse<Void>> purgeAll() {
        productService.purgeAllProducts();
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Tous les produits ont été supprimés avec succès").build());
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

    /**
     * 8. ACTIVER / DÉSACTIVER UN PRODUIT (METTRE EN LIGNE / HORS LIGNE)
     */
    @RequestMapping(value = "/{id}/toggle-status", method = { RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST })
    public ResponseEntity<ApiResponse<ProductResponse>> toggleProductStatus(@PathVariable Long id) {
        ProductResponse response = productService.toggleProductStatus(id);
        String message = Boolean.TRUE.equals(response.getIsActive()) ? "Produit mis en ligne avec succès" : "Produit retiré de la ligne";
        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .status("success").code(200).message(message).data(response).build());
    }
}