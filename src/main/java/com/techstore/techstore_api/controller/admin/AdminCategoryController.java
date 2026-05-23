package com.techstore.techstore_api.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.techstore_api.dto.request.CategoryRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.CategoryResponse;
import com.techstore.techstore_api.service.CategoryService;
import com.techstore.techstore_api.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    /**
     * 1. LISTER TOUTES LES CATÉGORIES (ADMIN)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.<List<CategoryResponse>>builder()
                .status("success").code(200).data(categories).build());
    }

    /**
     * 2. VOIR LE DÉTAIL D'UNE CATÉGORIE
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
                .status("success").code(200).data(response).build());
    }

    /**
     * 3. CRÉER UNE CATÉGORIE (AVEC ICÔNE)
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @RequestPart("category") String categoryJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        
        CategoryRequest request = objectMapper.readValue(categoryJson, CategoryRequest.class);
        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.storeFile(file);
            request.setIconUrl("http://localhost:8080/uploads/categories/" + fileName);
        }

        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(201).body(ApiResponse.<CategoryResponse>builder()
                .status("success").code(201).message("Catégorie créée").data(response).build());
    }

    /**
     * 4. MODIFIER UNE CATÉGORIE
     */
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @RequestPart("category") String categoryJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        
        CategoryRequest request = objectMapper.readValue(categoryJson, CategoryRequest.class);
        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.storeFile(file);
            request.setIconUrl("http://localhost:8080/uploads/categories/" + fileName);
        }

        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
                .status("success").code(200).message("Catégorie mise à jour").data(response).build());
    }

    /**
     * 5. SUPPRIMER (SOFT DELETE)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Catégorie désactivée avec succès").build());
    }
}