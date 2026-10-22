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
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final CartItemRepository cartItemRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    // ==========================================
    // 🔍 LECTURE (PUBLIC & ADMIN)
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return mapProductPage(productRepository.findByIsActiveTrue(pageable));
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
        return mapProductPage(productRepository.searchProducts(keyword, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(String categorySlug, Pageable pageable) {
        Category category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée : " + categorySlug));
        
        return mapProductPage(productRepository.findByCategoryIdAndIsActiveTrue(category.getId(), pageable));
    }

    // ==========================================
    // 🛠️ GESTION PRODUITS (ADMIN)
    // ==========================================

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        // 1. Slug unique et sécurisé
        String baseSlug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? request.getSlug().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "")
                : (request.getName() != null ? request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "") : "produit");
        if (baseSlug.isEmpty()) baseSlug = "produit-" + System.currentTimeMillis();
        
        String finalSlug = baseSlug;
        int counter = 1;
        while (productRepository.findBySlug(finalSlug).isPresent()) {
            finalSlug = baseSlug + "-" + counter++;
        }

        // 2. SKU sécurisé
        String sku = (request.getSku() != null && !request.getSku().trim().isEmpty()) 
                ? request.getSku().trim() 
                : "PEOC-" + (System.currentTimeMillis() % 1000000);

        // 3. Marque & prix sécurisés
        String brand = (request.getBrand() != null && !request.getBrand().trim().isEmpty()) ? request.getBrand().trim() : "PEOC";
        BigDecimal basePrice = request.getBasePrice() != null ? request.getBasePrice() : BigDecimal.ZERO;

        int initialStock = request.getStockQty() != null ? request.getStockQty() : 0;
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            int variantTotal = request.getVariants().stream()
                    .mapToInt(v -> v.getStockQty() != null ? v.getStockQty() : 0)
                    .sum();
            if (variantTotal > 0) initialStock = variantTotal;
        }

        String rawName = request.getName() != null && !request.getName().trim().isEmpty() ? request.getName().trim() : "Nouveau Produit";
        String safeName = rawName.substring(0, Math.min(rawName.length(), 490));
        String safeSlug = finalSlug.substring(0, Math.min(finalSlug.length(), 490));

        Product product = Product.builder()
                .name(safeName)
                .sku(sku)
                .slug(safeSlug)
                .description(request.getDescription())
                .brand(brand)
                .category(category)
                .basePrice(basePrice)
                .costPrice(request.getCostPrice())
                .discountPrice(request.getDiscountPrice())
                .specifications(request.getSpecifications())
                .stockQty(initialStock) 
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
            int vIdx = 1;
            for (ProductRequest.VariantRequest vReq : request.getVariants()) {
                String vSku = (vReq.getSkuVariant() != null && !vReq.getSkuVariant().trim().isEmpty())
                        ? vReq.getSkuVariant().trim()
                        : sku + "-V" + (vIdx++);
                
                BigDecimal vPrice = vReq.getPrice() != null ? vReq.getPrice() : basePrice;

                variantRepository.save(ProductVariant.builder()
                        .product(savedProduct)
                        .skuVariant(vSku)
                        .price(vPrice)
                        .costPrice(vReq.getCostPrice() != null ? vReq.getCostPrice() : request.getCostPrice())
                        .stockQty(vReq.getStockQty() != null ? vReq.getStockQty() : 0)
                        .attributes(vReq.getAttributes())
                        .build());
            }
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
    product.setDiscountPrice(request.getDiscountPrice());
    product.setSpecifications(request.getSpecifications());
    
    if(request.getCategoryId() != null) {
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow();
        product.setCategory(category);
    }

    // 2. Gestion des images : synchronisation directe sur la collection de l'entité
    if (request.getImageUrls() != null) {
        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        } else {
            product.getImages().clear();
        }
        boolean isFirst = true;
        for (String url : request.getImageUrls()) {
            if (url != null && !url.trim().isEmpty()) {
                product.getImages().add(ProductImage.builder()
                        .product(product)
                        .url(url.trim())
                        .isPrimary(isFirst)
                        .build());
                isFirst = false;
            }
        }
    }

    // 3. Mise à jour des variantes : synchronisation propre sur l'entité sans désynchronisation JPA
    if (request.getVariants() != null) {
        // Détacher les variantes de cart_items pour éviter toute violation de clé étrangère
        try {
            cartItemRepository.detachVariantByProductId(id);
        } catch (Exception e) {
            log.warn("Notice: Impossible de détacher cart_items pour le produit {} : {}", id, e.getMessage());
        }

        if (product.getVariants() == null) {
            product.setVariants(new ArrayList<>());
        }

        // On indexe les variantes existantes par ID pour réutiliser les entités existantes sans changer leurs clés
        Map<Long, ProductVariant> existingMap = product.getVariants().stream()
                .filter(v -> v.getId() != null)
                .collect(Collectors.toMap(ProductVariant::getId, v -> v, (a, b) -> a));

        List<ProductVariant> updatedVariants = new ArrayList<>();
        int vIdx = 1;

        for (ProductRequest.VariantRequest vReq : request.getVariants()) {
            String vSku = (vReq.getSkuVariant() != null && !vReq.getSkuVariant().trim().isEmpty())
                    ? vReq.getSkuVariant().trim()
                    : product.getSku() + "-V" + (vIdx++);

            BigDecimal vPrice = vReq.getPrice() != null ? vReq.getPrice() : product.getBasePrice();
            BigDecimal vCost = vReq.getCostPrice() != null ? vReq.getCostPrice() : product.getCostPrice();
            int vStock = vReq.getStockQty() != null ? vReq.getStockQty() : 0;

            ProductVariant pv;
            if (vReq.getId() != null && existingMap.containsKey(vReq.getId())) {
                pv = existingMap.get(vReq.getId());
                pv.setSkuVariant(vSku);
                pv.setPrice(vPrice);
                pv.setCostPrice(vCost);
                pv.setStockQty(vStock);
                pv.setAttributes(vReq.getAttributes());
                pv.setIsActive(true);
            } else {
                pv = ProductVariant.builder()
                        .product(product)
                        .skuVariant(vSku)
                        .price(vPrice)
                        .costPrice(vCost)
                        .stockQty(vStock)
                        .attributes(vReq.getAttributes())
                        .isActive(true)
                        .build();
            }
            updatedVariants.add(pv);
        }

        product.getVariants().clear();
        product.getVariants().addAll(updatedVariants);

        int variantTotal = updatedVariants.stream()
                .mapToInt(v -> v.getStockQty() != null ? v.getStockQty() : 0)
                .sum();
        if (variantTotal > 0 || request.getStockQty() == null) {
            product.setStockQty(variantTotal);
        }
    } else if (request.getStockQty() != null) {
        product.setStockQty(request.getStockQty());
    }

    Product savedProduct = productRepository.saveAndFlush(product);
    return mapToProductResponse(savedProduct);
}

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProductsForAdmin(Pageable pageable) {
        return mapProductPage(productRepository.findAll(pageable));
    }

    @Override
    @Transactional
    public ProductResponse toggleProductStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id : " + id));
        product.setActive(!product.isActive());
        Product updated = productRepository.save(product);
        return mapToProductResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setActive(false); // Soft Delete
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void hardDeleteProduct(Long id) {
        // Suppression en cascade sécurisée de toutes les dépendances
        try { jdbcTemplate.update("DELETE FROM cart_items WHERE product_id = ?", id); } catch (Exception ignored) {}
        try { jdbcTemplate.update("DELETE FROM stock_movements WHERE product_id = ?", id); } catch (Exception ignored) {}
        try { jdbcTemplate.update("DELETE FROM wishlists WHERE product_id = ?", id); } catch (Exception ignored) {}
        try { jdbcTemplate.update("DELETE FROM reviews WHERE product_id = ?", id); } catch (Exception ignored) {}
        try { jdbcTemplate.update("DELETE FROM product_images WHERE product_id = ?", id); } catch (Exception ignored) {}
        try { jdbcTemplate.update("DELETE FROM product_variants WHERE product_id = ?", id); } catch (Exception ignored) {}
        try { jdbcTemplate.update("DELETE FROM order_items WHERE product_id = ?", id); } catch (Exception ignored) {}
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void purgeAllProducts() {
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            try { jdbcTemplate.execute("DELETE FROM cart_items"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("DELETE FROM stock_movements"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("DELETE FROM wishlists"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("DELETE FROM reviews"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("DELETE FROM product_images"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("DELETE FROM product_variants"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("DELETE FROM order_items"); } catch (Exception ignored) {}
            productRepository.unlinkAllCategories();
            productRepository.deleteAll();
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (Exception e) {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            throw new RuntimeException("Erreur purge : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void bulkDeleteProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            hardDeleteProduct(id);
        }
    }

    @Override
    @Transactional
    public void bulkToggleStatus(List<Long> ids, boolean active) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            productRepository.findById(id).ifPresent(p -> {
                p.setActive(active);
                productRepository.save(p);
            });
        }
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
    // 🔄 MAPPING TOOLS (OPTIMISÉ BULK / ANTI-N+1)
    // ==========================================

    private Page<ProductResponse> mapProductPage(Page<Product> productPage) {
        if (productPage.isEmpty()) {
            return productPage.map(this::mapToProductResponse);
        }
        List<Long> productIds = productPage.getContent().stream()
                .map(Product::getId)
                .toList();

        Map<Long, List<ProductVariant>> variantsByProduct = variantRepository.findByProductIdIn(productIds).stream()
                .filter(v -> v.getProduct() != null)
                .collect(Collectors.groupingBy(v -> v.getProduct().getId()));

        Map<Long, List<ProductImage>> imagesByProduct = imageRepository.findByProductIdInOrderByIdAsc(productIds).stream()
                .filter(img -> img.getProduct() != null)
                .collect(Collectors.groupingBy(img -> img.getProduct().getId()));

        return productPage.map(p -> mapToProductResponseWithPreloaded(
                p,
                variantsByProduct.getOrDefault(p.getId(), List.of()),
                imagesByProduct.getOrDefault(p.getId(), List.of())
        ));
    }

    private ProductResponse mapToProductResponseWithPreloaded(Product product, List<ProductVariant> variants, List<ProductImage> images) {
        int totalStock = 0;
        if (!variants.isEmpty()) {
            totalStock = variants.stream()
                    .mapToInt(v -> v.getStockQty() != null ? v.getStockQty() : 0)
                    .sum();
        } else {
            totalStock = product.getStockQty() != null ? product.getStockQty() : 0;
        }

        List<ProductImageResponse> imageResponses = images.stream()
                .map(img -> ProductImageResponse.builder()
                        .id(img.getId()).url(img.getUrl())
                        .altText(img.getAltText()).isPrimary(img.isPrimary())
                        .build())
                .collect(Collectors.toList());

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
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
                .basePrice(product.getBasePrice())
                .discountPrice(product.getDiscountPrice())
                .ratingAvg(product.getRatingAvg())
                .stockQty(totalStock)
                .isActive(product.isActive())
                .specifications(product.getSpecifications())
                .images(imageResponses)
                .variants(variantResponses)
                .build();
    }

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
    List<ProductImageResponse> imageResponses = imageRepository.findByProductIdOrderByIdAsc(product.getId()).stream()
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
            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
            .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
            .basePrice(product.getBasePrice())
            .discountPrice(product.getDiscountPrice())
            .ratingAvg(product.getRatingAvg())
            // ✨ ON ENVOIE LE TOTAL CALCULÉ ✨
            .stockQty(totalStock) 
            .isActive(product.isActive())
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