package com.stacko.mall.domain.repository;

import com.stacko.mall.domain.model.ProductId;
import com.stacko.mall.domain.model.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {
    Stock save(Stock stock);

    Optional<Stock> findByProductId(String tenantId, ProductId productId);

    List<Stock> listByTenant(String tenantId);
}
