package com.cognevance.ecommerce.repository;

import com.cognevance.ecommerce.entity.Order;
import com.cognevance.ecommerce.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'PAID'")
    BigDecimal totalRevenue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PAID'")
    long totalPaidOrders();

    // Revenue grouped by month, e.g. for a line chart: [ "2026-08", 4520.00 ]
    @Query("SELECT FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m'), SUM(o.totalAmount) " +
           "FROM Order o WHERE o.status = 'PAID' " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m') " +
           "ORDER BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m')")
    List<Object[]> monthlyRevenue();

    long countByStatus(OrderStatus status);
}
