package com.stacko.mall.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stacko.mall.domain.model.IdempotencyRecord;
import com.stacko.mall.domain.repository.IdempotencyRepository;
import com.stacko.mall.infra.dao.IdempotencyMapper;
import com.stacko.mall.infra.po.IdempotencyEntity;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MybatisIdempotencyRepository implements IdempotencyRepository {
    private final IdempotencyMapper idempotencyMapper;

    public MybatisIdempotencyRepository(IdempotencyMapper idempotencyMapper) {
        this.idempotencyMapper = idempotencyMapper;
    }

    @Override
    public Optional<IdempotencyRecord> find(String tenantId, String idempotencyKey, String bizType) {
        LambdaQueryWrapper<IdempotencyEntity> query = new LambdaQueryWrapper<>();
        query.eq(IdempotencyEntity::getTenantId, tenantId)
                .eq(IdempotencyEntity::getIdempotencyKey, idempotencyKey)
                .eq(IdempotencyEntity::getBizType, bizType);
        return Optional.ofNullable(idempotencyMapper.selectOne(query))
                .map(IdempotencyEntity::toDomain);
    }

    @Override
    public IdempotencyRecord save(IdempotencyRecord record) {
        IdempotencyEntity entity = IdempotencyEntity.fromDomain(record);
        if (entity.getId() == null) {
            try {
                idempotencyMapper.insert(entity);
            } catch (DuplicateKeyException ex) {
                throw ex;
            }
            return entity.toDomain();
        }
        idempotencyMapper.updateById(entity);
        return record;
    }
}
