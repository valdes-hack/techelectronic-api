package com.techstore.techstore_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(exclude = {"password", "addresses"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore // Sécurité : Cache le mot de passe
    @Column(name = "password_hash")
    private String password;

    private String firstName;
    private String lastName;
    private String phone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CLIENT;

    private String oauthProvider;

    @Builder.Default
    private Integer loyaltyPoints = 0;

    @Builder.Default
    private boolean isVerified = false;

    @Builder.Default
    private boolean isDeleted = false;

    // --- CHAMP CRUCIAL POUR LA COMPILATION ---
    @Builder.Default
    private boolean isGuest = false; 

    private String profilePictureUrl; 

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}