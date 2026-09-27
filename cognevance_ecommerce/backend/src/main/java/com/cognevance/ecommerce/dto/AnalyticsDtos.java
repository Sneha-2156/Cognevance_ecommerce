package com.cognevance.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AnalyticsDtos {

    public static class DashboardSummary {
        public BigDecimal totalRevenue;
        public long totalOrders;
        public long pendingOrders;
        public List<Map<String, Object>> monthlyRevenue; // [{month: "2026-08", revenue: 4520.00}]
        public List<Map<String, Object>> topProducts;     // [{productId, name, unitsSold}]

        public DashboardSummary(BigDecimal totalRevenue, long totalOrders, long pendingOrders,
                                 List<Map<String, Object>> monthlyRevenue, List<Map<String, Object>> topProducts) {
            this.totalRevenue = totalRevenue;
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.monthlyRevenue = monthlyRevenue;
            this.topProducts = topProducts;
        }
    }
}
