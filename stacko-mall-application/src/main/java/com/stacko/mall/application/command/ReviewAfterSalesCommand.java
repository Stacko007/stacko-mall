package com.stacko.mall.application.command;

public class ReviewAfterSalesCommand {
    private String tenantId;
    private String afterSalesId;
    private boolean approved;
    private String remark;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAfterSalesId() {
        return afterSalesId;
    }

    public void setAfterSalesId(String afterSalesId) {
        this.afterSalesId = afterSalesId;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
