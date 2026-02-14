package com.stacko.mall.domain.model;

import java.util.UUID;

public record AfterSalesId(String value) {
    public AfterSalesId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AfterSalesId cannot be blank");
        }
    }

    public static AfterSalesId newId() {
        return new AfterSalesId(UUID.randomUUID().toString());
    }
}
