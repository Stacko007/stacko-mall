package com.stacko.mall.domain.model;

import java.time.Instant;
import java.util.Objects;

public class ShippingAddress {
    private final ShippingAddressId id;
    private final String tenantId;
    private final String buyerId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private boolean defaultAddress;
    private Instant createdAt;
    private Instant updatedAt;

    private ShippingAddress(ShippingAddressId id,
                            String tenantId,
                            String buyerId,
                            String receiverName,
                            String receiverPhone,
                            String province,
                            String city,
                            String district,
                            String detailAddress,
                            boolean defaultAddress,
                            Instant createdAt,
                            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.buyerId = Objects.requireNonNull(buyerId, "buyerId");
        this.receiverName = requireText(receiverName, "receiverName");
        this.receiverPhone = requireText(receiverPhone, "receiverPhone");
        this.province = requireText(province, "province");
        this.city = requireText(city, "city");
        this.district = district;
        this.detailAddress = requireText(detailAddress, "detailAddress");
        this.defaultAddress = defaultAddress;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static ShippingAddress create(String tenantId,
                                         String buyerId,
                                         String receiverName,
                                         String receiverPhone,
                                         String province,
                                         String city,
                                         String district,
                                         String detailAddress,
                                         boolean defaultAddress) {
        Instant now = Instant.now();
        return new ShippingAddress(ShippingAddressId.newId(), tenantId, buyerId, receiverName, receiverPhone,
                province, city, district, detailAddress, defaultAddress, now, now);
    }

    public static ShippingAddress restore(ShippingAddressId id,
                                          String tenantId,
                                          String buyerId,
                                          String receiverName,
                                          String receiverPhone,
                                          String province,
                                          String city,
                                          String district,
                                          String detailAddress,
                                          boolean defaultAddress,
                                          Instant createdAt,
                                          Instant updatedAt) {
        return new ShippingAddress(id, tenantId, buyerId, receiverName, receiverPhone, province, city,
                district, detailAddress, defaultAddress, createdAt, updatedAt);
    }

    public void update(String receiverName,
                       String receiverPhone,
                       String province,
                       String city,
                       String district,
                       String detailAddress,
                       boolean defaultAddress) {
        this.receiverName = requireText(receiverName, "receiverName");
        this.receiverPhone = requireText(receiverPhone, "receiverPhone");
        this.province = requireText(province, "province");
        this.city = requireText(city, "city");
        this.district = district;
        this.detailAddress = requireText(detailAddress, "detailAddress");
        this.defaultAddress = defaultAddress;
        this.updatedAt = Instant.now();
    }

    public void markDefault() {
        this.defaultAddress = true;
        this.updatedAt = Instant.now();
    }

    public void clearDefault() {
        this.defaultAddress = false;
        this.updatedAt = Instant.now();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " required");
        }
        return value.trim();
    }

    public ShippingAddressId getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
