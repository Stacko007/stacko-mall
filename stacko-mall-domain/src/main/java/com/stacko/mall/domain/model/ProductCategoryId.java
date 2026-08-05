package com.stacko.mall.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ProductCategoryId(String value) {
    public ProductCategoryId {
        Objects.requireNonNull(value, "value");
    }

    public static ProductCategoryId newId() {
        return new ProductCategoryId(UUID.randomUUID().toString());
    }
}
