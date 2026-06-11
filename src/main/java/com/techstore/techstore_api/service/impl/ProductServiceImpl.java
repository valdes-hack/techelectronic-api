package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.dto.request.ProductRequest;
import com.techstore.techstore_api.dto.request.VariantRequest;
import com.techstore.techstore_api.dto.response.ProductImageResponse;
import com.techstore.techstore_api.dto.response.ProductResponse;
import com.techstore.techstore_api.dto.response.ProductVariantResponse;
import com.techstore.techstore_api.model.*;
import com.techstore.techstore_api.repository.*;
import com.techstore.techstore_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional; // ✨ AJOUTE CET IMPORT ✨
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;

    // ==========================================
    // 🔍 LECTURE (PUBLIC & ADMIN)
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable)
                .map(this::mapToProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id : " + id));
        return mapToProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        return mapToProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable)
                .map(this::mapToProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(String categorySlug, Pageable pageable) {
        Category category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée : " + categorySlug));
        
        return productRepository.findByCategoryIdAndIsActiveTrue(category.getId(), pageable)
                .map(this::mapToProductResponse);
    }

    // ==========================================
    // 🛠️ GESTION PRODUITS (ADMIN)
    // ==========================================

   @Override
@Transactional
public ProductResponse createProduct(ProductRequest request) {
    Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Catégorie inexistante."));

    Product product = Product.builder()
            .name(request.getName())
            .sku(request.getSku())
            .slug(request.getSlug())
            .description(request.getDescription())
            .brand(request.getBrand())
            .category(category)
            .basePrice(request.getBasePrice())
            .costPrice(request.getCostPrice())
            .stockQty(0) 
            .isActive(true)
            .build();
    
    Product savedProduct = productRepository.save(product);

    // ✨ SAUVEGARDE DES IMAGES
    if (request.getImageUrls() != null) {
        request.getImageUrls().forEach(url -> {
            imageRepository.save(ProductImage.builder()
                    .url(url).product(savedProduct).isPrimary(false).build());
        });
    }

    // ✨ SAUVEGARDE DES VARIANTES (Si présentes à la création)
    if (request.getVariants() != null) {
        request.getVariants().forEach(vReq -> {
            variantRepository.save(ProductVariant.builder()
                    .product(savedProduct)
                    .skuVariant(vReq.getSkuVariant())
                    .price(vReq.getPrice())
                    .stockQty(vReq.getStockQty())
                    .attributes(vReq.getAttributes())
                    .build());
        });
    }

    return mapToProductResponse(savedProduct);
}
@Override
@Transactional
public ProductResponse updateProduct(Long id, ProductRequest request) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

    // 1. Mise à jour de TOUS les champs (pas seulement 4)
    product.setName(request.getName());
    
    // Ne mettre à jour le SKU que s'il est différent et n'existe pas déjà
    if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
        if (productRepository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw new RuntimeException("SKU déjà utilisé par un autre produit");
        }
        product.setSku(request.getSku());
    }
    
    // Ne mettre à jour le slug que s'il est différent et n'existe pas déjà
    if (request.getSlug() != null && !request.getSlug().equals(product.getSlug())) {
        if (productRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            throw new RuntimeException("Slug déjà utilisé par un autre produit");
        }
        product.setSlug(request.getSlug());
    }
    
    product.setBrand(request.getBrand());
    product.setDescription(request.getDescription());
    product.setBasePrice(request.getBasePrice());
    product.setCostPrice(request.getCostPrice());
    
    if(request.getCategoryId() != null) {
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow();
        product.setCategory(category);
    }

    // 2. Gestion des images : On ne supprime que si on reçoit une nouvelle liste
    if (request.getImageUrls() != null) {
        imageRepository.deleteByProductId(id);
        request.getImageUrls().forEach(url -> {
            imageRepository.save(ProductImage.builder()
                    .url(url).product(product).build());
        });
    }

    // 3. Mise à jour des variantes
    if (request.getVariants() != null) {
        request.getVariants().forEach(vReq -> {
            // Chercher par SKU de variante
            ProductVariant variant = variantRepository.findBySkuVariant(vReq.getSkuVariant())
                    .orElse(new ProductVariant());

            variant.setProduct(product);
            variant.setSkuVariant(vReq.getSkuVariant());
            variant.setPrice(vReq.getPrice());
            variant.setStockQty(vReq.getStockQty());
            variant.setAttributes(vReq.getAttributes());
            variantRepository.save(variant);
        });
    }

    return mapToProductResponse(productRepository.save(product));
}
  @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setActive(false); // Soft Delete
        productRepository.save(product);
    }

    // ==========================================
    // 🎨 GESTION VARIANTES (ADMIN)
    // ==========================================

    @Override
    @Transactional
    public ProductVariantResponse addVariant(Long productId, VariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit parent non trouvé"));

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .skuVariant(request.getSkuVariant())
                .price(request.getPrice())
                .costPrice(request.getCostPrice())
                .stockQty(request.getStockQty() != null ? request.getStockQty() : 0)
                .attributes(request.getAttributes())
                .build();

        return mapToVariantResponse(variantRepository.save(variant));
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariant(Long variantId, VariantRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variante non trouvée"));

        variant.setSkuVariant(request.getSkuVariant());
        variant.setPrice(request.getPrice());
        variant.setCostPrice(request.getCostPrice());
        variant.setStockQty(request.getStockQty());
        variant.setAttributes(request.getAttributes());

        return mapToVariantResponse(variantRepository.save(variant));
    }

    @Override
    @Transactional
    public void deleteVariant(Long variantId) {
        variantRepository.deleteById(variantId);
    }

    // ==========================================
    // 🔄 MAPPING TOOLS
    // ==========================================

private ProductResponse mapToProductResponse(Product product) {
    // 1. On récupère les variantes
    List<ProductVariant> variants = variantRepository.findByProductId(product.getId());

    // 2. ✨ CALCUL DU STOCK TOTAL ✨
    // Si le produit a des variantes, on fait la somme. Sinon, on prend le stockQty du produit.
    int totalStock = 0;
    if (!variants.isEmpty()) {
        totalStock = variants.stream()
                .mapToInt(v -> v.getStockQty() != null ? v.getStockQty() : 0)
                .sum();
    } else {
        totalStock = product.getStockQty() != null ? product.getStockQty() : 0;
    }

    // 3. Mapping des images (inchangé)
    List<ProductImageResponse> imageResponses = imageRepository.findByProductIdOrderBySortOrderAsc(product.getId()).stream()
            .map(img -> ProductImageResponse.builder()
                    .id(img.getId()).url(img.getUrl())
                    .altText(img.getAltText()).isPrimary(img.isPrimary())
                    .build())
            .collect(Collectors.toList());

    // 4. Mapping des variantes
    List<ProductVariantResponse> variantResponses = variants.stream()
            .map(this::mapToVariantResponse)
            .collect(Collectors.toList());

    return ProductResponse.builder()
            .id(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .slug(product.getSlug())
            .description(product.getDescription())
            .brand(product.getBrand())
            .categoryName(product.getCategory().getName())
            .basePrice(product.getBasePrice())
            .discountPrice(product.getDiscountPrice())
            .ratingAvg(product.getRatingAvg())
            // ✨ ON ENVOIE LE TOTAL CALCULÉ ✨
            .stockQty(totalStock) 
            .specifications(product.getSpecifications())
            .images(imageResponses)
            .variants(variantResponses)
            .build();
}

    private ProductVariantResponse mapToVariantResponse(ProductVariant v) {
        return ProductVariantResponse.builder()
                .id(v.getId())
                .skuVariant(v.getSkuVariant())
                .price(v.getPrice())
                .stockQty(v.getStockQty())
                .attributes(v.getAttributes())
                .build();
    }
}