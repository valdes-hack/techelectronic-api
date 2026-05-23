package com.techstore.techstore_api.service;

import com.techstore.techstore_api.model.Order;

public interface EmailService {
    void sendOrderConfirmation(Order order);
    void sendAdminAlert(Order order);
}