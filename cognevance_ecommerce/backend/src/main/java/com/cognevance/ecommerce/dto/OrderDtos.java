package com.cognevance.ecommerce.dto;

import java.util.List;

public class OrderDtos {

    // What the frontend cart sends to create an order
    public static class CheckoutRequest {
        public List<CartLine> items;
        public String shippingAddress;
    }

    public static class CartLine {
        public Long productId;
        public int quantity;
    }

    // Returned after creating an order + Stripe PaymentIntent, so the
    // frontend can confirm payment with Stripe.js using the client secret
    public static class CheckoutResponse {
        public Long orderId;
        public String clientSecret;
        public String amount;

        public CheckoutResponse(Long orderId, String clientSecret, String amount) {
            this.orderId = orderId;
            this.clientSecret = clientSecret;
            this.amount = amount;
        }
    }
}
