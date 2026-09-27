package com.cognevance.ecommerce.service;

import com.cognevance.ecommerce.dto.OrderDtos.CartLine;
import com.cognevance.ecommerce.dto.OrderDtos.CheckoutRequest;
import com.cognevance.ecommerce.dto.OrderDtos.CheckoutResponse;
import com.cognevance.ecommerce.entity.*;
import com.cognevance.ecommerce.repository.OrderRepository;
import com.cognevance.ecommerce.repository.ProductRepository;
import com.cognevance.ecommerce.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final EmailService emailService;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                         UserRepository userRepository, ProductService productService,
                         PaymentService paymentService, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
        this.paymentService = paymentService;
        this.emailService = emailService;
    }

    // Builds the order from the cart, reserves stock, and creates a Stripe PaymentIntent.
    // The order stays PENDING until PaymentService.markOrderPaid() confirms payment.
    @Transactional
    public CheckoutResponse checkout(String userEmail, CheckoutRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.items == null || request.items.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.shippingAddress);

        BigDecimal total = BigDecimal.ZERO;
        for (CartLine line : request.items) {
            Product product = productRepository.findById(line.productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + line.productId));

            OrderItem item = new OrderItem(order, product, line.quantity, product.getPrice());
            order.getItems().add(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(line.quantity)));

            productService.reduceStock(product.getId(), line.quantity); // throws if insufficient stock
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        try {
            PaymentIntent intent = paymentService.createPaymentIntent(saved);
            return new CheckoutResponse(saved.getId(), intent.getClientSecret(), total.toString());
        } catch (StripeException e) {
            // Order + stock reservation still stand; the client can retry payment separately.
            throw new IllegalStateException("Payment setup failed: " + e.getMessage());
        }
    }

    // Called once the frontend confirms the Stripe payment succeeded (or by a webhook)
    public void confirmPayment(String paymentIntentId) {
        paymentService.markOrderPaid(paymentIntentId);
    }

    public void sendConfirmationEmail(Long orderId) {
        Order order = findById(orderId);
        emailService.sendOrderConfirmation(order);
    }

    public List<Order> findByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public List<Order> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    public Order updateStatus(Long id, OrderStatus status) {
        Order order = findById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
