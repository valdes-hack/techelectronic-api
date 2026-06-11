package com.techstore.techstore_api.service;

import com.techstore.techstore_api.dto.request.CategoryRequest;
import com.techstore.techstore_api.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {
    // On utilise CategoryRequest au lieu de Category
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    
    CategoryResponse getCategoryById(Long id);
    void deleteCategory(Long id);
    List<CategoryResponse> getAllCategories();
    List<CategoryResponse> getParentCategories();
    List<com.techstore.techstore_api.dto.response.CategoryGroupedResponse> getGroupedCategories();
}