package com.stacko.mall.interfaces.web.dto;

import jakarta.validation.constraints.NotNull;

public class AfterSalesReviewRequest {
    @NotNull
    private Boolean approved;
    private String remark;

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
