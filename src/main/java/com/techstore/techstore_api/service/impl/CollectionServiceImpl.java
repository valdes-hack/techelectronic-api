package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.dto.request.CollectionRequest;
import com.techstore.techstore_api.dto.response.CollectionResponse;
import com.techstore.techstore_api.dto.response.ProductResponse;
import com.techstore.techstore_api.model.Collection;
import com.techstore.techstore_api.model.Product;
import com.techstore.techstore_api.model.ProductCollection;
import com.techstore.techstore_api.repository.CollectionRepository;
import com.techstore.techstore_api.repository.ProductCollectionRepository;
import com.techstore.techstore_api.repository.ProductRepository;
import com.techstore.techstore_api.service.CollectionService;
import com.techstore.techstore_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final ProductCollectionRepository productCollectionRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    // ==========================================
    // 🔍 LECTURE PUBLIC
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<CollectionResponse> getActiveCollections() {
        return collectionRepository.findByIsActiveTrueOrderBySortOrderAscIdAsc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionResponse getCollectionBySlug(String slug) {
        Collection collection = collectionRepository.findBySlug(slug)
                .orElseThrow(() -> new com.techstore.techstore_api.exception.ResourceNotFoundException("Collection non trouvée : " + slug));
        return mapToResponse(collection);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCollection(String slug, Pageable pageable) {
        Collection collection = collectionRepository.findBySlug(slug)
                .orElseThrow(() -> new com.techstore.techstore_api.exception.ResourceNotFoundException("Collection non trouvée : " + slug));
        Page<Product> products = productCollectionRepository.findActiveProductsByCollection(collection.getId(), pageable);
        return productService.mapToResponsePage(products);
    }

    // ==========================================
    // 🔍 LECTURE ADMIN
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<CollectionResponse> getAllCollections() {
        return collectionRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionResponse getCollectionById(Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection non trouvée avec l'id : " + id));
        return mapToResponse(collection);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsForAdmin(Long collectionId, Pageable pageable) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection non trouvée avec l'id : " + collectionId));
        Page<Product> products = productCollectionRepository.findAllProductsByCollection(collection.getId(), pageable);
        return productService.mapToResponsePage(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionResponse> getCollectionsByProduct(Long productId) {
        return productCollectionRepository.findByProductId(productId).stream()
                .map(pc -> mapToResponse(pc.getCollection()))
                .toList();
    }

    // ==========================================
    // 🛠️ ÉCRITURE ADMIN
    // ==========================================

    @Override
    @Transactional
    public CollectionResponse createCollection(CollectionRequest request) {
        String name = requireName(request.getName());
        String slug = resolveUniqueSlug(request.getSlug(), name, null);

        Collection collection = Collection.builder()
                .name(name)
                .slug(slug)
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .bannerUrl(request.getBannerUrl())
                .collectionType(request.getCollectionType() != null ? request.getCollectionType() : "TYPE")
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        Collection saved = collectionRepository.save(collection);

        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            linkProducts(saved, request.getProductIds());
        }

        log.info("✅ [COLLECTION] Collection '{}' créée ({} produits liés)", saved.getName(),
                request.getProductIds() != null ? request.getProductIds().size() : 0);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CollectionResponse updateCollection(Long id, CollectionRequest request) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection non trouvée avec l'id : " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            collection.setName(request.getName().trim());
        }
        // Le slug ne change QUE s'il est explicitement fourni (stabilité des URLs / SEO).
        // Renommer une collection ne casse donc pas ses liens existants.
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = slugify(request.getSlug());
            if (!newSlug.isBlank() && !newSlug.equals(collection.getSlug())) {
                if (collectionRepository.existsBySlugAndIdNot(newSlug, id)) {
                    throw new RuntimeException("Le slug '" + newSlug + "' est déjà utilisé par une autre collection");
                }
                collection.setSlug(newSlug);
            }
        }

        if (request.getDescription() != null) collection.setDescription(request.getDescription());
        if (request.getIconUrl() != null) collection.setIconUrl(request.getIconUrl());
        if (request.getBannerUrl() != null) collection.setBannerUrl(request.getBannerUrl());
        if (request.getCollectionType() != null && !request.getCollectionType().isBlank()) {
            collection.setCollectionType(request.getCollectionType());
        }
        if (request.getIsActive() != null) collection.setIsActive(request.getIsActive());
        if (request.getSortOrder() != null) collection.setSortOrder(request.getSortOrder());

        Collection saved = collectionRepository.save(collection);

        // Si productIds est fourni (même vide), on REMPLACE toute la liste
        if (request.getProductIds() != null) {
            replaceProducts(saved, request.getProductIds());
        }

        log.info("✅ [COLLECTION] Collection '{}' mise à jour", saved.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCollection(Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection non trouvée avec l'id : " + id));
        // orphanRemoval = true + FK ON DELETE CASCADE : les liaisons disparaissent,
        // les PRODUITS ne sont jamais supprimés.
        collectionRepository.delete(collection);
        log.info("🗑️ [COLLECTION] Collection '{}' supprimée (produits conservés)", collection.getName());
    }

    @Override
    @Transactional
    public CollectionResponse addProductToCollection(Long collectionId, Long productId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection non trouvée avec l'id : " + collectionId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id : " + productId));

        if (productCollectionRepository.existsByCollectionIdAndProductId(collectionId, productId)) {
            return mapToResponse(collection); // Déjà lié : idempotent, pas d'erreur
        }

        // La nouvelle liaison prend la position max + 1
        int nextPosition = (int) productCollectionRepository.countByCollectionId(collectionId);
        productCollectionRepository.save(ProductCollection.builder()
                .collection(collection)
                .product(product)
                .sortOrder(nextPosition)
                .build());

        return mapToResponse(collection);
    }

    @Override
    @Transactional
    public void removeProductFromCollection(Long collectionId, Long productId) {
        productCollectionRepository.deleteByCollectionIdAndProductId(collectionId, productId);
    }

    @Override
    @Transactional
    public CollectionResponse setCollectionProducts(Long collectionId, List<Long> productIds) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection non trouvée avec l'id : " + collectionId));
        replaceProducts(collection, productIds != null ? productIds : new ArrayList<>());
        return mapToResponse(collection);
    }

    // ==========================================
    // 🔧 OUTILS PRIVÉS
    // ==========================================

    /** Remplace TOUTE la liste des produits d'une collection (diff intelligent). */
    private void replaceProducts(Collection collection, List<Long> productIds) {
        List<ProductCollection> current = productCollectionRepository.findByCollectionIdOrderBySortOrderAscIdAsc(collection.getId());

        // 1. Supprimer les liaisons qui ne sont plus voulues
        Set<Long> wanted = new HashSet<>(productIds);
        List<Long> toRemove = current.stream()
                .map(pc -> pc.getProduct().getId())
                .filter(pid -> !wanted.contains(pid))
                .toList();
        for (Long pid : toRemove) {
            productCollectionRepository.deleteByCollectionIdAndProductId(collection.getId(), pid);
        }

        // 2. Ajouter les nouvelles liaisons (en conservant la position des existantes)
        int position = 0;
        for (Long productId : productIds) {
            ProductCollection existing = productCollectionRepository
                    .findByCollectionIdAndProductId(collection.getId(), productId)
                    .orElse(null);
            if (existing != null) {
                existing.setSortOrder(position);
                productCollectionRepository.save(existing);
            } else {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id : " + productId));
                productCollectionRepository.save(ProductCollection.builder()
                        .collection(collection)
                        .product(product)
                        .sortOrder(position)
                        .build());
            }
            position++;
        }
    }

    /** Lie une liste de produits (ignore les déjà liés, vérifie l'existence). */
    private void linkProducts(Collection collection, List<Long> productIds) {
        int position = (int) productCollectionRepository.countByCollectionId(collection.getId());
        for (Long productId : productIds) {
            if (productCollectionRepository.existsByCollectionIdAndProductId(collection.getId(), productId)) {
                continue;
            }
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id : " + productId));
            productCollectionRepository.save(ProductCollection.builder()
                    .collection(collection)
                    .product(product)
                    .sortOrder(position++)
                    .build());
        }
    }

    private String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Le nom de la collection est obligatoire");
        }
        return name.trim();
    }

    /** Slug unique : depuis le slug fourni, sinon dérivé du nom ; suffixe -2, -3... si collision. */
    private String resolveUniqueSlug(String requestedSlug, String name, Long excludeId) {
        String base = (requestedSlug != null && !requestedSlug.isBlank())
                ? slugify(requestedSlug)
                : slugify(name);

        String candidate = base;
        int suffix = 2;
        boolean taken = (excludeId != null)
                ? collectionRepository.existsBySlugAndIdNot(candidate, excludeId)
                : collectionRepository.existsBySlug(candidate);
        while (taken) {
            candidate = base + "-" + suffix++;
            taken = (excludeId != null)
                    ? collectionRepository.existsBySlugAndIdNot(candidate, excludeId)
                    : collectionRepository.existsBySlug(candidate);
        }
        return candidate;
    }

    /** Slugify robuste : minuscules, sans accents, tirets. "Élevage porcin" -> "elevage-porcin" */
    private String slugify(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input.toLowerCase(Locale.FRENCH), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized
                .replaceAll("[^a-z0-9\\u00C0-\\u024F]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    private CollectionResponse mapToResponse(Collection collection) {
        long productCount = collectionRepository.countActiveProducts(collection.getId());
        return CollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .slug(collection.getSlug())
                .description(collection.getDescription())
                .iconUrl(collection.getIconUrl())
                .bannerUrl(collection.getBannerUrl())
                .collectionType(collection.getCollectionType())
                .sortOrder(collection.getSortOrder())
                .isActive(collection.isActive())
                .productCount(productCount)
                .createdAt(collection.getCreatedAt())
                .build();
    }
}
