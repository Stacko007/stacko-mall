package com.stacko.mall.domain.model;

import com.stacko.mall.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Product {
    private final ProductId id;
    private final String tenantId;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private Product(ProductId id,
                    String tenantId,
                    String name,
                    String description,
                    BigDecimal price,
                    ProductStatus status,
                    Instant createdAt,
                    Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.name = Objects.requireNonNull(name, "name");
        this.description = description;
        this.price = Objects.requireNonNull(price, "price");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static Product create(String tenantId,
                                 String name,
                                 String description,
                                 BigDecimal price) {
        Instant now = Instant.now();
        return new Product(ProductId.newId(), tenantId, name, description, price, ProductStatus.DRAFT, now, now);
    }

    public static Product restore(ProductId id,
                                  String tenantId,
                                  String name,
                                  String description,
                                  BigDecimal price,
                                  ProductStatus status,
                                  Instant createdAt,
                                  Instant updatedAt) {
        return new Product(id, tenantId, name, description, price, status, createdAt, updatedAt);
    }

    public void update(String name, String description, BigDecimal price, ProductStatus status) {
        this.name = Objects.requireNonNull(name, "name");
        this.description = description;
        this.price = Objects.requireNonNull(price, "price");
        this.status = Objects.requireNonNull(status, "status");
        this.updatedAt = Instant.now();
    }

    public ProductId getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
