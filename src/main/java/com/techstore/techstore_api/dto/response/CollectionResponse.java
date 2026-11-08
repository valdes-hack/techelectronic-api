package com.techstore.techstore_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CollectionResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private String collectionType;
    private Integer sortOrder;
    private Boolean isActive;

    /** Nombre de produits ACTIFS dans la collection (pour la vitrine et l'admin). */
    private Long productCount;

    private LocalDateTime createdAt;
}
