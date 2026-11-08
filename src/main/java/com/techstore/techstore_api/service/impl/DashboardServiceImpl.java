package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.dto.response.DashboardStatsResponse;
import com.techstore.techstore_api.dto.response.OrderItemResponse;
import com.techstore.techstore_api.dto.response.OrderResponse;
import com.techstore.techstore_api.model.Order;
import com.techstore.techstore_api.model.OrderStatus;
import com.techstore.techstore_api.model.Role;
import com.techstore.techstore_api.repository.OrderRepository;
import com.techstore.techstore_api.repository.ProductRepository;
import com.techstore.techstore_api.repository.ProductVariantRepository;
import com.techstore.techstore_api.repository.UserRepository;
import com.techstore.techstore_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        // 1. Calcul des KPI de base
        BigDecimal totalRevenue = orderRepository.sumTotalRevenue();
        long totalOrdersCount = orderRepository.count();
        long pendingOrdersCount = orderRepository.countByStatus(OrderStatus.EN_ATTENTE);
        long totalCustomersCount = userRepository.countByRoleAndIsDeletedFalseAndIsGuestFalse(Role.CLIENT);
        
        // Stock faible défini à moins de 5 unités (produits simples + variantes)
        long lowStockProductsCount = productRepository.countByStockQtyLessThanAndIsActiveTrue(5)
                + productVariantRepository.countByStockQtyLessThan(5);

        // 2. Ventes par catégorie (Graphique en camembert)
        List<Object[]> categorySalesList = orderRepository.getSalesBreakdownByCategory();
        List<DashboardStatsResponse.CategorySalesDTO> salesBreakdownByCategory = categorySalesList.stream()
                .map(obj -> DashboardStatsResponse.CategorySalesDTO.builder()
                        .categoryName((String) obj[0])
                        .totalSales((BigDecimal) obj[1])
                        .build())
                .collect(Collectors.toList());

        // 3. Ventes mensuelles (Graphique linéaire pour l'année en cours)
        int currentYear = LocalDate.now().getYear();
        List<Object[]> monthlySalesList = orderRepository.getMonthlySalesForYear(currentYear);
        List<DashboardStatsResponse.MonthlySalesDTO> monthlySales = monthlySalesList.stream()
                .map(obj -> DashboardStatsResponse.MonthlySalesDTO.builder()
                        .month((String) obj[0])
                        .totalSales((BigDecimal) obj[1])
                        .build())
                .collect(Collectors.toList());

        // 4. Les 5 produits les plus vendus (Top selling)
        Pageable topFive = PageRequest.of(0, 5);
        List<Object[]> topProductsList = orderRepository.getTopSellingProducts(topFive);
        List<DashboardStatsResponse.ProductSalesStatsDTO> topSellingProducts = topProductsList.stream()
                .map(obj -> DashboardStatsResponse.ProductSalesStatsDTO.builder()
                        .productId((Long) obj[0])
                        .productName((String) obj[1])
                        .sku((String) obj[2])
                        .quantitySold(obj[3] != null ? ((Number) obj[3]).longValue() : 0L)
                        .totalSales(obj[4] != null ? (BigDecimal) obj[4] : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        // 5. Les 5 dernières commandes récentes (Activités récentes)
        Pageable recentPageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Order> recentOrdersList = orderRepository.findAll(recentPageable).getContent();
        List<OrderResponse> recentOrders = recentOrdersList.stream()
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());

        // 6. Assemblage du résultat final
        return DashboardStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrdersCount(totalOrdersCount)
                .pendingOrdersCount(pendingOrdersCount)
                .totalCustomersCount(totalCustomersCount)
                .lowStockProductsCount(lowStockProductsCount)
                .salesBreakdownByCategory(salesBreakdownByCategory)
                .monthlySales(monthlySales)
                .topSellingProducts(topSellingProducts)
                .recentOrders(recentOrders)
                .build();
    }

    /**
     * Méthode utilitaire privée pour mapper une entité Order vers OrderResponse
     */
    private OrderResponse mapOrderToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> 
            OrderItemResponse.builder()
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subTotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                    .build()
        ).collect(Collectors.toList());

        String name = (order.getUser() != null) ? 
            (order.getUser().getFirstName() + " " + order.getUser().getLastName()) : order.getGuestName();
        String email = (order.getUser() != null) ? order.getUser().getEmail() : order.getGuestEmail();
        String phone = (order.getUser() != null) ? order.getUser().getPhone() : order.getGuestPhone();

        String addressLabel = (order.getShippingAddress() != null) ? 
                              order.getShippingAddress().getLabel() : "Adresse Saisie";

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber() != null ? order.getOrderNumber() : "TS-" + order.getId())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingCost(order.getShippingCost())
                .shippingType(order.getShippingType())
                .paymentMethod(order.getPaymentMethod())
                .trackingToken(order.getTrackingToken())
                .customerName(name)
                .customerEmail(email)
                .customerPhone(phone)
                .shippingAddressLabel(addressLabel)
                .latitude(order.getLatitude())
                .longitude(order.getLongitude())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardStatsResponse.LowStockItemDTO> getLowStockItems() {
        int threshold = 5;
        List<DashboardStatsResponse.LowStockItemDTO> lowStockItems = new java.util.ArrayList<>();
        
        // Fetch low stock products
        List<com.techstore.techstore_api.model.Product> lowProducts = productRepository.findByStockQtyLessThanAndIsActiveTrueOrderByStockQtyAsc(threshold);
        for (com.techstore.techstore_api.model.Product p : lowProducts) {
            // Only add if it doesn't have variants (otherwise the variant stock matters)
            if (p.getVariants() == null || p.getVariants().isEmpty()) {
                lowStockItems.add(DashboardStatsResponse.LowStockItemDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .sku(p.getSku())
                        .stockQty(p.getStockQty())
                        .type("PRODUCT")
                        .build());
            }
        }
        
        // Fetch low stock variants
        List<com.techstore.techstore_api.model.ProductVariant> lowVariants = productVariantRepository.findByStockQtyLessThanOrderByStockQtyAsc(threshold);
        for (com.techstore.techstore_api.model.ProductVariant v : lowVariants) {
            lowStockItems.add(DashboardStatsResponse.LowStockItemDTO.builder()
                    .id(v.getId())
                    .name(v.getProduct().getName() + " - " + (v.getAttributes() != null ? v.getAttributes() : v.getSkuVariant()))
                    .sku(v.getSkuVariant())
                    .stockQty(v.getStockQty())
                    .type("VARIANT")
                    .build());
        }
        
        // Sort by stock quantity ascending
        lowStockItems.sort((a, b) -> Integer.compare(a.getStockQty(), b.getStockQty()));
        
        return lowStockItems;
    }
}
