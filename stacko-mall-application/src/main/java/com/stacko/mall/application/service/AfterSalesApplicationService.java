package com.stacko.mall.application.service;

import com.stacko.mall.application.command.ApplyAfterSalesCommand;
import com.stacko.mall.application.command.RefundAfterSalesCommand;
import com.stacko.mall.application.command.ReviewAfterSalesCommand;
import com.stacko.mall.domain.model.AfterSales;
import com.stacko.mall.domain.model.AfterSalesId;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.domain.model.OrderId;
import com.stacko.mall.domain.model.Payment;
import com.stacko.mall.domain.repository.AfterSalesRepository;
import com.stacko.mall.domain.repository.OrderRepository;
import com.stacko.mall.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AfterSalesApplicationService {
    private final AfterSalesRepository afterSalesRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public AfterSalesApplicationService(AfterSalesRepository afterSalesRepository,
                                        OrderRepository orderRepository,
                                        PaymentRepository paymentRepository) {
        this.afterSalesRepository = afterSalesRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public AfterSales apply(ApplyAfterSalesCommand command) {
        ensureApplyRequest(command);
        Order order = orderRepository.findById(command.getTenantId(), new OrderId(command.getOrderId()))
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        Payment payment = paymentRepository.findByOrderId(command.getTenantId(), order.getId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        AfterSales afterSales = AfterSales.apply(
                command.getTenantId(),
                order.getId(),
                payment.getId(),
                command.getType(),
                command.getReason()
        );
        return afterSalesRepository.save(afterSales);
    }

    public AfterSales get(String tenantId, String afterSalesId) {
        return afterSalesRepository.findById(tenantId, new AfterSalesId(afterSalesId))
                .orElseThrow(() -> new IllegalArgumentException("After-sales not found"));
    }

    public List<AfterSales> listByTenant(String tenantId) {
        return afterSalesRepository.listByTenant(tenantId);
    }

    public List<AfterSales> listByOrderId(String tenantId, String orderId) {
        return afterSalesRepository.listByOrderId(tenantId, new OrderId(orderId));
    }

    @Transactional
    public AfterSales review(ReviewAfterSalesCommand command) {
        ensureReviewRequest(command);
        AfterSales afterSales = afterSalesRepository.findById(command.getTenantId(), new AfterSalesId(command.getAfterSalesId()))
                .orElseThrow(() -> new IllegalArgumentException("After-sales not found"));
        if (command.isApproved()) {
            afterSales.approve(command.getRemark());
        } else {
            afterSales.reject(command.getRemark());
        }
        return afterSalesRepository.save(afterSales);
    }

    @Transactional
    public AfterSales refund(RefundAfterSalesCommand command) {
        ensureRefundRequest(command);
        AfterSales afterSales = afterSalesRepository.findById(command.getTenantId(), new AfterSalesId(command.getAfterSalesId()))
                .orElseThrow(() -> new IllegalArgumentException("After-sales not found"));
        if (afterSales.getPaymentId() == null) {
            throw new IllegalStateException("After-sales payment id missing");
        }
        Payment payment = paymentRepository.findById(command.getTenantId(), afterSales.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        Order order = orderRepository.findById(command.getTenantId(), afterSales.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        payment.refund();
        if (order.getStatus() != com.stacko.mall.domain.enums.OrderStatus.CLOSED) {
            order.close();
        }
        afterSales.refund(command.getRemark());

        paymentRepository.save(payment);
        orderRepository.save(order);
        return afterSalesRepository.save(afterSales);
    }

    private void ensureApplyRequest(ApplyAfterSalesCommand command) {
        if (command.getTenantId() == null || command.getTenantId().isBlank()) {
            throw new IllegalArgumentException("Tenant id required");
        }
        if (command.getOrderId() == null || command.getOrderId().isBlank()) {
            throw new IllegalArgumentException("Order id required");
        }
        if (command.getType() == null) {
            throw new IllegalArgumentException("After-sales type required");
        }
        if (command.getReason() == null || command.getReason().isBlank()) {
            throw new IllegalArgumentException("Reason required");
        }
    }

    private void ensureReviewRequest(ReviewAfterSalesCommand command) {
        if (command.getTenantId() == null || command.getTenantId().isBlank()) {
            throw new IllegalArgumentException("Tenant id required");
        }
        if (command.getAfterSalesId() == null || command.getAfterSalesId().isBlank()) {
            throw new IllegalArgumentException("After-sales id required");
        }
    }

    private void ensureRefundRequest(RefundAfterSalesCommand command) {
        if (command.getTenantId() == null || command.getTenantId().isBlank()) {
            throw new IllegalArgumentException("Tenant id required");
        }
        if (command.getAfterSalesId() == null || command.getAfterSalesId().isBlank()) {
            throw new IllegalArgumentException("After-sales id required");
        }
    }
}
