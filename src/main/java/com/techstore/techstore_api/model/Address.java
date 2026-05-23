package com.techstore.techstore_api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(exclude = "user") // Empêche Lombok de boucler
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference // INDISPENSABLE : Coupe la boucle Address -> User -> Address
    private User user;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
@Builder.Default
private String country = "Cameroun";
    @Column(nullable = false)
    private String phone;

    @Column(name = "is_default")
    private boolean isDefault = false;

    private Double latitude;
    private Double longitude;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}