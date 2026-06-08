package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.CategoryResponse;
import com.techstore.techstore_api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor

public class PublicCategoryController {

    private final CategoryService categoryService;

    // 1. CETTE MÃ‰THODE RÃ‰POND Ã€ : GET /api/v1/categories
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(
            ApiResponse.<List<CategoryResponse>>builder()
                .status("success")
                .code(200)
                .message("Liste des catÃ©gories rÃ©cupÃ©rÃ©e")
                .timestamp(LocalDateTime.now())
                .data(categories)
                .build()
        );
    }

    // 2. CETTE MÃ‰THODE RÃ‰POND Ã€ : GET /api/v1/categories/parents
    @GetMapping("/parents")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getParents() {
        List<CategoryResponse> categories = categoryService.getParentCategories();
        return ResponseEntity.ok(
            ApiResponse.<List<CategoryResponse>>builder()
                .status("success")
                .code(200)
                .message("CatÃ©gories parentes rÃ©cupÃ©rÃ©es")
                .timestamp(LocalDateTime.now())
                .data(categories)
                .build()
        );
    }
}
