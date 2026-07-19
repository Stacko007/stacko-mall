package com.stacko.mall.interfaces.web.view;

import com.stacko.mall.domain.enums.OrderStatus;
import com.stacko.mall.domain.model.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {
    private String id;
    private String buyerId;
    private String buyerName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingCarrier;
    private String trackingNo;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant shippedAt;
    private Instant completedAt;
    private List<OrderItemResponse> items;

    public static OrderResponse from(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId().value());
        response.setBuyerId(order.getBuyerId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setShippingCarrier(order.getShippingCarrier());
        response.setTrackingNo(order.getTrackingNo());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setShippedAt(order.getShippedAt());
        response.setCompletedAt(order.getCompletedAt());
        response.setItems(order.getItems().stream().map(OrderItemResponse::from).toList());
        return response;
    }

    public static OrderResponse from(Order order, String buyerName) {
        OrderResponse response = from(order);
        response.setBuyerName(buyerName);
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
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

    public Instant getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(Instant shippedAt) {
        this.shippedAt = shippedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }
}
