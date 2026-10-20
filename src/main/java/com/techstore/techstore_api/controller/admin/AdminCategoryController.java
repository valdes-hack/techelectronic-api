package com.techstore.techstore_api.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.techstore_api.dto.request.CategoryRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.CategoryResponse;
import com.techstore.techstore_api.service.CategoryService;
import com.techstore.techstore_api.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.base-url:}")
    private String baseUrl;

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
     * 3. CRÉER UNE CATÉGORIE (JSON OU MULTIPART)
     */
    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<ApiResponse<CategoryResponse>> createJson(@RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(201).body(ApiResponse.<CategoryResponse>builder()
                .status("success").code(201).message("Catégorie créée").data(response).build());
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<CategoryResponse>> createMultipart(
            @RequestPart("category") String categoryJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        
        CategoryRequest request = objectMapper.readValue(categoryJson, CategoryRequest.class);
        if (file != null && !file.isEmpty()) {
            String fileUrlOrName = fileStorageService.storeFile(file);
            if (fileUrlOrName.startsWith("http://") || fileUrlOrName.startsWith("https://")) {
                request.setIconUrl(fileUrlOrName);
            } else {
                request.setIconUrl(baseUrl + "/uploads/categories/" + fileUrlOrName);
            }
        }

        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(201).body(ApiResponse.<CategoryResponse>builder()
                .status("success").code(201).message("Catégorie créée").data(response).build());
    }

    /**
     * 4. MODIFIER UNE CATÉGORIE (JSON OU MULTIPART)
     */
    @PutMapping(value = "/{id}", consumes = {"application/json"})
    public ResponseEntity<ApiResponse<CategoryResponse>> updateJson(
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
                .status("success").code(200).message("Catégorie mise à jour").data(response).build());
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<CategoryResponse>> updateMultipart(
            @PathVariable Long id,
            @RequestPart("category") String categoryJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        
        CategoryRequest request = objectMapper.readValue(categoryJson, CategoryRequest.class);
        if (file != null && !file.isEmpty()) {
            String fileUrlOrName = fileStorageService.storeFile(file);
            if (fileUrlOrName.startsWith("http://") || fileUrlOrName.startsWith("https://")) {
                request.setIconUrl(fileUrlOrName);
            } else {
                request.setIconUrl(baseUrl + "/uploads/categories/" + fileUrlOrName);
            }
        }

        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
                .status("success").code(200).message("Catégorie mise à jour").data(response).build());
    }

    /**
     * 5. SUPPRIMER UNE CATÉGORIE
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Catégorie supprimée avec succès").build());
    }

    /**
     * 6. VIDER TOUTES LES CATÉGORIES (PURGE)
     */
    @DeleteMapping("/purge-all")
    public ResponseEntity<ApiResponse<Void>> purgeAll() {
        categoryService.purgeAllCategories();
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success").code(200).message("Toutes les catégories ont été supprimées avec succès").build());
    }
}