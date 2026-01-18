package com.stacko.mall.interfaces.web.dto;

import jakarta.validation.constraints.NotBlank;

public class OrderShipRequest {
    @NotBlank
    private String carrier;
    @NotBlank
    private String trackingNo;

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }
}
