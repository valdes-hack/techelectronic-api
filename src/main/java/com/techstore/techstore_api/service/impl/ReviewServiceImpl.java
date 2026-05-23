package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.dto.request.ReviewRequest;
import com.techstore.techstore_api.model.*;
import com.techstore.techstore_api.repository.*;
import com.techstore.techstore_api.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void addReview(ReviewRequest request, String userEmail) {
        // 1. Récupérer les objets
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        // 2. SÉCURITÉ : Vérifier si la commande appartient bien à l'utilisateur
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Action interdite : vous n'êtes pas l'auteur de cette commande.");
        }

        // 3. SÉCURITÉ : Vérifier si le produit était bien dans cette commande
        boolean hasPurchased = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(product.getId()));
        
        if (!hasPurchased) {
            throw new RuntimeException("Erreur : vous n'avez pas acheté ce produit dans cette commande.");
        }

        // 4. SÉCURITÉ : Un seul avis par produit et par commande
        if (reviewRepository.existsByUserIdAndOrderIdAndProductId(user.getId(), order.getId(), product.getId())) {
            throw new RuntimeException("Vous avez déjà laissé un avis pour ce produit concernant cet achat.");
        }

        // 5. ENREGISTREMENT DE L'AVIS
        Review review = Review.builder()
                .product(product)
                .user(user)
                .order(order)
                .rating(request.getRating())
                .title(request.getTitle())
                .body(request.getBody())
                .isVerified(true)
                .build();
        
        reviewRepository.save(review);

        // 6. ✨ APPEL DE TA PROCÉDURE SQL ✨
        // Recalcule la moyenne du produit automatiquement dans la table 'products'
        reviewRepository.updateProductAverageRating(product.getId());
    }
}