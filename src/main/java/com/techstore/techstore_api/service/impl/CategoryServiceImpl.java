package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.dto.request.CategoryRequest;
import com.techstore.techstore_api.dto.response.CategoryResponse;
import com.techstore.techstore_api.model.Category;
import com.techstore.techstore_api.repository.CategoryRepository;
import com.techstore.techstore_api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import com.techstore.techstore_api.dto.response.CategoryGroupedResponse;
import com.techstore.techstore_api.dto.response.ProductResponse;
import com.techstore.techstore_api.repository.ProductRepository;
import com.techstore.techstore_api.service.ProductService;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;

    /**
     * CRÉER UNE CATÉGORIE
     */
       @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID : " + id));
        return mapToResponse(category);
    }
    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .iconUrl(request.getIconUrl())
                .isActive(true)
                .build();

        // Gestion de la catégorie parente
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Catégorie parente non trouvée"));
            category.setParent(parent);
        }

        return mapToResponse(categoryRepository.save(category));
    }

    /**
     * MODIFIER UNE CATÉGORIE
     */
    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'id : " + id));
        
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        
        // On ne met à jour l'icône que si une nouvelle URL est fournie
        if (request.getIconUrl() != null) {
            category.setIconUrl(request.getIconUrl());
        }

        // Mise à jour du parent
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Nouveau parent non trouvé"));
            category.setParent(parent);
        } else {
            category.setParent(null); // Devient une catégorie racine
        }

        return mapToResponse(categoryRepository.save(category));
    }

    /**
     * SUPPRIMER (SOFT DELETE)
     */
    

    /**
     * RÉCUPÉRER PAR ID
     */
    

    /**
     * LISTER TOUTES LES CATÉGORIES
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * LISTER LES CATÉGORIES PARENTES (RACINES)
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getParentCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * MÉTHODE PRIVÉE DE MAPPING (ENTITÉ -> DTO)
     * Indispensable pour éviter les boucles infinies dans Swagger/JSON
     */
    private CategoryResponse mapToResponse(Category cat) {
        return CategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .slug(cat.getSlug())
                .iconUrl(cat.getIconUrl())
                .parentId(cat.getParent() != null ? cat.getParent().getId() : null)
                .build();
    }
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
        
        // On ne supprime pas la ligne SQL, on la désactive
        category.setActive(false);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryGroupedResponse> getGroupedCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(cat -> {
                    // Fetch top 4 active products for this category
                    List<com.techstore.techstore_api.model.Product> products = productRepository
                            .findByCategoryIdAndIsActiveTrue(cat.getId(), PageRequest.of(0, 4))
                            .getContent();
                            
                    // We need a mapToResponse method from product to ProductResponse
                    // We can reuse the one from ProductService or implement a simple mapping if needed
                    // For now we assume we map it here or fetch directly
                    // It's safer to use a custom mapper or call a service method, 
                    // but we will do a simple mapping here to avoid circular dependencies.
                    // Wait, ProductServiceImpl has a mapToResponse. We can use it if we don't have circular dependency.
                    // But to be safe, let's just fetch the page using productService:
                    List<ProductResponse> productResponses = productService
                            .getProductsByCategory(cat.getSlug(), PageRequest.of(0, 4))
                            .getContent();

                    return CategoryGroupedResponse.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .slug(cat.getSlug())
                            .iconUrl(cat.getIconUrl())
                            .products(productResponses)
                            .build();
                })
                // Only return categories that actually have products
                .filter(res -> !res.getProducts().isEmpty())
                .collect(Collectors.toList());
    }
}