package com.cognevance.ecommerce.service;

import com.cognevance.ecommerce.entity.Order;
import com.cognevance.ecommerce.entity.OrderStatus;
import com.cognevance.ecommerce.entity.Payment;
import com.cognevance.ecommerce.repository.OrderRepository;
import com.cognevance.ecommerce.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Wraps Stripe's PaymentIntents API. Set stripe.secret-key (a real "sk_test_..."
 * key from your Stripe dashboard) before going live - with the placeholder
 * default below, Stripe calls will fail with an auth error, which is expected
 * until you plug in real test/live keys.
 */
@Service
public class PaymentService {

    @Value("${stripe.secret-key:sk_test_placeholder_replace_me}")
    private String stripeSecretKey;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    // Creates a Stripe PaymentIntent for the order total and stores a pending Payment record
    public PaymentIntent createPaymentIntent(Order order) throws StripeException {
        long amountInSmallestUnit = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100)).longValueExact(); // Stripe expects paise/cents

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInSmallestUnit)
                .setCurrency("inr")
                .putMetadata("orderId", String.valueOf(order.getId()))
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true).build())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setTransactionId(intent.getId());
        payment.setStatus(intent.getStatus());
        paymentRepository.save(payment);

        return intent;
    }

    // Called by the Stripe webhook (or, for simple/dev setups, by the frontend after
    // Stripe.js confirms the card) to mark the order as paid once payment succeeds.
    public void markOrderPaid(String paymentIntentId) {
        paymentRepository.findAll().stream()
                .filter(p -> paymentIntentId.equals(p.getTransactionId()))
                .findFirst()
                .ifPresent(payment -> {
                    payment.setStatus("succeeded");
                    paymentRepository.save(payment);

                    Order order = payment.getOrder();
                    order.setStatus(OrderStatus.PAID);
                    orderRepository.save(order);
                });
    }
}
