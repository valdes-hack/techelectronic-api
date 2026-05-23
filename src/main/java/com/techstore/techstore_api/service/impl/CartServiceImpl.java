package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.dto.request.CartItemRequest;
import com.techstore.techstore_api.dto.response.CartItemResponse;
import com.techstore.techstore_api.dto.response.CartResponse;
import com.techstore.techstore_api.model.*;
import com.techstore.techstore_api.repository.*;
import com.techstore.techstore_api.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    /**
     * AJOUTER AU PANIER (Visiteur ou Connecté)
     */
    @Override
    @Transactional
    public CartResponse addToCart(CartItemRequest request, String identifier, boolean isUser) {
        // 1. Récupérer ou créer le panier
        Cart cart = getOrCreateCart(identifier, isUser);

        // 2. Récupérer le produit
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        ProductVariant variant = null;
        int availableStock = product.getStockQty();
        BigDecimal price = (product.getDiscountPrice() != null) ? product.getDiscountPrice() : product.getBasePrice();

        // 3. Gestion de la variante
        if (request.getVariantId() != null) {
            variant = variantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variante non trouvée"));
            availableStock = variant.getStockQty();
            price = variant.getPrice();
        }

        // 4. VÉRIFICATION DU STOCK
        if (request.getQuantity() > availableStock) {
            throw new RuntimeException("Stock insuffisant. Il ne reste que " + availableStock + " articles.");
        }

        // 5. Vérifier si l'article existe déjà dans ce panier
        final Long reqVariantId = request.getVariantId();
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()) &&
                        Objects.equals(item.getVariant() != null ? item.getVariant().getId() : null, reqVariantId))
                .findFirst().orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > availableStock) throw new RuntimeException("Action impossible : stock total dépassé.");
            existingItem.setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart).product(product).variant(variant)
                    .quantity(request.getQuantity()).unitPrice(price).build();
            cart.getItems().add(newItem);
        }

        return mapToResponse(cartRepository.save(cart));
    }

    /**
     * VOIR LE PANIER
     */
    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart(String identifier, boolean isUser) {
        Cart cart;
        if (isUser) {
            User user = userRepository.findByEmail(identifier).orElseThrow();
            cart = cartRepository.findByUserId(user.getId()).orElse(null);
        } else {
            cart = cartRepository.findBySessionId(identifier).orElse(null);
        }

        if (cart == null) {
            return CartResponse.builder().items(new ArrayList<>()).totalAmount(BigDecimal.ZERO).totalItems(0).build();
        }
        return mapToResponse(cart);
    }

    /**
     * MODIFIER QUANTITÉ
     */
    @Override
    @Transactional
    public CartResponse updateQuantity(Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        
        int availableStock = (item.getVariant() != null) ? item.getVariant().getStockQty() : item.getProduct().getStockQty();
        if (quantity > availableStock) {
            throw new RuntimeException("Stock insuffisant (" + availableStock + " max).");
        }

        item.setQuantity(quantity);
        return mapToResponse(item.getCart());
    }

    /**
     * RETIRER UN ARTICLE
     */
    @Override
    @Transactional
    public CartResponse removeFromCart(Long itemId, String identifier, boolean isUser) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        Cart cart = item.getCart();
        cart.getItems().remove(item);
        return mapToResponse(cartRepository.save(cart));
    }

    /**
     * VIDER LE PANIER
     */
    @Override
    @Transactional
    public void clearCart(String identifier, boolean isUser) {
        if (isUser) {
            User user = userRepository.findByEmail(identifier).orElseThrow();
            cartRepository.findByUserId(user.getId()).ifPresent(cartRepository::delete);
        } else {
            cartRepository.findBySessionId(identifier).ifPresent(cartRepository::delete);
        }
    }

    /**
     * FUSIONNER LE PANIER (Visiteur -> Client)
     */
    @Override
    @Transactional
    public CartResponse mergeCart(String sessionId, String userEmail) {
        Cart guestCart = cartRepository.findBySessionId(sessionId).orElse(null);
        if (guestCart == null || guestCart.getItems().isEmpty()) return getMyCart(userEmail, true);

        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Cart userCart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).items(new ArrayList<>()).build()));

        for (CartItem guestItem : guestCart.getItems()) {
            final Long pId = guestItem.getProduct().getId();
            final Long vId = (guestItem.getVariant() != null) ? guestItem.getVariant().getId() : null;

            CartItem existing = userCart.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(pId) && Objects.equals(i.getVariant() != null ? i.getVariant().getId() : null, vId))
                    .findFirst().orElse(null);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
            } else {
                userCart.getItems().add(CartItem.builder()
                        .cart(userCart).product(guestItem.getProduct()).variant(guestItem.getVariant())
                        .quantity(guestItem.getQuantity()).unitPrice(guestItem.getUnitPrice()).build());
            }
        }
        cartRepository.delete(guestCart);
        return mapToResponse(cartRepository.save(userCart));
    }

    /**
     * MÉTHODE PRIVÉE : RÉCUPÉRER OU CRÉER LE PANIER
     */
    private Cart getOrCreateCart(String identifier, boolean isUser) {
        if (isUser) {
            User user = userRepository.findByEmail(identifier).orElseThrow();
            return cartRepository.findByUserId(user.getId())
                    .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
        } else {
            return cartRepository.findBySessionId(identifier)
                    .orElseGet(() -> cartRepository.save(Cart.builder().sessionId(identifier).build()));
        }
    }

    /**
     * MÉTHODE PRIVÉE : MAPPING DTO
     */
    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream().map(item -> {
            BigDecimal subTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            
            // Récupération de l'image principale pour React
            String imageUrl = (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) 
                    ? item.getProduct().getImages().get(0).getUrl() : "";

            return CartItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .variantAttributes(item.getVariant() != null ? item.getVariant().getAttributes() : "Standard")
                    .imageUrl(imageUrl)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subTotal(subTotal)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(itemResponses)
                .totalAmount(total)
                .totalItems(itemResponses.size())
                .build();
    }
}