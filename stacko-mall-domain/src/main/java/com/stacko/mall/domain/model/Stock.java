package com.stacko.mall.domain.model;

import java.time.Instant;
import java.util.Objects;

public class Stock {
    private final ProductId productId;
    private final String tenantId;
    private int quantity;
    private Instant createdAt;
    private Instant updatedAt;

    private Stock(ProductId productId, String tenantId, int quantity, Instant createdAt, Instant updatedAt) {
        this.productId = Objects.requireNonNull(productId, "productId");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.quantity = quantity;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static Stock create(String tenantId, ProductId productId, int quantity) {
        Instant now = Instant.now();
        return new Stock(productId, tenantId, quantity, now, now);
    }

    public static Stock restore(ProductId productId,
                                String tenantId,
                                int quantity,
                                Instant createdAt,
                                Instant updatedAt) {
        return new Stock(productId, tenantId, quantity, createdAt, updatedAt);
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.updatedAt = Instant.now();
    }

    public void adjust(int delta) {
        this.quantity += delta;
        this.updatedAt = Instant.now();
    }

    public ProductId getProductId() {
        return productId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
