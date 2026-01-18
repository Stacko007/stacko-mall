package com.stacko.mall.interfaces.web.view;

import com.stacko.mall.domain.model.Product;
import com.stacko.mall.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductResponse {
    private String id;
    private String tenantId;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProductResponse from(Product product) {
        ProductResponse response = new ProductResponse();
        response.id = product.getId().value();
        response.tenantId = product.getTenantId();
        response.name = product.getName();
        response.description = product.getDescription();
        response.price = product.getPrice();
        response.status = product.getStatus();
        response.createdAt = product.getCreatedAt();
        response.updatedAt = product.getUpdatedAt();
        return response;
    }

    public String getId() {
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
