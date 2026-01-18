package com.stacko.mall.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ProductId(String value) {
    public ProductId {
        Objects.requireNonNull(value, "value");
    }

    public static ProductId newId() {
        return new ProductId(UUID.randomUUID().toString());
    }
}
