package com.techstore.techstore_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRequest {

    private String name;

    /** Optionnel : généré automatiquement depuis le nom si absent. */
    private String slug;

    private String description;

    /** Emoji ("🐣") ou URL d'image. */
    private String iconUrl;

    /** TYPE, USAGE ou PROMO (libre). */
    private String collectionType;

    private Integer sortOrder;

    private Boolean isActive;

    /**
     * Optionnel : liste des IDs produits à lier d'un coup.
     * - À la création : lie ces produits.
     * - À la mise à jour : si fourni (même vide []), REMPLACE toute la liste des produits de la collection.
     */
    private List<Long> productIds;
}
