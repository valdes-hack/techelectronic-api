package com.techstore.techstore_api.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VariantRequest {
    private String skuVariant; // Ex: IPH16-PRO-BLK-256
    private BigDecimal price;   // Prix de vente de cette version
    private BigDecimal costPrice; // Prix d'achat
    private Integer stockQty;
    private String attributes; // JSON ✨ Ex: {"Couleur": "Noir", "Capacité": "256Go"}
}