package com.techstore.techstore_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private Integer loyaltyPoints;
    private boolean isVerified;
    
    // --- AJOUTE CETTE LIGNE ICI ---
    private String profilePictureUrl; 

    private LocalDateTime createdAt;
}