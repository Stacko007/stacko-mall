package com.stacko.mall.interfaces.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class OrderShipRequest {
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9 _\\-\\u4E00-\\u9FFF]+$")
    @Size(max = 32)
    private String carrier;
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9-]+$")
    @Size(max = 64)
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
