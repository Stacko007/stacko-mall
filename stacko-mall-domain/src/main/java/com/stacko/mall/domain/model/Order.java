package com.stacko.mall.domain.model;

import com.stacko.mall.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Order {
    private final OrderId id;
    private final String tenantId;
    private final String buyerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingCarrier;
    private String trackingNo;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant shippedAt;
    private Instant completedAt;

    private Order(OrderId id,
                  String tenantId,
                  String buyerId,
                  List<OrderItem> items,
                  OrderStatus status,
                  BigDecimal totalAmount,
                  String shippingCarrier,
                  String trackingNo,
                  Instant createdAt,
                  Instant updatedAt,
                  Instant shippedAt,
                  Instant completedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.buyerId = Objects.requireNonNull(buyerId, "buyerId");
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
        this.status = Objects.requireNonNull(status, "status");
        this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount");
        this.shippingCarrier = shippingCarrier;
        this.trackingNo = trackingNo;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        this.shippedAt = shippedAt;
        this.completedAt = completedAt;
    }

    public static Order create(String tenantId, String buyerId, List<OrderItem> items) {
        Instant now = Instant.now();
        BigDecimal total = items.stream()
                .map(OrderItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Order(OrderId.newId(), tenantId, buyerId, items, OrderStatus.CREATED, total, null, null, now, now, null, null);
    }

    public static Order restore(OrderId id,
                                String tenantId,
                                String buyerId,
                                List<OrderItem> items,
                                OrderStatus status,
                                BigDecimal totalAmount,
                                String shippingCarrier,
                                String trackingNo,
                                Instant createdAt,
                                Instant updatedAt,
                                Instant shippedAt,
                                Instant completedAt) {
        return new Order(id, tenantId, buyerId, items, status, totalAmount, shippingCarrier, trackingNo, createdAt, updatedAt, shippedAt, completedAt);
    }

    public void pay() {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Order not in created state");
        }
        status = OrderStatus.PAID;
        updatedAt = Instant.now();
    }

    public void ship(String carrier, String trackingNo) {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Order not paid");
        }
        this.shippingCarrier = Objects.requireNonNull(carrier, "carrier");
        this.trackingNo = Objects.requireNonNull(trackingNo, "trackingNo");
        this.shippedAt = Instant.now();
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        confirm();
    }

    public void cancel() {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Order cannot be cancelled");
        }
        status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void close() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.COMPLETED
                || status == OrderStatus.CANCELLED || status == OrderStatus.CLOSED) {
            throw new IllegalStateException("Order cannot be closed");
        }
        status = OrderStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    public void confirm() {
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Order not shipped");
        }
        this.completedAt = Instant.now();
        this.status = OrderStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public OrderId getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getShippingCarrier() {
        return shippingCarrier;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
