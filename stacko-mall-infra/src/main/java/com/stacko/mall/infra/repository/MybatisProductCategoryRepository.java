package com.stacko.mall.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stacko.mall.domain.model.ProductCategory;
import com.stacko.mall.domain.model.ProductCategoryId;
import com.stacko.mall.domain.repository.ProductCategoryRepository;
import com.stacko.mall.infra.dao.ProductCategoryMapper;
import com.stacko.mall.infra.po.ProductCategoryEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MybatisProductCategoryRepository implements ProductCategoryRepository {
    private final ProductCategoryMapper productCategoryMapper;

    public MybatisProductCategoryRepository(ProductCategoryMapper productCategoryMapper) {
        this.productCategoryMapper = productCategoryMapper;
    }

    @Override
    public ProductCategory save(ProductCategory category) {
        ProductCategoryEntity entity = ProductCategoryEntity.fromDomain(category);
        ProductCategoryEntity existing = productCategoryMapper.selectById(entity.getId());
        if (existing == null) {
            productCategoryMapper.insert(entity);
            return category;
        }
        if (!Objects.equals(existing.getTenantId(), entity.getTenantId())) {
            throw new IllegalStateException("Category tenant mismatch");
        }
        productCategoryMapper.updateById(entity);
        return category;
    }

    @Override
    public Optional<ProductCategory> findById(String tenantId, ProductCategoryId id) {
        LambdaQueryWrapper<ProductCategoryEntity> query = new LambdaQueryWrapper<>();
        query.eq(ProductCategoryEntity::getTenantId, tenantId)
                .eq(ProductCategoryEntity::getId, id.value());
        return Optional.ofNullable(productCategoryMapper.selectOne(query))
                .map(ProductCategoryEntity::toDomain);
    }

    @Override
    public List<ProductCategory> listByTenant(String tenantId) {
        LambdaQueryWrapper<ProductCategoryEntity> query = new LambdaQueryWrapper<>();
        query.eq(ProductCategoryEntity::getTenantId, tenantId)
                .orderByAsc(ProductCategoryEntity::getLevel)
                .orderByAsc(ProductCategoryEntity::getSort)
                .orderByDesc(ProductCategoryEntity::getUpdatedAt);
        return productCategoryMapper.selectList(query).stream()
                .map(ProductCategoryEntity::toDomain)
                .toList();
    }

    @Override
    public long countByParentId(String tenantId, String parentId) {
        LambdaQueryWrapper<ProductCategoryEntity> query = new LambdaQueryWrapper<>();
        query.eq(ProductCategoryEntity::getTenantId, tenantId)
                .eq(ProductCategoryEntity::getParentId, parentId);
        return productCategoryMapper.selectCount(query);
    }

    @Override
    public void delete(String tenantId, ProductCategoryId id) {
        LambdaQueryWrapper<ProductCategoryEntity> query = new LambdaQueryWrapper<>();
        query.eq(ProductCategoryEntity::getTenantId, tenantId)
                .eq(ProductCategoryEntity::getId, id.value());
        productCategoryMapper.delete(query);
    }
}
