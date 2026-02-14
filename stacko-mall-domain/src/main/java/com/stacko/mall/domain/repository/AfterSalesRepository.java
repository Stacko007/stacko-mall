package com.stacko.mall.domain.repository;

import com.stacko.mall.domain.model.AfterSales;
import com.stacko.mall.domain.model.AfterSalesId;
import com.stacko.mall.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

public interface AfterSalesRepository {
    AfterSales save(AfterSales afterSales);

    Optional<AfterSales> findById(String tenantId, AfterSalesId id);

    List<AfterSales> listByTenant(String tenantId);

    List<AfterSales> listByOrderId(String tenantId, OrderId orderId);
}
