package com.stacko.mall.domain.catalog;

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
