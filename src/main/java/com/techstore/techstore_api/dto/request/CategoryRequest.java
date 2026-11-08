package com.techstore.techstore_api.dto.request;

import lombok.Data;

@Data
public class CategoryRequest {
    private String name;
    private String slug;
    private String iconUrl;
    private Long parentId;
}