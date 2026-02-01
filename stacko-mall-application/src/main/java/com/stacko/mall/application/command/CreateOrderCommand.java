package com.stacko.mall.application.command;

import java.util.List;

public class CreateOrderCommand {
    private String tenantId;
    private String buyerId;
    private List<OrderItemCommand> items;
    private String idempotencyKey;

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

    public List<OrderItemCommand> getItems() {
        return items;
    }

    public void setItems(List<OrderItemCommand> items) {
        this.items = items;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
