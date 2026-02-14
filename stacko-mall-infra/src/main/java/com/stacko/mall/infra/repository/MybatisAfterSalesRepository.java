package com.stacko.mall.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stacko.mall.domain.model.AfterSales;
import com.stacko.mall.domain.model.AfterSalesId;
import com.stacko.mall.domain.model.OrderId;
import com.stacko.mall.domain.repository.AfterSalesRepository;
import com.stacko.mall.infra.dao.AfterSalesMapper;
import com.stacko.mall.infra.po.AfterSalesEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MybatisAfterSalesRepository implements AfterSalesRepository {
    private final AfterSalesMapper afterSalesMapper;

    public MybatisAfterSalesRepository(AfterSalesMapper afterSalesMapper) {
        this.afterSalesMapper = afterSalesMapper;
    }

    @Override
    public AfterSales save(AfterSales afterSales) {
        AfterSalesEntity entity = AfterSalesEntity.fromDomain(afterSales);
        AfterSalesEntity existing = afterSalesMapper.selectById(entity.getId());
        if (existing == null) {
            afterSalesMapper.insert(entity);
            return afterSales;
        }
        if (!Objects.equals(existing.getTenantId(), entity.getTenantId())) {
            throw new IllegalStateException("After-sales tenant mismatch");
        }
        afterSalesMapper.updateById(entity);
        return afterSales;
    }

    @Override
    public Optional<AfterSales> findById(String tenantId, AfterSalesId id) {
        LambdaQueryWrapper<AfterSalesEntity> query = new LambdaQueryWrapper<>();
        query.eq(AfterSalesEntity::getId, id.value())
                .eq(AfterSalesEntity::getTenantId, tenantId);
        return Optional.ofNullable(afterSalesMapper.selectOne(query))
                .map(AfterSalesEntity::toDomain);
    }

    @Override
    public List<AfterSales> listByTenant(String tenantId) {
        LambdaQueryWrapper<AfterSalesEntity> query = new LambdaQueryWrapper<>();
        query.eq(AfterSalesEntity::getTenantId, tenantId)
                .orderByDesc(AfterSalesEntity::getUpdatedAt);
        return afterSalesMapper.selectList(query).stream()
                .map(AfterSalesEntity::toDomain)
                .toList();
    }

    @Override
    public List<AfterSales> listByOrderId(String tenantId, OrderId orderId) {
        LambdaQueryWrapper<AfterSalesEntity> query = new LambdaQueryWrapper<>();
        query.eq(AfterSalesEntity::getTenantId, tenantId)
                .eq(AfterSalesEntity::getOrderId, orderId.value())
                .orderByDesc(AfterSalesEntity::getUpdatedAt);
        return afterSalesMapper.selectList(query).stream()
                .map(AfterSalesEntity::toDomain)
                .toList();
    }
}
