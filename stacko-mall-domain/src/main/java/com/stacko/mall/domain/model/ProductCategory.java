package com.stacko.mall.domain.model;

import com.stacko.mall.domain.enums.ProductCategoryStatus;

import java.time.Instant;
import java.util.Objects;

public class ProductCategory {
    private final ProductCategoryId id;
    private final String tenantId;
    private String parentId;
    private String name;
    private int sort;
    private ProductCategoryStatus status;
    private int level;
    private String path;
    private Instant createdAt;
    private Instant updatedAt;

    private ProductCategory(ProductCategoryId id,
                            String tenantId,
                            String parentId,
                            String name,
                            int sort,
                            ProductCategoryStatus status,
                            int level,
                            String path,
                            Instant createdAt,
                            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.parentId = normalizeParentId(parentId);
        this.name = requireName(name);
        this.sort = sort;
        this.status = Objects.requireNonNull(status, "status");
        this.level = level;
        this.path = Objects.requireNonNull(path, "path");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static ProductCategory create(String tenantId,
                                         String parentId,
                                         String name,
                                         int sort,
                                         ProductCategoryStatus status,
                                         int level,
                                         String parentPath) {
        ProductCategoryId id = ProductCategoryId.newId();
        Instant now = Instant.now();
        return new ProductCategory(id, tenantId, parentId, name, sort, status, level,
                buildPath(parentPath, id.value()), now, now);
    }

    public static ProductCategory restore(ProductCategoryId id,
                                          String tenantId,
                                          String parentId,
                                          String name,
                                          int sort,
                                          ProductCategoryStatus status,
                                          int level,
                                          String path,
                                          Instant createdAt,
                                          Instant updatedAt) {
        return new ProductCategory(id, tenantId, parentId, name, sort, status, level, path, createdAt, updatedAt);
    }

    public void update(String parentId, String name, int sort, ProductCategoryStatus status, int level, String parentPath) {
        this.parentId = normalizeParentId(parentId);
        this.name = requireName(name);
        this.sort = sort;
        this.status = Objects.requireNonNull(status, "status");
        this.level = level;
        this.path = buildPath(parentPath, id.value());
        this.updatedAt = Instant.now();
    }

    public boolean isEnabled() {
        return status == ProductCategoryStatus.ENABLED;
    }

    private static String buildPath(String parentPath, String id) {
        if (parentPath == null || parentPath.isBlank()) {
            return id;
        }
        return parentPath + "/" + id;
    }

    private static String normalizeParentId(String parentId) {
        return parentId == null || parentId.isBlank() ? null : parentId.trim();
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name required");
        }
        return name.trim();
    }

    public ProductCategoryId getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public int getSort() {
        return sort;
    }

    public ProductCategoryStatus getStatus() {
        return status;
    }

    public int getLevel() {
        return level;
    }

    public String getPath() {
        return path;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
