package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.CollectionResponse;
import com.techstore.techstore_api.dto.response.ProductResponse;
import com.techstore.techstore_api.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CONTRÔLEUR PUBLIC DES COLLECTIONS (Rayons de la boutique).
 *
 *  GET /api/v1/collections                    -> toutes les collections actives
 *  GET /api/v1/collections/{slug}             -> détail d'une collection
 *  GET /api/v1/collections/{slug}/products    -> produits de la collection (paginés)
 */
@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * 1. TOUTES LES COLLECTIONS ACTIVES (pour la vitrine et la Navbar)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CollectionResponse>>> getAll() {
        List<CollectionResponse> collections = collectionService.getActiveCollections();
        return ResponseEntity.ok(
                ApiResponse.<List<CollectionResponse>>builder()
                        .status("success")
                        .code(200)
                        .message("Collections récupérées")
                        .timestamp(LocalDateTime.now())
                        .data(collections)
                        .build()
        );
    }

    /**
     * 2. DÉTAIL D'UNE COLLECTION (par slug)
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<CollectionResponse>> getBySlug(@PathVariable String slug) {
        CollectionResponse collection = collectionService.getCollectionBySlug(slug);
        return ResponseEntity.ok(
                ApiResponse.<CollectionResponse>builder()
                        .status("success")
                        .code(200)
                        .message("Collection récupérée")
                        .timestamp(LocalDateTime.now())
                        .data(collection)
                        .build()
        );
    }

    /**
     * 3. PRODUITS D'UNE COLLECTION (paginés, uniquement actifs)
     * Exemple : /api/v1/collections/couveuses/products?page=0&size=12
     */
    @GetMapping("/{slug}/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = collectionService.getProductsByCollection(slug, pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<ProductResponse>>builder()
                        .status("success")
                        .code(200)
                        .message("Produits de la collection récupérés")
                        .timestamp(LocalDateTime.now())
                        .data(products)
                        .build()
        );
    }
}
