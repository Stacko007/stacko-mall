package com.stacko.mall.interfaces.web.dto;

import jakarta.validation.constraints.Min;

public class StockSetRequest {
    @Min(0)
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
