package com.stacko.mall.application.command;

import com.stacko.mall.domain.enums.ProductCategoryStatus;

public class CreateProductCategoryCommand {
    private String tenantId;
    private String parentId;
    private String name;
    private Integer sort;
    private ProductCategoryStatus status;

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
}
