package com.stacko.mall.domain.repository;

import com.stacko.mall.domain.model.ProductCategory;
import com.stacko.mall.domain.model.ProductCategoryId;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository {
    ProductCategory save(ProductCategory category);

    Optional<ProductCategory> findById(String tenantId, ProductCategoryId id);

    List<ProductCategory> listByTenant(String tenantId);

    long countByParentId(String tenantId, String parentId);

    void delete(String tenantId, ProductCategoryId id);
}
