package com.stacko.mall.infra.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stacko.mall.domain.model.ProductId;
import com.stacko.mall.domain.model.Stock;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@TableName("catalog_stock")
public class StockEntity {
    @TableId("product_id")
    private String productId;
    private String tenantId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StockEntity fromDomain(Stock stock) {
        StockEntity entity = new StockEntity();
        entity.setProductId(stock.getProductId().value());
        entity.setTenantId(stock.getTenantId());
        entity.setQuantity(stock.getQuantity());
        entity.setCreatedAt(toLocalDateTime(stock.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(stock.getUpdatedAt()));
        return entity;
    }

    public Stock toDomain() {
        return Stock.restore(
                new ProductId(productId),
                tenantId,
                quantity == null ? 0 : quantity,
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
