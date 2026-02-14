package com.stacko.mall.interfaces.web.view;

import com.stacko.mall.domain.enums.AfterSalesStatus;
import com.stacko.mall.domain.enums.AfterSalesType;
import com.stacko.mall.domain.model.AfterSales;

import java.time.Instant;

public class AfterSalesResponse {
    private String id;
    private String orderId;
    private String paymentId;
    private AfterSalesType type;
    private String reason;
    private AfterSalesStatus status;
    private String remark;
    private Instant createdAt;
    private Instant updatedAt;

    public static AfterSalesResponse from(AfterSales afterSales) {
        AfterSalesResponse response = new AfterSalesResponse();
        response.setId(afterSales.getId().value());
        response.setOrderId(afterSales.getOrderId().value());
        response.setPaymentId(afterSales.getPaymentId() == null ? null : afterSales.getPaymentId().value());
        response.setType(afterSales.getType());
        response.setReason(afterSales.getReason());
        response.setStatus(afterSales.getStatus());
        response.setRemark(afterSales.getRemark());
        response.setCreatedAt(afterSales.getCreatedAt());
        response.setUpdatedAt(afterSales.getUpdatedAt());
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

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public AfterSalesType getType() {
        return type;
    }

    public void setType(AfterSalesType type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AfterSalesStatus getStatus() {
        return status;
    }

    public void setStatus(AfterSalesStatus status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
