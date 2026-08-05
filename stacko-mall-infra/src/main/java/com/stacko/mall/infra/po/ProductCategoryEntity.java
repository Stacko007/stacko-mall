package com.stacko.mall.infra.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stacko.mall.domain.enums.ProductCategoryStatus;
import com.stacko.mall.domain.model.ProductCategory;
import com.stacko.mall.domain.model.ProductCategoryId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@TableName("mall_product_category")
public class ProductCategoryEntity {
    @TableId
    private String id;
    private String tenantId;
    private String parentId;
    private String name;
    private Integer sort;
    private ProductCategoryStatus status;
    private Integer level;
    private String path;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductCategoryEntity fromDomain(ProductCategory category) {
        ProductCategoryEntity entity = new ProductCategoryEntity();
        entity.setId(category.getId().value());
        entity.setTenantId(category.getTenantId());
        entity.setParentId(category.getParentId());
        entity.setName(category.getName());
        entity.setSort(category.getSort());
        entity.setStatus(category.getStatus());
        entity.setLevel(category.getLevel());
        entity.setPath(category.getPath());
        entity.setCreatedAt(toLocalDateTime(category.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(category.getUpdatedAt()));
        return entity;
    }

    public ProductCategory toDomain() {
        return ProductCategory.restore(
                new ProductCategoryId(id),
                tenantId,
                parentId,
                name,
                sort == null ? 0 : sort,
                status,
                level == null ? 1 : level,
                path,
                toInstant(createdAt),
                toInstant(updatedAt)
        );
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public ProductCategoryStatus getStatus() {
        return status;
    }

    public void setStatus(ProductCategoryStatus status) {
        this.status = status;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
