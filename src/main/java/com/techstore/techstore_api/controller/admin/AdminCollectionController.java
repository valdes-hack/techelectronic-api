package com.techstore.techstore_api.controller.admin;

import com.techstore.techstore_api.dto.request.CollectionRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.CollectionResponse;
import com.techstore.techstore_api.dto.response.ProductResponse;
import com.techstore.techstore_api.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTRÔLEUR ADMIN DES COLLECTIONS.
 *
 *  GET    /api/v1/admin/collections                 -> toutes les collections (y compris inactives)
 *  GET    /api/v1/admin/collections/{id}            -> détail
 *  POST   /api/v1/admin/collections                 -> créer (optionnellement avec productIds)
 *  PUT    /api/v1/admin/collections/{id}            -> modifier (productIds => remplacement complet)
 *  DELETE /api/v1/admin/collections/{id}            -> supprimer (les produits sont conservés)
 *  GET    /api/v1/admin/collections/{id}/products   -> produits liés (y compris inactifs)
 *  POST   /api/v1/admin/collections/{id}/products/{productId}     -> lier un produit
 *  DELETE /api/v1/admin/collections/{id}/products/{productId}     -> délier un produit
 *  PUT    /api/v1/admin/collections/{id}/products   -> remplacer toute la liste (body: [1,2,3])
 *  GET    /api/v1/admin/collections/by-product/{productId}        -> collections contenant ce produit
 */
@RestController
@RequestMapping("/api/v1/admin/collections")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCollectionController {

    private final CollectionService collectionService;

    /**
     * 1. LISTER TOUTES LES COLLECTIONS (ADMIN)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CollectionResponse>>> getAll() {
        List<CollectionResponse> collections = collectionService.getAllCollections();
        return ResponseEntity.ok(ApiResponse.<List<CollectionResponse>>builder()
                .status("success").code(200).data(collections).build());
    }

    /**
     * 2. DÉTAIL D'UNE COLLECTION
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CollectionResponse>> getById(@PathVariable Long id) {
        CollectionResponse response = collectionService.getCollectionById(id);
        return ResponseEntity.ok(ApiResponse.<CollectionResponse>builder()
                .status("success").code(200).data(response).build());
    }

    /**
     * 3. CRÉER UNE COLLECTION
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CollectionResponse>> create(@RequestBody CollectionRequest request) {
        CollectionResponse response = collectionService.createCollection(request);
        return ResponseEntity.status(201).body(ApiResponse.<CollectionResponse>builder()
                .status("success").code(201).message("Collection créée").data(response).build());
    }

    /**
     * 4. MODIFIER UNE COLLECTION
     * Astuce : envoyer "productIds": [] pour vider la collection.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CollectionResponse>> update(
            @PathVariable Long id,
            @RequestBody CollectionRequest request) {
        CollectionResponse response = collectionService.updateCollection(id, request);
        return ResponseEntity.ok(ApiResponse.<CollectionResponse>builder()
                .status("success").code(200).message("Collection mise à jour").data(response).build());
    }

    /**
     * 5. SUPPRIMER UNE COLLECTION (les produits ne sont PAS supprimés)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Collection supprimée avec succès").build());
    }

    /**
     * 6. PRODUITS D'UNE COLLECTION (ADMIN, y compris produits désactivés)
     */
    @GetMapping("/{id}/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = collectionService.getProductsForAdmin(id, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ProductResponse>>builder()
                .status("success").code(200).data(products).build());
    }

    /**
     * 7. LIER UN PRODUIT À LA COLLECTION
     */
    @PostMapping("/{id}/products/{productId}")
    public ResponseEntity<ApiResponse<CollectionResponse>> addProduct(
            @PathVariable Long id,
            @PathVariable Long productId) {
        CollectionResponse response = collectionService.addProductToCollection(id, productId);
        return ResponseEntity.ok(ApiResponse.<CollectionResponse>builder()
                .status("success").code(200).message("Produit ajouté à la collection").data(response).build());
    }

    /**
     * 8. DÉLIER UN PRODUIT DE LA COLLECTION
     */
    @DeleteMapping("/{id}/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeProduct(
            @PathVariable Long id,
            @PathVariable Long productId) {
        collectionService.removeProductFromCollection(id, productId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Produit retiré de la collection").build());
    }

    /**
     * 9. REMPLACER TOUTE LA LISTE DES PRODUITS (body : [1, 5, 12])
     */
    @PutMapping("/{id}/products")
    public ResponseEntity<ApiResponse<CollectionResponse>> setProducts(
            @PathVariable Long id,
            @RequestBody List<Long> productIds) {
        CollectionResponse response = collectionService.setCollectionProducts(id, productIds);
        return ResponseEntity.ok(ApiResponse.<CollectionResponse>builder()
                .status("success").code(200).message("Produits de la collection mis à jour").data(response).build());
    }

    /**
     * 10. COLLECTIONS CONTENANT UN PRODUIT DONNÉ (pour la page d'édition produit)
     */
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<ApiResponse<List<CollectionResponse>>> getByProduct(@PathVariable Long productId) {
        List<CollectionResponse> collections = collectionService.getCollectionsByProduct(productId);
        return ResponseEntity.ok(ApiResponse.<List<CollectionResponse>>builder()
                .status("success").code(200).data(collections).build());
    }
}
