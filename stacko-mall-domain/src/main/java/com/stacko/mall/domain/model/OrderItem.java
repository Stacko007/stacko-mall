package com.stacko.mall.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {
    private final ProductId productId;
    private final String productName;
    private final BigDecimal price;
    private final int quantity;
    private final BigDecimal amount;

    public OrderItem(ProductId productId, String productName, BigDecimal price, int quantity) {
        this.productId = Objects.requireNonNull(productId, "productId");
        this.productName = Objects.requireNonNull(productName, "productName");
        this.price = Objects.requireNonNull(price, "price");
        this.quantity = quantity;
        this.amount = price.multiply(BigDecimal.valueOf(quantity));
    }

    public ProductId getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
