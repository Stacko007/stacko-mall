package com.stacko.mall.application.service;

import com.stacko.mall.application.command.CancelOrderCommand;
import com.stacko.mall.application.command.CloseOrderCommand;
import com.stacko.mall.application.command.ConfirmReceiptCommand;
import com.stacko.mall.application.command.CreateOrderCommand;
import com.stacko.mall.application.command.OrderItemCommand;
import com.stacko.mall.application.command.PayOrderCommand;
import com.stacko.mall.application.command.ShipOrderCommand;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.domain.model.OrderId;
import com.stacko.mall.domain.model.OrderItem;
import com.stacko.mall.domain.model.ProductId;
import com.stacko.mall.domain.model.Stock;
import com.stacko.mall.domain.repository.OrderRepository;
import com.stacko.mall.domain.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class OrderApplicationService {
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final IdempotencyService idempotencyService;
    private final PaymentApplicationService paymentApplicationService;

    public OrderApplicationService(OrderRepository orderRepository,
                                   StockRepository stockRepository,
                                   IdempotencyService idempotencyService,
                                   PaymentApplicationService paymentApplicationService) {
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
        this.idempotencyService = idempotencyService;
        this.paymentApplicationService = paymentApplicationService;
    }

    @Transactional
    public Order create(CreateOrderCommand command) {
        ensureIdempotencyKey(command.getIdempotencyKey());
        var acquire = idempotencyService.acquire(command.getTenantId(), command.getIdempotencyKey(), "ORDER_CREATE");
        var idempotency = acquire.getRecord();
        if (idempotencyService.isSuccess(idempotency)) {
            String orderId = idempotency.getBizId();
            if (orderId == null) {
                throw new IllegalStateException("Idempotency record missing order id");
            }
            return orderRepository
                    .findById(command.getTenantId(), new OrderId(orderId))
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        }
        if (idempotencyService.isInProgress(idempotency) && !acquire.isNewlyCreated()) {
            throw new IllegalStateException("Request in progress");
        }
        if (!acquire.isNewlyCreated()) {
            idempotencyService.restart(idempotency);
        }
        List<OrderItem> items = command.getItems().stream()
                .map(this::toItem)
                .toList();
        Order order = Order.create(command.getTenantId(), command.getBuyerId(), items,
                command.getReceiverName(), command.getReceiverPhone(), command.getReceiverProvince(),
                command.getReceiverCity(), command.getReceiverDistrict(), command.getReceiverAddress());
        try {
            reserveStock(command.getTenantId(), items);
            Order saved = orderRepository.save(order);
            idempotencyService.markSuccess(idempotency, saved.getId().value());
            log.info("Order created. tenantId={}, orderId={}, status={}", command.getTenantId(), saved.getId().value(), saved.getStatus());
            return saved;
        } catch (RuntimeException ex) {
            idempotencyService.markFailed(idempotency);
            throw ex;
        }
    }

    @Transactional
    public Order pay(PayOrderCommand command) {
        ensureIdempotencyKey(command.getIdempotencyKey());
        var acquire = idempotencyService.acquire(command.getTenantId(), command.getIdempotencyKey(), "ORDER_PAY");
        var idempotency = acquire.getRecord();
        if (idempotencyService.isSuccess(idempotency)) {
            String orderId = idempotency.getBizId();
            if (orderId == null) {
                throw new IllegalStateException("Idempotency record missing order id");
            }
            return orderRepository
                    .findById(command.getTenantId(), new OrderId(orderId))
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        }
        if (idempotencyService.isInProgress(idempotency) && !acquire.isNewlyCreated()) {
            throw new IllegalStateException("Request in progress");
        }
        if (!acquire.isNewlyCreated()) {
            idempotencyService.restart(idempotency);
        }
        Order order = orderRepository
                .findById(command.getTenantId(), new OrderId(command.getOrderId()))
                .orElse(null);
        if (order == null) {
            log.warn("Order pay failed: order not found. tenantId={}, orderId={}", command.getTenantId(), command.getOrderId());
            throw new IllegalArgumentException("Order not found");
        }
        var before = order.getStatus();
        try {
            order.pay();
        } catch (RuntimeException ex) {
            log.warn("Order pay failed: invalid state. tenantId={}, orderId={}, status={}, reason={}",
                    command.getTenantId(), command.getOrderId(), before, ex.getMessage());
            throw ex;
        }
        try {
            paymentApplicationService.createOrPayMock(command.getTenantId(), order);
            Order saved = orderRepository.save(order);
            idempotencyService.markSuccess(idempotency, saved.getId().value());
            log.info("Order paid. tenantId={}, orderId={}, status={} -> {}", command.getTenantId(), command.getOrderId(), before, saved.getStatus());
            return saved;
        } catch (RuntimeException ex) {
            idempotencyService.markFailed(idempotency);
            throw ex;
        }
    }

    @Transactional
    public Order ship(ShipOrderCommand command) {
        Order order = orderRepository
                .findById(command.getTenantId(), new OrderId(command.getOrderId()))
                .orElse(null);
        if (order == null) {
            log.warn("Order ship failed: order not found. tenantId={}, orderId={}", command.getTenantId(), command.getOrderId());
            throw new IllegalArgumentException("Order not found");
        }
        var before = order.getStatus();
        try {
            order.ship(command.getCarrier(), command.getTrackingNo());
        } catch (RuntimeException ex) {
            log.warn("Order ship failed: invalid state. tenantId={}, orderId={}, status={}, carrier={}, trackingNo={}, reason={}",
                    command.getTenantId(), command.getOrderId(), before, command.getCarrier(), command.getTrackingNo(), ex.getMessage());
            throw ex;
        }
        Order saved = orderRepository.save(order);
        log.info("Order shipped. tenantId={}, orderId={}, status={} -> {}, carrier={}, trackingNo={}",
                command.getTenantId(), command.getOrderId(), before, saved.getStatus(), command.getCarrier(), command.getTrackingNo());
        return saved;
    }

    @Transactional
    public Order cancel(CancelOrderCommand command) {
        ensureIdempotencyKey(command.getIdempotencyKey());
        var acquire = idempotencyService.acquire(command.getTenantId(), command.getIdempotencyKey(), "ORDER_CANCEL");
        var idempotency = acquire.getRecord();
        if (idempotencyService.isSuccess(idempotency)) {
            String orderId = idempotency.getBizId();
            if (orderId == null) {
                throw new IllegalStateException("Idempotency record missing order id");
            }
            return orderRepository
                    .findById(command.getTenantId(), new OrderId(orderId))
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        }
        if (idempotencyService.isInProgress(idempotency) && !acquire.isNewlyCreated()) {
            throw new IllegalStateException("Request in progress");
        }
        if (!acquire.isNewlyCreated()) {
            idempotencyService.restart(idempotency);
        }
        Order order = orderRepository
                .findById(command.getTenantId(), new OrderId(command.getOrderId()))
                .orElse(null);
        if (order == null) {
            log.warn("Order cancel failed: order not found. tenantId={}, orderId={}", command.getTenantId(), command.getOrderId());
            throw new IllegalArgumentException("Order not found");
        }
        var before = order.getStatus();
        try {
            order.cancel();
        } catch (RuntimeException ex) {
            log.warn("Order cancel failed: invalid state. tenantId={}, orderId={}, status={}, reason={}",
                    command.getTenantId(), command.getOrderId(), before, ex.getMessage());
            throw ex;
        }
        try {
            releaseStock(command.getTenantId(), order.getItems());
            Order saved = orderRepository.save(order);
            idempotencyService.markSuccess(idempotency, saved.getId().value());
            log.info("Order cancelled. tenantId={}, orderId={}, status={} -> {}", command.getTenantId(), command.getOrderId(), before, saved.getStatus());
            return saved;
        } catch (RuntimeException ex) {
            idempotencyService.markFailed(idempotency);
            throw ex;
        }
    }

    @Transactional
    public Order confirmReceipt(ConfirmReceiptCommand command) {
        ensureIdempotencyKey(command.getIdempotencyKey());
        var acquire = idempotencyService.acquire(command.getTenantId(), command.getIdempotencyKey(), "ORDER_CONFIRM");
        var idempotency = acquire.getRecord();
        if (idempotencyService.isSuccess(idempotency)) {
            String orderId = idempotency.getBizId();
            if (orderId == null) {
                throw new IllegalStateException("Idempotency record missing order id");
            }
            return orderRepository
                    .findById(command.getTenantId(), new OrderId(orderId))
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        }
        if (idempotencyService.isInProgress(idempotency) && !acquire.isNewlyCreated()) {
            throw new IllegalStateException("Request in progress");
        }
        if (!acquire.isNewlyCreated()) {
            idempotencyService.restart(idempotency);
        }
        Order order = orderRepository
                .findById(command.getTenantId(), new OrderId(command.getOrderId()))
                .orElse(null);
        if (order == null) {
            log.warn("Order confirm failed: order not found. tenantId={}, orderId={}", command.getTenantId(), command.getOrderId());
            throw new IllegalArgumentException("Order not found");
        }
        var before = order.getStatus();
        try {
            order.confirm();
            Order saved = orderRepository.save(order);
            idempotencyService.markSuccess(idempotency, saved.getId().value());
            log.info("Order confirmed. tenantId={}, orderId={}, status={} -> {}",
                    command.getTenantId(), command.getOrderId(), before, saved.getStatus());
            return saved;
        } catch (RuntimeException ex) {
            idempotencyService.markFailed(idempotency);
            log.warn("Order confirm failed: invalid state. tenantId={}, orderId={}, status={}, reason={}",
                    command.getTenantId(), command.getOrderId(), before, ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public Order close(CloseOrderCommand command) {
        Order order = orderRepository
                .findById(command.getTenantId(), new OrderId(command.getOrderId()))
                .orElse(null);
        if (order == null) {
            log.warn("Order close failed: order not found. tenantId={}, orderId={}", command.getTenantId(), command.getOrderId());
            throw new IllegalArgumentException("Order not found");
        }
        var before = order.getStatus();
        try {
            order.close();
        } catch (RuntimeException ex) {
            log.warn("Order close failed: invalid state. tenantId={}, orderId={}, status={}, reason={}",
                    command.getTenantId(), command.getOrderId(), before, ex.getMessage());
            throw ex;
        }
        releaseStock(command.getTenantId(), order.getItems());
        Order saved = orderRepository.save(order);
        log.info("Order closed. tenantId={}, orderId={}, status={} -> {}", command.getTenantId(), command.getOrderId(), before, saved.getStatus());
        return saved;
    }

    @Transactional
    public Order closeForTimeout(String tenantId, String orderId) {
        Order order = orderRepository
                .findById(tenantId, new OrderId(orderId))
                .orElse(null);
        if (order == null) {
            log.warn("Order close (timeout) skipped: order not found. tenantId={}, orderId={}", tenantId, orderId);
            throw new IllegalArgumentException("Order not found");
        }
        var before = order.getStatus();
        try {
            order.close();
        } catch (RuntimeException ex) {
            log.warn("Order close (timeout) failed: invalid state. tenantId={}, orderId={}, status={}, reason={}",
                    tenantId, orderId, before, ex.getMessage());
            throw ex;
        }
        releaseStock(tenantId, order.getItems());
        Order saved = orderRepository.save(order);
        log.info("Order closed (timeout). tenantId={}, orderId={}, status={} -> {}", tenantId, orderId, before, saved.getStatus());
        return saved;
    }

    public Order get(String tenantId, String orderId) {
        return orderRepository
                .findById(tenantId, new OrderId(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public List<Order> listForTenant(String tenantId) {
        return orderRepository.listByTenant(tenantId);
    }

    public List<Order> listForBuyer(String tenantId, String buyerId) {
        return orderRepository.listByTenantAndBuyer(tenantId, buyerId);
    }

    private OrderItem toItem(OrderItemCommand item) {
        return new OrderItem(
                new ProductId(item.getProductId()),
                item.getProductName(),
                item.getPrice(),
                item.getQuantity()
        );
    }

    private void reserveStock(String tenantId, List<OrderItem> items) {
        for (OrderItem item : items) {
            Stock stock = stockRepository
                    .findByProductId(tenantId, item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("商品库存不存在：" + item.getProductName()));
            int nextQuantity = stock.getQuantity() - item.getQuantity();
            if (nextQuantity < 0) {
                throw new IllegalArgumentException("商品库存不足：" + item.getProductName());
            }
            stock.adjust(-item.getQuantity());
            stockRepository.save(stock);
        }
    }

    private void releaseStock(String tenantId, List<OrderItem> items) {
        for (OrderItem item : items) {
            Stock stock = stockRepository
                    .findByProductId(tenantId, item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("商品库存不存在：" + item.getProductName()));
            stock.adjust(item.getQuantity());
            stockRepository.save(stock);
        }
    }

    private void ensureIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key required");
        }
    }
}
