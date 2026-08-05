package com.stacko.mall.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ShippingAddressId(String value) {
    public ShippingAddressId {
        Objects.requireNonNull(value, "value");
    }

    public static ShippingAddressId newId() {
        return new ShippingAddressId(UUID.randomUUID().toString());
    }
}
