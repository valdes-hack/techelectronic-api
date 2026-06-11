package com.techstore.techstore_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryGroupedResponse {
    private Long id;
    private String name;
    private String slug;
    private String iconUrl;
    private List<ProductResponse> products;
}
