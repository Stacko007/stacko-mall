package com.stacko.mall.interfaces.web.dto;

import com.stacko.mall.domain.enums.ProductCategoryStatus;
import jakarta.validation.constraints.NotBlank;

public class ProductCategoryRequest {
    private String parentId;
    @NotBlank
    private String name;
    private Integer sort;
    private ProductCategoryStatus status;

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
