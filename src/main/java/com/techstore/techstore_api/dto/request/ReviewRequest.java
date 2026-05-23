package com.techstore.techstore_api.dto.request;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String title;
    private String body;
}