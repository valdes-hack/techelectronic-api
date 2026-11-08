package com.techstore.techstore_api.service;

import com.techstore.techstore_api.dto.request.CollectionRequest;
import com.techstore.techstore_api.dto.response.CollectionResponse;
import com.techstore.techstore_api.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CollectionService {

    // ── LECTURE PUBLIC ─────────────────────────────────────────────
    List<CollectionResponse> getActiveCollections();
    CollectionResponse getCollectionBySlug(String slug);
    Page<ProductResponse> getProductsByCollection(String slug, Pageable pageable);

    // ── LECTURE ADMIN ──────────────────────────────────────────────
    List<CollectionResponse> getAllCollections();
    CollectionResponse getCollectionById(Long id);
    Page<ProductResponse> getProductsForAdmin(Long collectionId, Pageable pageable);
    List<CollectionResponse> getCollectionsByProduct(Long productId);

    // ── ÉCRITURE ADMIN ─────────────────────────────────────────────
    CollectionResponse createCollection(CollectionRequest request);
    CollectionResponse updateCollection(Long id, CollectionRequest request);
    void deleteCollection(Long id);

    CollectionResponse addProductToCollection(Long collectionId, Long productId);
    void removeProductFromCollection(Long collectionId, Long productId);
    CollectionResponse setCollectionProducts(Long collectionId, List<Long> productIds);
}
