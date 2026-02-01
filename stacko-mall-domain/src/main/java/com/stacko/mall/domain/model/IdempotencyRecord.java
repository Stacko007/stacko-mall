package com.stacko.mall.domain.model;

import com.stacko.mall.domain.enums.IdempotencyStatus;

import java.time.Instant;
import java.util.Objects;

public class IdempotencyRecord {
    private Long id;
    private final String tenantId;
    private final String idempotencyKey;
    private final String bizType;
    private String bizId;
    private IdempotencyStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private IdempotencyRecord(Long id,
                              String tenantId,
                              String idempotencyKey,
                              String bizType,
                              String bizId,
                              IdempotencyStatus status,
                              Instant createdAt,
                              Instant updatedAt) {
        this.id = id;
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        this.bizType = Objects.requireNonNull(bizType, "bizType");
        this.bizId = bizId;
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static IdempotencyRecord createInProgress(String tenantId, String idempotencyKey, String bizType) {
        Instant now = Instant.now();
        return new IdempotencyRecord(null, tenantId, idempotencyKey, bizType, null, IdempotencyStatus.IN_PROGRESS, now, now);
    }

    public static IdempotencyRecord restore(Long id,
                                            String tenantId,
                                            String idempotencyKey,
                                            String bizType,
                                            String bizId,
                                            IdempotencyStatus status,
                                            Instant createdAt,
                                            Instant updatedAt) {
        return new IdempotencyRecord(id, tenantId, idempotencyKey, bizType, bizId, status, createdAt, updatedAt);
    }

    public void markSuccess(String bizId) {
        this.bizId = bizId;
        this.status = IdempotencyStatus.SUCCESS;
        this.updatedAt = Instant.now();
    }

    public void markFailed() {
        this.status = IdempotencyStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public void restart() {
        this.status = IdempotencyStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getBizType() {
        return bizType;
    }

    public String getBizId() {
        return bizId;
    }

    public IdempotencyStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
