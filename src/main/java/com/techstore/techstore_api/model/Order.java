package com.techstore.techstore_api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(exclude = "items")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // L'utilisateur est optionnel (pour le Guest Checkout)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // Champs pour les clients non connectés (Invités)
    private String guestName;
    private String guestEmail;
    private String guestPhone;

    // Informations de livraison
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", nullable = true)
    private Address shippingAddress;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ShippingType shippingType = ShippingType.RETRAIT;

    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    private BigDecimal totalAmount;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Géolocalisation (Google Maps)
    private Double latitude;
    private Double longitude;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.EN_ATTENTE;

    @Builder.Default
    private String paymentStatus = "PENDING";
    
    private String paymentMethod;

    // Token unique pour le suivi sans compte
    private String trackingToken;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private String orderNumber; // Matricule de la commande (ex: TS-2025-001)
}