package com.techstore.techstore_api.controller;

import com.techstore.techstore_api.dto.request.ReviewRequest;
import com.techstore.techstore_api.dto.response.ApiResponse;
import com.techstore.techstore_api.dto.response.ReviewResponse;
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
                .message("Votre avis a été publié avec succès !")
                .timestamp(LocalDateTime.now())
                .build());
    }

    /**
     * AJOUTER UN AVIS EN TANT QU'INVITÉ (Public - Nécessite le trackingToken)
     */
    @PostMapping("/guest")
    public ResponseEntity<ApiResponse<Void>> addGuestReview(
            @RequestBody ReviewRequest request, 
            @RequestParam("token") String trackingToken) {
        
        reviewService.addGuestReview(request, trackingToken);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success")
                .code(200)
                .message("Votre avis invité a été publié avec succès !")
                .timestamp(LocalDateTime.now())
                .build());
    }

    /**
     * VOIR LES AVIS D'UN PRODUIT (Public)
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProductReviews(@PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewRepository.findByProductIdWithUser(productId).stream()
                .map(r -> ReviewResponse.builder()
                        .id(r.getId())
                        .productId(productId)
                        .rating(r.getRating())
                        .title(r.getTitle())
                        .body(r.getBody())
                        .verified(r.isVerified())
                        .authorName(displayName(r))
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.<List<ReviewResponse>>builder()
                .status("success")
                .code(200)
                .data(reviews)
                .build());
    }

    /**
     * VOIR MES PROPRES AVIS ET NOTES (Privé)
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.<List<ReviewResponse>>builder()
                    .status("error").code(401).message("Non autorisé").build());
        }
        List<ReviewResponse> reviews = reviewRepository.findByUserEmailWithProduct(principal.getName()).stream()
                .map(r -> ReviewResponse.builder()
                        .id(r.getId())
                        .productId(r.getProduct() != null ? r.getProduct().getId() : null)
                        .authorName(r.getProduct() != null ? r.getProduct().getName() : "Produit PEOC")
                        .rating(r.getRating())
                        .title(r.getTitle())
                        .body(r.getBody())
                        .verified(r.isVerified())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.<List<ReviewResponse>>builder()
                .status("success")
                .code(200)
                .data(reviews)
                .build());
    }

    /** "Valdes Hack" -> "Valdes H." ; on n'expose jamais l'email ni le nom complet. */
    private String displayName(Review r) {
        if (r.getUser() == null) return "Client";
        String first = r.getUser().getFirstName() != null ? r.getUser().getFirstName().trim() : "";
        String last = r.getUser().getLastName() != null ? r.getUser().getLastName().trim() : "";
        String name = first.isEmpty() ? "Client" : first;
        if (!last.isEmpty()) name += " " + last.charAt(0) + ".";
        return name;
    }
}
