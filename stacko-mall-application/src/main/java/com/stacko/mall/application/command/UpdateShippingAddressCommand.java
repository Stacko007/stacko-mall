package com.stacko.mall.application.command;

public class UpdateShippingAddressCommand extends CreateShippingAddressCommand {
    private String addressId;

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }
}
