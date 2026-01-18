package com.stacko.mall.domain.repository;

import com.stacko.mall.domain.model.Order;
import com.stacko.mall.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(String tenantId, OrderId id);

    List<Order> listByTenant(String tenantId);

    List<Order> listByTenantAndBuyer(String tenantId, String buyerId);
}
