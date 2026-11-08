package com.techstore.techstore_api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionRequest {

    private String name;

    /** Optionnel : généré automatiquement depuis le nom si absent. */
    private String slug;

    private String description;

    /** Emoji ("🐣") ou URL d'icône. */
    private String iconUrl;

    /** URL de la photo d'arrière-plan de la collection. */
    private String bannerUrl;

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
