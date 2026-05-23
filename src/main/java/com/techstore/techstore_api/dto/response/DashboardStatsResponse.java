package com.techstore.techstore_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private BigDecimal totalRevenue;
    private long totalOrdersCount;
    private long pendingOrdersCount;
    private long totalCustomersCount;
    private long lowStockProductsCount;
    
    private List<CategorySalesDTO> salesBreakdownByCategory;
    private List<MonthlySalesDTO> monthlySales;
    private List<ProductSalesStatsDTO> topSellingProducts;
    private List<OrderResponse> recentOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySalesDTO {
        private String categoryName;
        private BigDecimal totalSales;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySalesDTO {
        private String month;
        private BigDecimal totalSales;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSalesStatsDTO {
        private Long productId;
        private String productName;
        private String sku;
        private long quantitySold;
        private BigDecimal totalSales;
    }
}
