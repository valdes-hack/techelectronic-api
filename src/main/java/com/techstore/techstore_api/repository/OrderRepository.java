package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.Order;
import com.techstore.techstore_api.model.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Trouver une commande par son token de suivi
    java.util.Optional<Order> findByTrackingToken(String trackingToken);

    // --- REQUÊTES POUR LE DASHBOARD ---
    
    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != 'ANNULE'")
    BigDecimal sumTotalRevenue();

    @Query("SELECT oi.product.category.name, SUM(oi.quantity * oi.unitPrice) FROM OrderItem oi WHERE oi.order.status != 'ANNULE' GROUP BY oi.product.category.name")
    List<Object[]> getSalesBreakdownByCategory();

    @Query("SELECT FUNCTION('MONTHNAME', o.createdAt), SUM(o.totalAmount) FROM Order o WHERE o.status != 'ANNULE' AND FUNCTION('YEAR', o.createdAt) = :year GROUP BY FUNCTION('MONTH', o.createdAt), FUNCTION('MONTHNAME', o.createdAt) ORDER BY FUNCTION('MONTH', o.createdAt)")
    List<Object[]> getMonthlySalesForYear(@Param("year") int year);

    @Query("SELECT oi.product.id, oi.productName, oi.product.sku, SUM(oi.quantity), SUM(oi.quantity * oi.unitPrice) FROM OrderItem oi WHERE oi.order.status != 'ANNULE' GROUP BY oi.product.id, oi.productName, oi.product.sku ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getTopSellingProducts(Pageable pageable);
}