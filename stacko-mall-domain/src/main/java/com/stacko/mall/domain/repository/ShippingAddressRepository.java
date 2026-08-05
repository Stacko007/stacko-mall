package com.stacko.mall.domain.repository;

import com.stacko.mall.domain.model.ShippingAddress;
import com.stacko.mall.domain.model.ShippingAddressId;

import java.util.List;
import java.util.Optional;

public interface ShippingAddressRepository {
    ShippingAddress save(ShippingAddress address);

    Optional<ShippingAddress> findById(String tenantId, String buyerId, ShippingAddressId id);

    List<ShippingAddress> listByBuyer(String tenantId, String buyerId);

    Optional<ShippingAddress> findDefault(String tenantId, String buyerId);

    void clearDefault(String tenantId, String buyerId, ShippingAddressId exceptId);

    void delete(String tenantId, String buyerId, ShippingAddressId id);
}
