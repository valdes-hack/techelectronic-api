package com.techstore.techstore_api.service;

import com.techstore.techstore_api.dto.request.OrderRequest;
import com.techstore.techstore_api.dto.response.OrderResponse;
import com.techstore.techstore_api.model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request, String userEmail);
    List<OrderResponse> getMyOrders(String userEmail);
    OrderResponse getOrderDetails(Long orderId, String userEmail);
    List<OrderResponse> getAllOrdersForAdmin();
    OrderResponse getOrderByToken(String token);
OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
}