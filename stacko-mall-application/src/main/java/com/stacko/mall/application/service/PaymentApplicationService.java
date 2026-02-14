package com.stacko.mall.application.service;

import com.stacko.mall.application.command.PaymentCallbackCommand;
import com.stacko.mall.domain.enums.OrderStatus;
import com.stacko.mall.domain.enums.PaymentChannel;
import com.stacko.mall.domain.enums.PaymentStatus;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.domain.model.OrderId;
import com.stacko.mall.domain.model.Payment;
import com.stacko.mall.domain.model.PaymentId;
import com.stacko.mall.domain.repository.OrderRepository;
import com.stacko.mall.domain.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class PaymentApplicationService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final IdempotencyService idempotencyService;
    private final String callbackSecret;

    public PaymentApplicationService(PaymentRepository paymentRepository,
                                     OrderRepository orderRepository,
                                     IdempotencyService idempotencyService,
                                     @Value("${payment.mock.callback-secret:stacko-mall-callback-secret}") String callbackSecret) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.idempotencyService = idempotencyService;
        this.callbackSecret = callbackSecret;
    }

    @Transactional
    public Payment createOrPayMock(String tenantId, Order order) {
        Payment payment = paymentRepository.findByOrderId(tenantId, order.getId())
                .orElseGet(() -> paymentRepository.save(Payment.create(
                        tenantId,
                        order.getId(),
                        order.getTotalAmount(),
                        PaymentChannel.MOCK
                )));

        if (payment.getStatus() == PaymentStatus.CREATED) {
            payment.markPaid(buildTradeNo(order.getId().value()));
            return paymentRepository.save(payment);
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            return payment;
        }
        throw new IllegalStateException("Payment cannot be processed");
    }

    @Transactional
    public Payment handleCallback(PaymentCallbackCommand command) {
        ensureCallbackRequest(command);
        verifySignature(command);

        var acquire = idempotencyService.acquire(command.getTenantId(), command.getCallbackId(), "PAYMENT_CALLBACK");
        var idempotency = acquire.getRecord();
        if (idempotencyService.isSuccess(idempotency)) {
            String paymentId = idempotency.getBizId();
            if (paymentId == null) {
                throw new IllegalStateException("Idempotency record missing payment id");
            }
            return paymentRepository.findById(command.getTenantId(), new PaymentId(paymentId))
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        }
        if (idempotencyService.isInProgress(idempotency) && !acquire.isNewlyCreated()) {
            throw new IllegalStateException("Request in progress");
        }
        if (!acquire.isNewlyCreated()) {
            idempotencyService.restart(idempotency);
        }

        Order order = orderRepository.findById(command.getTenantId(), new OrderId(command.getOrderId()))
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        Payment payment = paymentRepository.findByOrderId(command.getTenantId(), order.getId())
                .orElseGet(() -> Payment.create(command.getTenantId(), order.getId(), order.getTotalAmount(), PaymentChannel.MOCK));

        try {
            applyCallback(payment, order, command);
            Payment savedPayment = paymentRepository.save(payment);
            orderRepository.save(order);
            idempotencyService.markSuccess(idempotency, savedPayment.getId().value());
            return savedPayment;
        } catch (RuntimeException ex) {
            idempotencyService.markFailed(idempotency);
            throw ex;
        }
    }

    public Payment get(String tenantId, String paymentId) {
        return paymentRepository.findById(tenantId, new PaymentId(paymentId))
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }

    private void applyCallback(Payment payment, Order order, PaymentCallbackCommand command) {
        payment.updateRawCallback(command.getRawPayload());
        String callbackStatus = command.getStatus().trim().toUpperCase();
        if ("PAID".equals(callbackStatus)) {
            if (payment.getStatus() == PaymentStatus.CREATED) {
                payment.markPaid(command.getTradeNo());
            } else if (payment.getStatus() != PaymentStatus.PAID) {
                throw new IllegalStateException("Payment status does not allow paid callback");
            }
            if (order.getStatus() == OrderStatus.CREATED) {
                order.pay();
            }
            return;
        }
        if ("FAILED".equals(callbackStatus)) {
            if (payment.getStatus() == PaymentStatus.CREATED) {
                payment.markFailed(command.getTradeNo());
                return;
            }
            if (payment.getStatus() == PaymentStatus.FAILED) {
                return;
            }
            throw new IllegalStateException("Payment status does not allow failed callback");
        }
        throw new IllegalArgumentException("Unsupported callback status");
    }

    private String buildTradeNo(String orderId) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return "MOCK-" + orderId + "-" + suffix;
    }

    private void ensureCallbackRequest(PaymentCallbackCommand command) {
        if (isBlank(command.getTenantId())) {
            throw new IllegalArgumentException("Tenant id required");
        }
        if (isBlank(command.getCallbackId())) {
            throw new IllegalArgumentException("Callback id required");
        }
        if (isBlank(command.getOrderId())) {
            throw new IllegalArgumentException("Order id required");
        }
        if (isBlank(command.getTradeNo())) {
            throw new IllegalArgumentException("Trade no required");
        }
        if (isBlank(command.getStatus())) {
            throw new IllegalArgumentException("Status required");
        }
        if (isBlank(command.getSignature())) {
            throw new IllegalArgumentException("Signature required");
        }
    }

    private void verifySignature(PaymentCallbackCommand command) {
        String payload = command.getCallbackId() + "|" + command.getOrderId() + "|" + command.getTradeNo() + "|" + command.getStatus();
        String expected = hmacSha256Hex(payload, callbackSecret);
        if (!expected.equalsIgnoreCase(command.getSignature())) {
            throw new SecurityException("Invalid callback signature");
        }
    }

    private String hmacSha256Hex(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to verify signature", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
