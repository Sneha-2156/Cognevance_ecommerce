package com.cognevance.ecommerce.controller;

import com.cognevance.ecommerce.dto.AnalyticsDtos.DashboardSummary;
import com.cognevance.ecommerce.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // Admin only (enforced in SecurityConfig)
    @GetMapping("/dashboard")
    public DashboardSummary getDashboard() {
        return analyticsService.getDashboardSummary();
    }
}
