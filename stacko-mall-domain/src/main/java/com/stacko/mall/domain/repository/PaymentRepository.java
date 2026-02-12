package com.stacko.mall.domain.repository;

import com.stacko.mall.domain.model.OrderId;
import com.stacko.mall.domain.model.Payment;
import com.stacko.mall.domain.model.PaymentId;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(String tenantId, PaymentId id);

    Optional<Payment> findByOrderId(String tenantId, OrderId orderId);

    List<Payment> listByTenant(String tenantId);
}
