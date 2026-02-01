package com.stacko.mall.infra.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stacko.mall.domain.enums.IdempotencyStatus;
import com.stacko.mall.domain.model.IdempotencyRecord;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@TableName("mall_idempotency")
public class IdempotencyEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String tenantId;
    private String idempotencyKey;
    private String bizType;
    private String bizId;
    private IdempotencyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static IdempotencyEntity fromDomain(IdempotencyRecord record) {
        IdempotencyEntity entity = new IdempotencyEntity();
        entity.setId(record.getId());
        entity.setTenantId(record.getTenantId());
        entity.setIdempotencyKey(record.getIdempotencyKey());
        entity.setBizType(record.getBizType());
        entity.setBizId(record.getBizId());
        entity.setStatus(record.getStatus());
        entity.setCreatedAt(toLocalDateTime(record.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(record.getUpdatedAt()));
        return entity;
    }

    public IdempotencyRecord toDomain() {
        return IdempotencyRecord.restore(
                id,
                tenantId,
                idempotencyKey,
                bizType,
                bizId,
                status,
                toInstant(createdAt),
                toInstant(updatedAt)
        );
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static Instant toInstant(LocalDateTime time) {
        return time == null ? null : time.toInstant(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public IdempotencyStatus getStatus() {
        return status;
    }

    public void setStatus(IdempotencyStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
