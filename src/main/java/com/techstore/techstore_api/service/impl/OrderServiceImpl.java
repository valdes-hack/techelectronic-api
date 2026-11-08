package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.dto.request.OrderRequest;
import com.techstore.techstore_api.dto.response.OrderResponse;
import com.techstore.techstore_api.dto.response.OrderItemResponse;
import com.techstore.techstore_api.model.*;
import com.techstore.techstore_api.repository.*;
import com.techstore.techstore_api.service.CartService;
import com.techstore.techstore_api.service.EmailService;
import com.techstore.techstore_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartService cartService;
    private final ShippingZoneRepository shippingZoneRepository;
    private final AdminNotificationRepository adminNotificationRepository;

    /**
     * 1. CRÉATION DE LA COMMANDE (Logique Hybride : Client & Invité)
     */
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request, String userEmail) {
        // --- ÉTAPE 1 : IDENTIFICATION (Trouver ou Créer l'utilisateur) ---
        User user;
        if (userEmail != null) {
            // Cas 1 : L'utilisateur est connecté
            user = userRepository.findByEmail(userEmail).orElseThrow();
        } else {
            // Cas 2 : C'est un invité. On vérifie si l'email existe déjà pour éviter l'erreur 409
            user = userRepository.findByEmail(request.getGuestEmail()).orElse(null);
            
            if (user == null) {
                // Si l'email n'existe pas, on crée le profil provisoire (isGuest = true)
                user = User.builder()
                        .firstName(request.getGuestName())
                        .lastName(request.getGuestLastName() != null ? request.getGuestLastName() : "Client")
                        .email(request.getGuestEmail())
                        .phone(request.getGuestPhone())
                        .role(Role.CLIENT)
                        .isGuest(true)
                        .isVerified(true)
                        .build();
                user = userRepository.save(user);
            }
        }

        // --- ÉTAPE 2 : GESTION DE L'ADRESSE (Remplissage de TOUS les champs NOT NULL) ---
        Address shippingAddress = Address.builder()
                .user(user)
                .label("Livraison du " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")))
                .street("Saisie via Tunnel d'achat")
                .city("Douala")
                .region("Littoral")
                .country("Cameroun")
                .phone(request.getGuestPhone() != null ? request.getGuestPhone() : user.getPhone())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        shippingAddress = addressRepository.save(shippingAddress);

        // --- ÉTAPE 3 : RÉCUPÉRATION DU PANIER ---
        Cart cart = null;
        if (userEmail != null && user != null) {
            cart = cartRepository.findByUserId(user.getId()).orElse(null);
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                if (request.getSessionId() != null && !request.getSessionId().trim().isEmpty()) {
                    cart = cartRepository.findBySessionId(request.getSessionId()).orElse(null);
                }
            }
        } else if (request.getSessionId() != null && !request.getSessionId().trim().isEmpty()) {
            cart = cartRepository.findBySessionId(request.getSessionId()).orElse(null);
        }

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Impossible de commander : votre panier est vide");
        }

        // --- ÉTAPE 4 : LIVRAISON & FACTURATION ---
        BigDecimal shippingFee = BigDecimal.ZERO;
        if (request.getShippingType() == ShippingType.LIVRAISON && request.getShippingZoneId() != null) {
            ShippingZone zone = shippingZoneRepository.findById(request.getShippingZoneId()).orElse(null);
            if (zone != null) shippingFee = zone.getDeliveryFee();
        }

        // --- ÉTAPE 5 : CRÉATION DE LA COMMANDE ---
        Order order = Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .guestName(request.getGuestName())
                .guestEmail(request.getGuestEmail())
                .guestPhone(request.getGuestPhone())
                .shippingType(request.getShippingType())
                .shippingCost(shippingFee)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.EN_ATTENTE)
                .trackingToken(java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .orderNumber("CMD-" + System.currentTimeMillis() % 1000000)
                .totalAmount(calculateTotal(cart).add(shippingFee))
                .createdAt(LocalDateTime.now())
                .build();

        // --- ÉTAPE 6 : ITEMS & DÉDUCTION DU STOCK (Snapshot) ---
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            // Déduction du stock fiable via JPA avec fallback
            try {
                if (item.getVariant() != null && item.getVariant().getId() != null) {
                    productVariantRepository.findById(item.getVariant().getId()).ifPresent(v -> {
                        int current = v.getStockQty() != null ? v.getStockQty() : 0;
                        v.setStockQty(Math.max(0, current - item.getQuantity()));
                        productVariantRepository.save(v);
                    });
                } else if (item.getProduct() != null && item.getProduct().getId() != null) {
                    productRepository.findById(item.getProduct().getId()).ifPresent(p -> {
                        int current = p.getStockQty() != null ? p.getStockQty() : 0;
                        p.setStockQty(Math.max(0, current - item.getQuantity()));
                        productRepository.save(p);
                    });
                }
            } catch (Exception stockEx) {
                System.err.println("Avertissement déduction stock: " + stockEx.getMessage());
            }

            orderItems.add(OrderItem.builder()
                    .order(order)
                    .product(item.getProduct())
                    .variant(item.getVariant())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .productName(item.getProduct() != null ? item.getProduct().getName() : "Produit")
                    .build());
        }
        order.setItems(orderItems);

        // --- ÉTAPE 7 : SAUVEGARDE ET NETTOYAGE ---
        Order savedOrder = orderRepository.saveAndFlush(order);
        
        // On vide le panier (pour utilisateur et pour sessionId)
        try {
            if (userEmail != null) {
                cartService.clearCart(userEmail, true);
            }
            if (request.getSessionId() != null && !request.getSessionId().trim().isEmpty()) {
                cartService.clearCart(request.getSessionId(), false);
            }
        } catch (Exception e) {
            System.err.println("Avertissement nettoyage panier: " + e.getMessage());
        }

        // --- ÉTAPE 8 : NOTIFICATIONS EMAILS ---
        try { 
            emailService.sendOrderConfirmation(savedOrder); 
            emailService.sendAdminAlert(savedOrder);
        } catch (Exception e) {
            System.err.println("Alerte : Échec envoi mail : " + e.getMessage());
        }
        // CRÉATION DE LA NOTIFICATION ADMIN ✨
    // Construction d'un résumé rapide des articles pour la notification
    String resumeArticles = savedOrder.getItems().stream()
            .map(i -> i.getQuantity() + "x " + i.getProductName())
            .collect(Collectors.joining(", "));

    adminNotificationRepository.save(AdminNotification.builder()
            .title("Nouvelle vente : " + savedOrder.getTotalAmount() + " FCFA")
            .message("Client : " + (savedOrder.getUser() != null ? savedOrder.getUser().getFirstName() : savedOrder.getGuestName()) + 
                     " | Articles : " + resumeArticles + 
                     " | Livraison : " + savedOrder.getShippingType())
            .type("NEW_ORDER")
            .build());

        return mapToResponse(savedOrder);
    }

    /**
     * 2. RÉCUPÉRER MES COMMANDES (Client)
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return new java.util.ArrayList<>();
        }
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * 3. DÉTAIL D'UNE COMMANDE
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Accès refusé à cette commande");
        }
        return mapToResponse(order);
    }

    /**
     * 4. LISTER TOUTES LES COMMANDES (Admin)
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersForAdmin() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 5. CHANGER LE STATUT (Admin)
     */
    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        
        order.setStatus(status);
        Order updated = orderRepository.save(order);

        // Notifier l'admin du changement de statut
        try {
            adminNotificationRepository.save(AdminNotification.builder()
                    .title("Statut commande mis à jour")
                    .message("Commande #" + (updated.getOrderNumber() != null ? updated.getOrderNumber() : updated.getId()) + " passée au statut : " + status)
                    .type("ORDER_STATUS")
                    .build());
        } catch (Exception ignored) {}

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    @Transactional
    public void bulkDeleteOrders(List<Long> orderIds) {
        if (orderIds != null && !orderIds.isEmpty()) {
            orderRepository.deleteAllById(orderIds);
        }
    }

    /**
     * CALCUL DU TOTAL DU PANIER
     */
    private BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * MÉTHODE DE MAPPING (ENTITÉ -> DTO)
     */
    private OrderResponse mapToResponse(Order order) {
    // 1. Transformation des articles
    List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
        return OrderItemResponse.builder()
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subTotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .build();
    }).collect(Collectors.toList());

    // 2. Extraction des infos client (Hybride)
    String name = (order.getUser() != null) ? 
        (order.getUser().getFirstName() + " " + order.getUser().getLastName()) : order.getGuestName();
    String email = (order.getUser() != null) ? order.getUser().getEmail() : order.getGuestEmail();
    String phone = (order.getUser() != null) ? order.getUser().getPhone() : order.getGuestPhone();

    // 3. Label de l'adresse
    String addressLabel = (order.getShippingAddress() != null) ? 
                          order.getShippingAddress().getLabel() : "Adresse Saisie";

    // 4. Construction de la réponse complète ✨
    return OrderResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber() != null ? order.getOrderNumber() : "TS-" + order.getId())
            .createdAt(order.getCreatedAt())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .shippingCost(order.getShippingCost())
            .shippingType(order.getShippingType())
            .paymentMethod(order.getPaymentMethod())
            .trackingToken(order.getTrackingToken())
            .customerName(name)
            .customerEmail(email)
            .customerPhone(phone)
            .shippingAddressLabel(addressLabel)
            .latitude(order.getLatitude())
            .longitude(order.getLongitude())
            .items(itemResponses)
            .build();
}
@Override
@Transactional(readOnly = true)
public OrderResponse getOrderByToken(String token) {
    Order order = orderRepository.findByTrackingToken(token)
            .orElseThrow(() -> new RuntimeException("Code de suivi invalide ou expiré."));
    
    return mapToResponse(order);
}
}