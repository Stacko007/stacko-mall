package com.stacko.mall.application.service;

import com.stacko.mall.domain.enums.IdempotencyStatus;
import com.stacko.mall.domain.model.IdempotencyRecord;
import com.stacko.mall.domain.repository.IdempotencyRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {
    private static final int MAX_KEY_LENGTH = 128;
    private final IdempotencyRepository idempotencyRepository;

    public IdempotencyService(IdempotencyRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
    }

    @Transactional
    public AcquireResult acquire(String tenantId, String idempotencyKey, String bizType) {
        validateInputs(tenantId, idempotencyKey, bizType);
        IdempotencyRecord record = idempotencyRepository
                .find(tenantId, idempotencyKey, bizType)
                .orElse(null);
        if (record != null) {
            return new AcquireResult(record, false);
        }
        IdempotencyRecord created = IdempotencyRecord.createInProgress(tenantId, idempotencyKey, bizType);
        try {
            return new AcquireResult(idempotencyRepository.save(created), true);
        } catch (DuplicateKeyException ex) {
            return new AcquireResult(idempotencyRepository
                    .find(tenantId, idempotencyKey, bizType)
                    .orElseThrow(() -> ex), false);
        }
    }

    @Transactional
    public IdempotencyRecord markSuccess(IdempotencyRecord record, String bizId) {
        record.markSuccess(bizId);
        return idempotencyRepository.save(record);
    }

    @Transactional
    public IdempotencyRecord markFailed(IdempotencyRecord record) {
        record.markFailed();
        return idempotencyRepository.save(record);
    }

    @Transactional
    public IdempotencyRecord restart(IdempotencyRecord record) {
        record.restart();
        return idempotencyRepository.save(record);
    }

    public boolean isSuccess(IdempotencyRecord record) {
        return record.getStatus() == IdempotencyStatus.SUCCESS;
    }

    public boolean isInProgress(IdempotencyRecord record) {
        return record.getStatus() == IdempotencyStatus.IN_PROGRESS;
    }

    private void validateInputs(String tenantId, String idempotencyKey, String bizType) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant id required");
        }
        if (bizType == null || bizType.isBlank()) {
            throw new IllegalArgumentException("Biz type required");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key required");
        }
        if (idempotencyKey.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("Idempotency key too long, max length is " + MAX_KEY_LENGTH);
        }
    }

    public static class AcquireResult {
        private final IdempotencyRecord record;
        private final boolean newlyCreated;

        public AcquireResult(IdempotencyRecord record, boolean newlyCreated) {
            this.record = record;
            this.newlyCreated = newlyCreated;
        }

        public IdempotencyRecord getRecord() {
            return record;
        }

        public boolean isNewlyCreated() {
            return newlyCreated;
        }
    }
}
