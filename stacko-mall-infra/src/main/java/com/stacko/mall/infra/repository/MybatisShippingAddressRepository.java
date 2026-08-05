package com.stacko.mall.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stacko.mall.domain.model.ShippingAddress;
import com.stacko.mall.domain.model.ShippingAddressId;
import com.stacko.mall.domain.repository.ShippingAddressRepository;
import com.stacko.mall.infra.dao.ShippingAddressMapper;
import com.stacko.mall.infra.po.ShippingAddressEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MybatisShippingAddressRepository implements ShippingAddressRepository {
    private final ShippingAddressMapper shippingAddressMapper;

    public MybatisShippingAddressRepository(ShippingAddressMapper shippingAddressMapper) {
        this.shippingAddressMapper = shippingAddressMapper;
    }

    @Override
    public ShippingAddress save(ShippingAddress address) {
        ShippingAddressEntity entity = ShippingAddressEntity.fromDomain(address);
        ShippingAddressEntity existing = shippingAddressMapper.selectById(entity.getId());
        if (existing == null) {
            shippingAddressMapper.insert(entity);
            return address;
        }
        if (!Objects.equals(existing.getTenantId(), entity.getTenantId())) {
            throw new IllegalStateException("Shipping address tenant mismatch");
        }
        shippingAddressMapper.updateById(entity);
        return address;
    }

    @Override
    public Optional<ShippingAddress> findById(String tenantId, String buyerId, ShippingAddressId id) {
        LambdaQueryWrapper<ShippingAddressEntity> query = baseBuyerQuery(tenantId, buyerId)
                .eq(ShippingAddressEntity::getId, id.value());
        return Optional.ofNullable(shippingAddressMapper.selectOne(query))
                .map(ShippingAddressEntity::toDomain);
    }

    @Override
    public List<ShippingAddress> listByBuyer(String tenantId, String buyerId) {
        LambdaQueryWrapper<ShippingAddressEntity> query = baseBuyerQuery(tenantId, buyerId)
                .orderByDesc(ShippingAddressEntity::getDefaultAddress)
                .orderByDesc(ShippingAddressEntity::getUpdatedAt);
        return shippingAddressMapper.selectList(query).stream()
                .map(ShippingAddressEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<ShippingAddress> findDefault(String tenantId, String buyerId) {
        LambdaQueryWrapper<ShippingAddressEntity> query = baseBuyerQuery(tenantId, buyerId)
                .eq(ShippingAddressEntity::getDefaultAddress, true);
        return Optional.ofNullable(shippingAddressMapper.selectOne(query))
                .map(ShippingAddressEntity::toDomain);
    }

    @Override
    public void clearDefault(String tenantId, String buyerId, ShippingAddressId exceptId) {
        listByBuyer(tenantId, buyerId).stream()
                .filter(ShippingAddress::isDefaultAddress)
                .filter(address -> exceptId == null || !address.getId().equals(exceptId))
                .forEach(address -> {
                    address.clearDefault();
                    shippingAddressMapper.updateById(ShippingAddressEntity.fromDomain(address));
                });
    }

    @Override
    public void delete(String tenantId, String buyerId, ShippingAddressId id) {
        LambdaQueryWrapper<ShippingAddressEntity> query = baseBuyerQuery(tenantId, buyerId)
                .eq(ShippingAddressEntity::getId, id.value());
        shippingAddressMapper.delete(query);
    }

    private LambdaQueryWrapper<ShippingAddressEntity> baseBuyerQuery(String tenantId, String buyerId) {
        return new LambdaQueryWrapper<ShippingAddressEntity>()
                .eq(ShippingAddressEntity::getTenantId, tenantId)
                .eq(ShippingAddressEntity::getBuyerId, buyerId);
    }
}
