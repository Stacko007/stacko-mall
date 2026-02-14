package com.stacko.mall.interfaces.web.dto;

import com.stacko.mall.domain.enums.AfterSalesType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AfterSalesApplyRequest {
    @NotBlank
    private String orderId;
    @NotNull
    private AfterSalesType type;
    @NotBlank
    private String reason;

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
