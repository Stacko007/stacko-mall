package com.stacko.mall.application.command;

public class RefundAfterSalesCommand {
    private String tenantId;
    private String afterSalesId;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
