package com.stacko.mall.domain.repository;

import com.stacko.mall.domain.model.IdempotencyRecord;

import java.util.Optional;

public interface IdempotencyRepository {
    Optional<IdempotencyRecord> find(String tenantId, String idempotencyKey, String bizType);

    IdempotencyRecord save(IdempotencyRecord record);
}
