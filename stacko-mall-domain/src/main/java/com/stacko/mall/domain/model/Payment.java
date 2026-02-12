package com.stacko.mall.domain.model;

import com.stacko.mall.domain.enums.PaymentChannel;
import com.stacko.mall.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Payment {
    private final PaymentId id;
    private final String tenantId;
    private final OrderId orderId;
    private final BigDecimal amount;
    private PaymentStatus status;
    private final PaymentChannel channel;
    private String tradeNo;
    private String rawCallback;
    private Instant createdAt;
    private Instant updatedAt;

    private Payment(PaymentId id,
                    String tenantId,
                    OrderId orderId,
                    BigDecimal amount,
                    PaymentStatus status,
                    PaymentChannel channel,
                    String tradeNo,
                    String rawCallback,
                    Instant createdAt,
                    Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.orderId = Objects.requireNonNull(orderId, "orderId");
        this.amount = Objects.requireNonNull(amount, "amount");
        this.status = Objects.requireNonNull(status, "status");
        this.channel = Objects.requireNonNull(channel, "channel");
        this.tradeNo = tradeNo;
        this.rawCallback = rawCallback;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static Payment create(String tenantId, OrderId orderId, BigDecimal amount, PaymentChannel channel) {
        Instant now = Instant.now();
        return new Payment(PaymentId.newId(), tenantId, orderId, amount, PaymentStatus.CREATED, channel, null, null, now, now);
    }

    public static Payment restore(PaymentId id,
                                  String tenantId,
                                  OrderId orderId,
                                  BigDecimal amount,
                                  PaymentStatus status,
                                  PaymentChannel channel,
                                  String tradeNo,
                                  String rawCallback,
                                  Instant createdAt,
                                  Instant updatedAt) {
        return new Payment(id, tenantId, orderId, amount, status, channel, tradeNo, rawCallback, createdAt, updatedAt);
    }

    public void markPaid(String tradeNo) {
        if (status != PaymentStatus.CREATED) {
            throw new IllegalStateException("Payment not in created state");
        }
        this.tradeNo = tradeNo;
        this.status = PaymentStatus.PAID;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String tradeNo) {
        if (status != PaymentStatus.CREATED) {
            throw new IllegalStateException("Payment not in created state");
        }
        this.tradeNo = tradeNo;
        this.status = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public void refund() {
        if (status != PaymentStatus.PAID) {
            throw new IllegalStateException("Payment not paid");
        }
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = Instant.now();
    }

    public void updateRawCallback(String rawCallback) {
        this.rawCallback = rawCallback;
        this.updatedAt = Instant.now();
    }

    public PaymentId getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentChannel getChannel() {
        return channel;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public String getRawCallback() {
        return rawCallback;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
