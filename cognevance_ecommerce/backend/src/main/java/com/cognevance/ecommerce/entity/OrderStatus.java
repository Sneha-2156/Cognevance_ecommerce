package com.cognevance.ecommerce.entity;

public enum OrderStatus {
    PENDING,      // created, awaiting payment
    PAID,         // payment succeeded
    SHIPPED,
    DELIVERED,
    CANCELLED
}
