package com.stacko.mall.domain.model;

import java.util.UUID;

public record PaymentId(String value) {
    public PaymentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PaymentId cannot be blank");
        }
    }

    public static PaymentId newId() {
        return new PaymentId(UUID.randomUUID().toString());
    }
}
