package com.stacko.mall.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stacko.mall.domain.model.Product;
import com.stacko.mall.domain.model.ProductId;
import com.stacko.mall.domain.repository.ProductRepository;
import com.stacko.mall.infra.po.ProductEntity;
import com.stacko.mall.infra.dao.ProductMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MybatisProductRepository implements ProductRepository {
    private final ProductMapper productMapper;

    public MybatisProductRepository(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = ProductEntity.fromDomain(product);
        ProductEntity existing = productMapper.selectById(entity.getId());
        if (existing == null) {
            productMapper.insert(entity);
            return product;
        }
        if (!Objects.equals(existing.getTenantId(), entity.getTenantId())) {
            throw new IllegalStateException("Product tenant mismatch");
        }
        productMapper.updateById(entity);
        return product;
    }

    @Override
    public Optional<Product> findById(String tenantId, ProductId id) {
        LambdaQueryWrapper<ProductEntity> query = new LambdaQueryWrapper<>();
        query.eq(ProductEntity::getId, id.value())
                .eq(ProductEntity::getTenantId, tenantId);
        ProductEntity entity = productMapper.selectOne(query);
        return Optional.ofNullable(entity).map(ProductEntity::toDomain);
    }

    @Override
    public List<Product> listByTenant(String tenantId) {
        LambdaQueryWrapper<ProductEntity> query = new LambdaQueryWrapper<>();
        query.eq(ProductEntity::getTenantId, tenantId)
                .orderByDesc(ProductEntity::getUpdatedAt);
        return productMapper.selectList(query)
                .stream()
                .map(ProductEntity::toDomain)
                .toList();
    }
}
