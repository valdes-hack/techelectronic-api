package com.techstore.techstore_api.service;

import com.techstore.techstore_api.dto.request.ReviewRequest;

public interface ReviewService {
    void addReview(ReviewRequest request, String userEmail);
}