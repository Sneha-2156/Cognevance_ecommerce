package com.cognevance.ecommerce.service;

import com.cognevance.ecommerce.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(Order order) {
        String to = order.getUser().getUsername();
        String subject = "Order Confirmation - Order #" + order.getId();
        StringBuilder body = new StringBuilder();
        body.append("Hi ").append(order.getUser().getName()).append(",\n\n")
            .append("Thank you for your order! Here's a summary:\n\n");

        order.getItems().forEach(item -> body.append("- ")
                .append(item.getProduct().getName())
                .append(" x").append(item.getQuantity())
                .append(" @ ₹").append(item.getPriceAtPurchase())
                .append("\n"));

        body.append("\nTotal: ₹").append(order.getTotalAmount())
            .append("\nShipping to: ").append(order.getShippingAddress())
            .append("\n\nWe'll notify you when your order ships.");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body.toString());
            mailSender.send(message);
        } catch (Exception e) {
            // Don't let a mail server outage fail the checkout - just log it.
            log.warn("Failed to send order confirmation email to {}: {}", to, e.getMessage());
        }
    }
}
