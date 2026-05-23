package com.techstore.techstore_api.service;

import com.techstore.techstore_api.dto.request.StockRequest;

public interface StockService {
    void supplyProduct(StockRequest request);
}