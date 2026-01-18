package com.stacko.mall.domain.model;

import java.util.UUID;

public record OrderId(String value) {
    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId cannot be blank");
        }
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID().toString());
    }
}
