package com.stacko.mall.infra.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stacko.mall.domain.enums.AfterSalesStatus;
import com.stacko.mall.domain.enums.AfterSalesType;
import com.stacko.mall.domain.model.AfterSales;
import com.stacko.mall.domain.model.AfterSalesId;
import com.stacko.mall.domain.model.OrderId;
import com.stacko.mall.domain.model.PaymentId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@TableName("mall_after_sales")
public class AfterSalesEntity {
    @TableId
    private String id;
    private String tenantId;
    private String orderId;
    private String paymentId;
    private AfterSalesType type;
    private String reason;
    private AfterSalesStatus status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AfterSalesEntity fromDomain(AfterSales afterSales) {
        AfterSalesEntity entity = new AfterSalesEntity();
        entity.setId(afterSales.getId().value());
        entity.setTenantId(afterSales.getTenantId());
        entity.setOrderId(afterSales.getOrderId().value());
        entity.setPaymentId(afterSales.getPaymentId() == null ? null : afterSales.getPaymentId().value());
        entity.setType(afterSales.getType());
        entity.setReason(afterSales.getReason());
        entity.setStatus(afterSales.getStatus());
        entity.setRemark(afterSales.getRemark());
        entity.setCreatedAt(toLocalDateTime(afterSales.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(afterSales.getUpdatedAt()));
        return entity;
    }

    public AfterSales toDomain() {
        return AfterSales.restore(
                new AfterSalesId(id),
                tenantId,
                new OrderId(orderId),
                paymentId == null ? null : new PaymentId(paymentId),
                type,
                reason,
                status,
                remark,
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
