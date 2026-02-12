package com.stacko.mall.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stacko.mall.domain.model.OrderId;
import com.stacko.mall.domain.model.Payment;
import com.stacko.mall.domain.model.PaymentId;
import com.stacko.mall.domain.repository.PaymentRepository;
import com.stacko.mall.infra.dao.PaymentMapper;
import com.stacko.mall.infra.po.PaymentEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MybatisPaymentRepository implements PaymentRepository {
    private final PaymentMapper paymentMapper;

    public MybatisPaymentRepository(PaymentMapper paymentMapper) {
        this.paymentMapper = paymentMapper;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentEntity.fromDomain(payment);
        PaymentEntity existing = paymentMapper.selectById(entity.getId());
        if (existing == null) {
            paymentMapper.insert(entity);
            return payment;
        }
        if (!Objects.equals(existing.getTenantId(), entity.getTenantId())) {
            throw new IllegalStateException("Payment tenant mismatch");
        }
        paymentMapper.updateById(entity);
        return payment;
    }

    @Override
    public Optional<Payment> findById(String tenantId, PaymentId id) {
        LambdaQueryWrapper<PaymentEntity> query = new LambdaQueryWrapper<>();
        query.eq(PaymentEntity::getId, id.value())
                .eq(PaymentEntity::getTenantId, tenantId);
        return Optional.ofNullable(paymentMapper.selectOne(query))
                .map(PaymentEntity::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(String tenantId, OrderId orderId) {
        LambdaQueryWrapper<PaymentEntity> query = new LambdaQueryWrapper<>();
        query.eq(PaymentEntity::getOrderId, orderId.value())
                .eq(PaymentEntity::getTenantId, tenantId);
        return Optional.ofNullable(paymentMapper.selectOne(query))
                .map(PaymentEntity::toDomain);
    }

    @Override
    public List<Payment> listByTenant(String tenantId) {
        LambdaQueryWrapper<PaymentEntity> query = new LambdaQueryWrapper<>();
        query.eq(PaymentEntity::getTenantId, tenantId)
                .orderByDesc(PaymentEntity::getUpdatedAt);
        return paymentMapper.selectList(query).stream()
                .map(PaymentEntity::toDomain)
                .toList();
    }
}
