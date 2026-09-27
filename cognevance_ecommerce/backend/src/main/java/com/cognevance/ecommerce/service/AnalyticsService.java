package com.cognevance.ecommerce.service;

import com.cognevance.ecommerce.dto.AnalyticsDtos.DashboardSummary;
import com.cognevance.ecommerce.entity.OrderStatus;
import com.cognevance.ecommerce.repository.OrderRepository;
import com.cognevance.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Autowired
    public AnalyticsService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public DashboardSummary getDashboardSummary() {
        List<Map<String, Object>> monthly = orderRepository.monthlyRevenue().stream()
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("month", row[0]);
                    m.put("revenue", row[1]);
                    return m;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> topProducts = productRepository.findTopSellingProducts().stream()
                .limit(5)
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("productId", row[0]);
                    m.put("name", row[1]);
                    m.put("unitsSold", row[2]);
                    return m;
                })
                .collect(Collectors.toList());

        return new DashboardSummary(
                orderRepository.totalRevenue(),
                orderRepository.totalPaidOrders(),
                orderRepository.countByStatus(OrderStatus.PENDING),
                monthly,
                topProducts
        );
    }
}
