package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.ProductResponse;
import com.techstore.techstore_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor

public class ProductController {

    private final ProductService productService;

    // 1. Lister tous les produits (avec pagination)
    // Exemple : /api/v1/products?page=0&size=10
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("id")));
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        
        return ResponseEntity.ok(ApiResponse.<Page<ProductResponse>>builder()
                .status("success").code(200).message("Liste des produits rÃ©cupÃ©rÃ©e")
                .timestamp(LocalDateTime.now()).data(products).build());
    }

    // 2. Voir un produit spÃ©cifique (DÃ©tail)
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySlug(@PathVariable String slug) {
        ProductResponse product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .status("success").code(200).message("DÃ©tail du produit")
                .timestamp(LocalDateTime.now()).data(product).build());
    }

    // 3. Rechercher des produits
    // Exemple : /api/v1/products/search?q=iphone
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> results = productService.searchProducts(query, pageable);
        
        return ResponseEntity.ok(ApiResponse.<Page<ProductResponse>>builder()
                .status("success").code(200).message("RÃ©sultats de recherche pour : " + query)
                .timestamp(LocalDateTime.now()).data(results).build());
    }

    // 4. Filtrer par catÃ©gorie
    /**
 * FILTRER LES PRODUITS PAR CATÃ‰GORIE
 * GET /api/v1/products/category/smartphones
 */
@GetMapping("/category/{slug}")
public ResponseEntity<ApiResponse<Page<ProductResponse>>> getByCategory(
        @PathVariable String slug,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<ProductResponse> products = productService.getProductsByCategory(slug, pageable);
    
    return ResponseEntity.ok(
        ApiResponse.<Page<ProductResponse>>builder()
            .status("success")
            .code(200)
            .message("Produits de la catÃ©gorie : " + slug)
            .timestamp(LocalDateTime.now())
            .data(products)
            .build()
    );
}
}
