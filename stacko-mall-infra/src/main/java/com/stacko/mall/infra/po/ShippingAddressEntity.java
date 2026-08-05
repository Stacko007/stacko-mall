package com.stacko.mall.infra.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stacko.mall.domain.model.ShippingAddress;
import com.stacko.mall.domain.model.ShippingAddressId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@TableName("mall_shipping_address")
public class ShippingAddressEntity {
    @TableId
    private String id;
    private String tenantId;
    private String buyerId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Boolean defaultAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ShippingAddressEntity fromDomain(ShippingAddress address) {
        ShippingAddressEntity entity = new ShippingAddressEntity();
        entity.setId(address.getId().value());
        entity.setTenantId(address.getTenantId());
        entity.setBuyerId(address.getBuyerId());
        entity.setReceiverName(address.getReceiverName());
        entity.setReceiverPhone(address.getReceiverPhone());
        entity.setProvince(address.getProvince());
        entity.setCity(address.getCity());
        entity.setDistrict(address.getDistrict());
        entity.setDetailAddress(address.getDetailAddress());
        entity.setDefaultAddress(address.isDefaultAddress());
        entity.setCreatedAt(toLocalDateTime(address.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(address.getUpdatedAt()));
        return entity;
    }

    public ShippingAddress toDomain() {
        return ShippingAddress.restore(
                new ShippingAddressId(id),
                tenantId,
                buyerId,
                receiverName,
                receiverPhone,
                province,
                city,
                district,
                detailAddress,
                Boolean.TRUE.equals(defaultAddress),
                toInstant(createdAt),
                toInstant(updatedAt)
        );
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static Instant toInstant(LocalDateTime time) {
        return time == null ? null : time.toInstant(ZoneOffset.UTC);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public Boolean getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(Boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
