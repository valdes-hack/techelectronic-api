package com.techstore.techstore_api.dto.request;

import com.techstore.techstore_api.model.ShippingType;
import lombok.Data;

@Data
public class OrderRequest {
    // Infos Invité (Optionnel si connecté)
    private String guestName;
        private String guestLastName; // <--- AJOUTE CECI (Le Nom)

    private String guestEmail;
    private String guestPhone;

    // Livraison
    private ShippingType shippingType; // RETRAIT ou LIVRAISON
    private Long shippingZoneId;       // ID de la zone (Akwa, Bastos, etc.)
    private Long addressId;            // ID de l'adresse (si déjà enregistrée)
    
    // Géolocalisation (Google Maps)
    private Double latitude;
    private Double longitude;

    // Paiement
    private String paymentMethod;
    
    // Pour le panier provisoire (Guest Cart)
    private String sessionId; 
}