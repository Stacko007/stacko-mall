package com.stacko.mall.infra.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stacko.mall.domain.enums.PaymentChannel;
import com.stacko.mall.domain.enums.PaymentStatus;
import com.stacko.mall.domain.model.OrderId;
import com.stacko.mall.domain.model.Payment;
import com.stacko.mall.domain.model.PaymentId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@TableName("mall_payment")
public class PaymentEntity {
    @TableId
    private String id;
    private String tenantId;
    private String orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentChannel channel;
    private String tradeNo;
    private String rawCallback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentEntity fromDomain(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId().value());
        entity.setTenantId(payment.getTenantId());
        entity.setOrderId(payment.getOrderId().value());
        entity.setAmount(payment.getAmount());
        entity.setStatus(payment.getStatus());
        entity.setChannel(payment.getChannel());
        entity.setTradeNo(payment.getTradeNo());
        entity.setRawCallback(payment.getRawCallback());
        entity.setCreatedAt(toLocalDateTime(payment.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(payment.getUpdatedAt()));
        return entity;
    }

    public Payment toDomain() {
        return Payment.restore(
                new PaymentId(id),
                tenantId,
                new OrderId(orderId),
                amount,
                status,
                channel,
                tradeNo,
                rawCallback,
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public PaymentChannel getChannel() {
        return channel;
    }

    public void setChannel(PaymentChannel channel) {
        this.channel = channel;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getRawCallback() {
        return rawCallback;
    }

    public void setRawCallback(String rawCallback) {
        this.rawCallback = rawCallback;
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
