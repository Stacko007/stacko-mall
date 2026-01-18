package com.stacko.mall.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stacko.mall.domain.model.ProductId;
import com.stacko.mall.domain.model.Stock;
import com.stacko.mall.domain.repository.StockRepository;
import com.stacko.mall.infra.dao.StockMapper;
import com.stacko.mall.infra.po.StockEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MybatisStockRepository implements StockRepository {
    private final StockMapper stockMapper;

    public MybatisStockRepository(StockMapper stockMapper) {
        this.stockMapper = stockMapper;
    }

    @Override
    public Stock save(Stock stock) {
        StockEntity entity = StockEntity.fromDomain(stock);
        StockEntity existing = stockMapper.selectById(entity.getProductId());
        if (existing == null) {
            stockMapper.insert(entity);
            return stock;
        }
        if (!Objects.equals(existing.getTenantId(), entity.getTenantId())) {
            throw new IllegalStateException("Stock tenant mismatch");
        }
        stockMapper.updateById(entity);
        return stock;
    }

    @Override
    public Optional<Stock> findByProductId(String tenantId, ProductId productId) {
        LambdaQueryWrapper<StockEntity> query = new LambdaQueryWrapper<>();
        query.eq(StockEntity::getProductId, productId.value())
                .eq(StockEntity::getTenantId, tenantId);
        StockEntity entity = stockMapper.selectOne(query);
        return Optional.ofNullable(entity).map(StockEntity::toDomain);
    }

    @Override
    public List<Stock> listByTenant(String tenantId) {
        LambdaQueryWrapper<StockEntity> query = new LambdaQueryWrapper<>();
        query.eq(StockEntity::getTenantId, tenantId)
                .orderByDesc(StockEntity::getUpdatedAt);
        return stockMapper.selectList(query)
                .stream()
                .map(StockEntity::toDomain)
                .toList();
    }
}
