package com.stacko.mall.infra.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stacko.mall.domain.enums.OrderStatus;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.domain.model.OrderId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@TableName("mall_order")
public class OrderEntity {
    @TableId
    private String id;
    private String tenantId;
    private String buyerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingCarrier;
    private String trackingNo;
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;

    public static OrderEntity fromDomain(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId().value());
        entity.setTenantId(order.getTenantId());
        entity.setBuyerId(order.getBuyerId());
        entity.setStatus(order.getStatus());
        entity.setTotalAmount(order.getTotalAmount());
        entity.setShippingCarrier(order.getShippingCarrier());
        entity.setTrackingNo(order.getTrackingNo());
        entity.setReceiverName(order.getReceiverName());
        entity.setReceiverPhone(order.getReceiverPhone());
        entity.setReceiverProvince(order.getReceiverProvince());
        entity.setReceiverCity(order.getReceiverCity());
        entity.setReceiverDistrict(order.getReceiverDistrict());
        entity.setReceiverAddress(order.getReceiverAddress());
        entity.setCreatedAt(toLocalDateTime(order.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(order.getUpdatedAt()));
        entity.setShippedAt(toLocalDateTime(order.getShippedAt()));
        entity.setCompletedAt(toLocalDateTime(order.getCompletedAt()));
        return entity;
    }

    public Order toDomain(List<com.stacko.mall.domain.model.OrderItem> items) {
        return Order.restore(
                new OrderId(id),
                tenantId,
                buyerId,
                items,
                status,
                totalAmount,
                shippingCarrier,
                trackingNo,
                receiverName,
                receiverPhone,
                receiverProvince,
                receiverCity,
                receiverDistrict,
                receiverAddress,
                toInstant(createdAt),
                toInstant(updatedAt),
                toInstant(shippedAt),
                toInstant(completedAt)
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

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingCarrier() {
        return shippingCarrier;
    }

    public void setShippingCarrier(String shippingCarrier) {
        this.shippingCarrier = shippingCarrier;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverProvince() {
        return receiverProvince;
    }

    public void setReceiverProvince(String receiverProvince) {
        this.receiverProvince = receiverProvince;
    }

    public String getReceiverCity() {
        return receiverCity;
    }

    public void setReceiverCity(String receiverCity) {
        this.receiverCity = receiverCity;
    }

    public String getReceiverDistrict() {
        return receiverDistrict;
    }

    public void setReceiverDistrict(String receiverDistrict) {
        this.receiverDistrict = receiverDistrict;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
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

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
