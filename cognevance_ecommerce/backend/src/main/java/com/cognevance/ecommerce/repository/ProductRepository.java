package com.cognevance.ecommerce.repository;

import com.cognevance.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> search(@Param("keyword") String keyword);

    // Analytics: top-selling products by units sold
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as unitsSold " +
           "FROM OrderItem oi GROUP BY oi.product.id, oi.product.name " +
           "ORDER BY unitsSold DESC")
    List<Object[]> findTopSellingProducts();
}
