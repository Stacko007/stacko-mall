package com.stacko.mall.application.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderApplicationService {
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;

    public OrderApplicationService(OrderRepository orderRepository, StockRepository stockRepository) {
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public Order create(CreateOrderCommand command) {
        List<OrderItem> items = command.getItems().stream()
                .map(this::toItem)
                .toList();
        Order order = Order.create(command.getTenantId(), command.getBuyerId(), items);
        reserveStock(command.getTenantId(), items);
        return orderRepository.save(order);
    }

    @Transactional
    public Order pay(PayOrderCommand command) {
        Order order = orderRepository
                .findById(command.getTenantId(), new OrderId(command.getOrderId()))
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.pay();
        return orderRepository.save(order);
    }

    @Transactional
    public Order ship(ShipOrderCommand command) {
        Order order = orderRepository
                .findById(command.getTenantId(), new OrderId(command.getOrderId()))
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.ship(command.getCarrier(), command.getTrackingNo());
        return orderRepository.save(order);
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
                    .orElseThrow(() -> new IllegalArgumentException("Stock not found"));
            int nextQuantity = stock.getQuantity() - item.getQuantity();
            if (nextQuantity < 0) {
                throw new IllegalArgumentException("Insufficient stock");
            }
            stock.adjust(-item.getQuantity());
            stockRepository.save(stock);
        }
    }
}
