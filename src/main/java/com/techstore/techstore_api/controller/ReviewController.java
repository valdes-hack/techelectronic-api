package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.request.ReviewRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.model.Review;
import com.techstore.techstore_api.repository.ReviewRepository;
import com.techstore.techstore_api.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    /**
     * AJOUTER UN AVIS (PrivÃ© - NÃ©cessite d'Ãªtre connectÃ©)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addReview(@RequestBody ReviewRequest request, Principal principal) {
        reviewService.addReview(request, principal.getName());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success")
                .code(200)
                .message("Votre avis a Ã©tÃ© publiÃ© avec succÃ¨s !")
                .timestamp(LocalDateTime.now())
                .build());
    }

    /**
     * VOIR LES AVIS D'UN PRODUIT (Public)
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<Review>>> getProductReviews(@PathVariable Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        return ResponseEntity.ok(ApiResponse.<List<Review>>builder()
                .status("success")
                .code(200)
                .data(reviews)
                .build());
    }
}
