package com.techstore.techstore_api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String iconUrl;
    private Long parentId; // On envoie juste l'ID du parent pour éviter la boucle
}