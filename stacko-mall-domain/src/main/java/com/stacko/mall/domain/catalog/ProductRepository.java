package com.stacko.mall.domain.catalog;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);

    Optional<Product> findById(String tenantId, ProductId id);

    List<Product> listByTenant(String tenantId);
}
