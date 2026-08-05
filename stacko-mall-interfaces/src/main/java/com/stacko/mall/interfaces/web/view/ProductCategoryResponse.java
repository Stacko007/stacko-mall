package com.stacko.mall.interfaces.web.view;

import com.stacko.mall.domain.enums.ProductCategoryStatus;
import com.stacko.mall.domain.model.ProductCategory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ProductCategoryResponse {
    private String id;
    private String parentId;
    private String name;
    private int sort;
    private ProductCategoryStatus status;
    private int level;
    private String path;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ProductCategoryResponse> children = new ArrayList<>();

    public static ProductCategoryResponse from(ProductCategory category) {
        ProductCategoryResponse response = new ProductCategoryResponse();
        response.setId(category.getId().value());
        response.setParentId(category.getParentId());
        response.setName(category.getName());
        response.setSort(category.getSort());
        response.setStatus(category.getStatus());
        response.setLevel(category.getLevel());
        response.setPath(category.getPath());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public ProductCategoryStatus getStatus() {
        return status;
    }

    public void setStatus(ProductCategoryStatus status) {
        this.status = status;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ProductCategoryResponse> getChildren() {
        return children;
    }

    public void setChildren(List<ProductCategoryResponse> children) {
        this.children = children == null ? new ArrayList<>() : children;
    }
}
