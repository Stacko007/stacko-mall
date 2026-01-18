package com.stacko.mall.infra.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stacko.mall.domain.model.OrderItem;
import com.stacko.mall.domain.model.ProductId;

import java.math.BigDecimal;

@TableName("mall_order_item")
public class OrderItemEntity {
    @TableId
    private String id;
    private String orderId;
    private String productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal amount;

    public static OrderItemEntity fromDomain(String orderId, OrderItem item) {
        OrderItemEntity entity = new OrderItemEntity();
        entity.setId(java.util.UUID.randomUUID().toString());
        entity.setOrderId(orderId);
        entity.setProductId(item.getProductId().value());
        entity.setProductName(item.getProductName());
        entity.setPrice(item.getPrice());
        entity.setQuantity(item.getQuantity());
        entity.setAmount(item.getAmount());
        return entity;
    }

    public OrderItem toDomain() {
        return new OrderItem(new ProductId(productId), productName, price, quantity == null ? 0 : quantity);
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
