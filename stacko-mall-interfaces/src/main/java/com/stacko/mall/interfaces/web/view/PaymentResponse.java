package com.stacko.mall.interfaces.web.view;

import com.stacko.mall.domain.enums.PaymentChannel;
import com.stacko.mall.domain.enums.PaymentStatus;
import com.stacko.mall.domain.model.Payment;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentResponse {
    private String id;
    private String orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentChannel channel;
    private String tradeNo;
    private String rawCallback;
    private Instant createdAt;
    private Instant updatedAt;

    public static PaymentResponse from(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId().value());
        response.setOrderId(payment.getOrderId().value());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setChannel(payment.getChannel());
        response.setTradeNo(payment.getTradeNo());
        response.setRawCallback(payment.getRawCallback());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
