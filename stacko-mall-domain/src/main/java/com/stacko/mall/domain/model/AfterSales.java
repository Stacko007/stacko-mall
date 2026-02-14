package com.stacko.mall.domain.model;

import com.stacko.mall.domain.enums.AfterSalesStatus;
import com.stacko.mall.domain.enums.AfterSalesType;

import java.time.Instant;
import java.util.Objects;

public class AfterSales {
    private final AfterSalesId id;
    private final String tenantId;
    private final OrderId orderId;
    private final PaymentId paymentId;
    private final AfterSalesType type;
    private final String reason;
    private AfterSalesStatus status;
    private String remark;
    private Instant createdAt;
    private Instant updatedAt;

    private AfterSales(AfterSalesId id,
                       String tenantId,
                       OrderId orderId,
                       PaymentId paymentId,
                       AfterSalesType type,
                       String reason,
                       AfterSalesStatus status,
                       String remark,
                       Instant createdAt,
                       Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.orderId = Objects.requireNonNull(orderId, "orderId");
        this.paymentId = paymentId;
        this.type = Objects.requireNonNull(type, "type");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.status = Objects.requireNonNull(status, "status");
        this.remark = remark;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static AfterSales apply(String tenantId,
                                   OrderId orderId,
                                   PaymentId paymentId,
                                   AfterSalesType type,
                                   String reason) {
        Instant now = Instant.now();
        return new AfterSales(
                AfterSalesId.newId(),
                tenantId,
                orderId,
                paymentId,
                type,
                reason,
                AfterSalesStatus.APPLIED,
                null,
                now,
                now
        );
    }

    public static AfterSales restore(AfterSalesId id,
                                     String tenantId,
                                     OrderId orderId,
                                     PaymentId paymentId,
                                     AfterSalesType type,
                                     String reason,
                                     AfterSalesStatus status,
                                     String remark,
                                     Instant createdAt,
                                     Instant updatedAt) {
        return new AfterSales(id, tenantId, orderId, paymentId, type, reason, status, remark, createdAt, updatedAt);
    }

    public void approve(String remark) {
        if (status != AfterSalesStatus.APPLIED) {
            throw new IllegalStateException("After-sales not in applied state");
        }
        this.status = AfterSalesStatus.APPROVED;
        this.remark = remark;
        this.updatedAt = Instant.now();
    }

    public void reject(String remark) {
        if (status != AfterSalesStatus.APPLIED) {
            throw new IllegalStateException("After-sales not in applied state");
        }
        this.status = AfterSalesStatus.REJECTED;
        this.remark = remark;
        this.updatedAt = Instant.now();
    }

    public void refund(String remark) {
        if (status != AfterSalesStatus.APPROVED) {
            throw new IllegalStateException("After-sales not approved");
        }
        this.status = AfterSalesStatus.REFUNDED;
        this.remark = remark;
        this.updatedAt = Instant.now();
    }

    public void cancel(String remark) {
        if (status != AfterSalesStatus.APPLIED) {
            throw new IllegalStateException("After-sales cannot be cancelled");
        }
        this.status = AfterSalesStatus.CANCELLED;
        this.remark = remark;
        this.updatedAt = Instant.now();
    }

    public AfterSalesId getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public PaymentId getPaymentId() {
        return paymentId;
    }

    public AfterSalesType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public AfterSalesStatus getStatus() {
        return status;
    }

    public String getRemark() {
        return remark;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
