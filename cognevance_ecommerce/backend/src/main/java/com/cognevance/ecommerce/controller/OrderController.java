package com.cognevance.ecommerce.controller;

import com.cognevance.ecommerce.dto.OrderDtos.CheckoutRequest;
import com.cognevance.ecommerce.dto.OrderDtos.CheckoutResponse;
import com.cognevance.ecommerce.entity.Order;
import com.cognevance.ecommerce.entity.OrderStatus;
import com.cognevance.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Customer checks out their cart -> creates Order + Stripe PaymentIntent
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@RequestBody CheckoutRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(auth.getName(), request));
    }

    // Customer's own order history
    @GetMapping("/me")
    public List<Order> myOrders(Authentication auth) {
        return orderService.findByUser(auth.getName());
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.findById(id);
    }

    // Admin: all orders
    @GetMapping
    public List<Order> allOrders() {
        return orderService.findAll();
    }

    // Admin: move an order through PENDING -> PAID -> SHIPPED -> DELIVERED (or CANCELLED)
    @PutMapping("/{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        OrderStatus status = OrderStatus.valueOf(body.get("status").toUpperCase());
        Order updated = orderService.updateStatus(id, status);
        if (status == OrderStatus.PAID) {
            orderService.sendConfirmationEmail(id); // fire the confirmation email once marked paid
        }
        return updated;
    }
}
