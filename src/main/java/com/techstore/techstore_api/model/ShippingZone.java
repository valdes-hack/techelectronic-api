package com.techstore.techstore_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "shipping_zones")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ShippingZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String zoneName; // Ex: Akwa
    private String city;     // Ex: Douala
    private BigDecimal deliveryFee;
    private Integer estimatedDays;
    private boolean isActive = true;
}