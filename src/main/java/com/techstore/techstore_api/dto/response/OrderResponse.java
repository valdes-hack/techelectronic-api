package com.techstore.techstore_api.dto.response;

import com.techstore.techstore_api.model.OrderStatus;
import com.techstore.techstore_api.model.ShippingType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private LocalDateTime createdAt;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal shippingCost;
    private ShippingType shippingType;
    private String paymentMethod;
    private String trackingToken;
    
    // Infos Client
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Localisation
    private String shippingAddressLabel;
    private Double latitude;
    private Double longitude;
    
    private List<OrderItemResponse> items;
}