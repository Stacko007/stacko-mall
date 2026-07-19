package com.stacko.mall.interfaces.web.view;

import com.stacko.mall.domain.model.Stock;

import java.time.Instant;

public class StockResponse {
    private String productId;
    private String productName;
    private int quantity;
    private Instant updatedAt;

    public static StockResponse from(Stock stock) {
        StockResponse response = new StockResponse();
        response.setProductId(stock.getProductId().value());
        response.setQuantity(stock.getQuantity());
        response.setUpdatedAt(stock.getUpdatedAt());
        return response;
    }

    public static StockResponse from(Stock stock, String productName) {
        StockResponse response = from(stock);
        response.setProductName(productName);
        return response;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
