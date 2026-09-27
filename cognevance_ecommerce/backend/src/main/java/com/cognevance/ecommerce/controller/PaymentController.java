package com.cognevance.ecommerce.controller;

import com.cognevance.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final OrderService orderService;

    @Autowired
    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Called by the frontend right after Stripe.js confirms the card payment succeeded.
    // For a production build, prefer a Stripe webhook (payment_intent.succeeded) instead,
    // since it's authoritative and doesn't depend on the browser staying online -
    // this endpoint is the simpler path for a learning/portfolio project.
    @PostMapping("/confirm")
    public Map<String, String> confirmPayment(@RequestBody Map<String, String> body) {
        String paymentIntentId = body.get("paymentIntentId");
        orderService.confirmPayment(paymentIntentId);
        return Map.of("status", "confirmed");
    }
}
