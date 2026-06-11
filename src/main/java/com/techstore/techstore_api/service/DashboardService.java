package com.techstore.techstore_api.service;

import com.techstore.techstore_api.dto.response.DashboardStatsResponse;

import java.util.List;

public interface DashboardService {
    DashboardStatsResponse getDashboardStats();
    List<DashboardStatsResponse.LowStockItemDTO> getLowStockItems();
}
