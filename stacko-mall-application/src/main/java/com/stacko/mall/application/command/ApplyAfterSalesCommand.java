package com.stacko.mall.application.command;

import com.stacko.mall.domain.enums.AfterSalesType;

public class ApplyAfterSalesCommand {
    private String tenantId;
    private String orderId;
    private AfterSalesType type;
    private String reason;

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
}
