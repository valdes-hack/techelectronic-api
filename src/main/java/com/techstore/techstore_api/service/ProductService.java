package com.techstore.techstore_api.service;

import com.techstore.techstore_api.dto.request.ProductRequest;
import com.techstore.techstore_api.dto.response.ProductResponse;
import com.techstore.techstore_api.dto.response.ProductVariantResponse;
import com.techstore.techstore_api.dto.request.VariantRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    // Récupérer tous les produits (paginés)
    Page<ProductResponse> getAllProducts(Pageable pageable);

    // Récupérer les produits d'une catégorie (via son slug)
    Page<ProductResponse> getProductsByCategory(String categorySlug, Pageable pageable);

    // Rechercher des produits
    Page<ProductResponse> searchProducts(String keyword, Pageable pageable);

    // Détail d'un produit via son slug
    ProductResponse getProductById(Long id);
    ProductResponse getProductBySlug(String slug);
    ProductResponse createProduct(ProductRequest productRequest);
    ProductResponse updateProduct(Long id, ProductRequest productRequest);
    // Dans ProductService.java
ProductVariantResponse addVariant(Long productId, VariantRequest request);
ProductVariantResponse updateVariant(Long variantId, VariantRequest request);
void deleteVariant(Long variantId);
void deleteProduct(Long id);
}