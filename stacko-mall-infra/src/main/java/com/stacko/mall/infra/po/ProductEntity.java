package com.stacko.mall.infra.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stacko.mall.domain.model.Product;
import com.stacko.mall.domain.model.ProductId;
import com.stacko.mall.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@TableName("mall_product")
public class ProductEntity {
    @TableId
    private String id;
    private String tenantId;
    private String categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductEntity fromDomain(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setId(product.getId().value());
        entity.setTenantId(product.getTenantId());
        entity.setCategoryId(product.getCategoryId());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPrice(product.getPrice());
        entity.setStatus(product.getStatus());
        entity.setCreatedAt(toLocalDateTime(product.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(product.getUpdatedAt()));
        return entity;
    }

    public Product toDomain() {
        return Product.restore(
                new ProductId(id),
                tenantId,
                categoryId,
                name,
                description,
                price,
                status,
                toInstant(createdAt),
                toInstant(updatedAt)
        );
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static Instant toInstant(LocalDateTime time) {
        return time == null ? null : time.toInstant(ZoneOffset.UTC);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
