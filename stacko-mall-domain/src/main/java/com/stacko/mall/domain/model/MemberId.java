package com.stacko.mall.domain.model;

import java.util.Objects;
import java.util.UUID;

public record MemberId(String value) {
    public MemberId {
        Objects.requireNonNull(value, "value");
    }

    public static MemberId newId() {
        return new MemberId(UUID.randomUUID().toString());
    }
}
