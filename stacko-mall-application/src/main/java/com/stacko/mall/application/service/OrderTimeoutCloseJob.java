package com.stacko.mall.application.service;

import com.stacko.mall.domain.enums.OrderStatus;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.domain.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
public class OrderTimeoutCloseJob {
    private final OrderRepository orderRepository;
    private final OrderApplicationService orderApplicationService;
    private final boolean enabled;
    private final int timeoutMinutes;
    private final int batchSize;

    public OrderTimeoutCloseJob(OrderRepository orderRepository,
                                OrderApplicationService orderApplicationService,
                                @Value("${order.timeout.job-enabled:true}") boolean enabled,
                                @Value("${order.timeout.minutes:30}") int timeoutMinutes,
                                @Value("${order.timeout.batch-size:200}") int batchSize) {
        this.orderRepository = orderRepository;
        this.orderApplicationService = orderApplicationService;
        this.enabled = enabled;
        this.timeoutMinutes = timeoutMinutes;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${order.timeout.fixed-delay-ms:60000}")
    public void closeTimeoutOrders() {
        if (!enabled) {
            return;
        }
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(timeoutMinutes));
        List<Order> orders = orderRepository.listByStatusCreatedBefore(OrderStatus.CREATED, cutoff, batchSize);
        if (orders.isEmpty()) {
            return;
        }
        for (Order order : orders) {
            try {
                orderApplicationService.closeForTimeout(order.getTenantId(), order.getId().value());
            } catch (RuntimeException ex) {
                log.warn("Order close (timeout) failed. tenantId={}, orderId={}, reason={}",
                        order.getTenantId(), order.getId().value(), ex.getMessage());
            }
        }
        log.info("Order close (timeout) done. total={}, cutoff={}", orders.size(), cutoff);
    }
}
