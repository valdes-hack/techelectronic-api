package com.techstore.techstore_api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariantRequest {
    private Long id;
    private String skuVariant; // Ex: IPH16-PRO-BLK-256
    private BigDecimal price;   // Prix de vente de cette version
    private BigDecimal costPrice; // Prix d'achat
    private Integer stockQty;
    private String attributes; // JSON ✨ Ex: {"Couleur": "Noir", "Capacité": "256Go"}
}