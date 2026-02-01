package com.stacko.mall.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stacko.mall.domain.enums.OrderStatus;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.domain.model.OrderId;
import com.stacko.mall.domain.model.OrderItem;
import com.stacko.mall.domain.repository.OrderRepository;
import com.stacko.mall.infra.dao.OrderItemMapper;
import com.stacko.mall.infra.dao.OrderMapper;
import com.stacko.mall.infra.po.OrderEntity;
import com.stacko.mall.infra.po.OrderItemEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MybatisOrderRepository implements OrderRepository {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public MybatisOrderRepository(OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        OrderEntity existing = orderMapper.selectById(entity.getId());
        if (existing == null) {
            orderMapper.insert(entity);
            for (OrderItem item : order.getItems()) {
                orderItemMapper.insert(OrderItemEntity.fromDomain(entity.getId(), item));
            }
            return order;
        }
        if (!Objects.equals(existing.getTenantId(), entity.getTenantId())) {
            throw new IllegalStateException("Order tenant mismatch");
        }
        orderMapper.updateById(entity);
        return order;
    }

    @Override
    public Optional<Order> findById(String tenantId, OrderId id) {
        LambdaQueryWrapper<OrderEntity> query = new LambdaQueryWrapper<>();
        query.eq(OrderEntity::getId, id.value())
                .eq(OrderEntity::getTenantId, tenantId);
        OrderEntity entity = orderMapper.selectOne(query);
        if (entity == null) {
            return Optional.empty();
        }
        List<OrderItem> items = listItems(entity.getId());
        return Optional.of(entity.toDomain(items));
    }

    @Override
    public List<Order> listByTenant(String tenantId) {
        LambdaQueryWrapper<OrderEntity> query = new LambdaQueryWrapper<>();
        query.eq(OrderEntity::getTenantId, tenantId)
                .orderByDesc(OrderEntity::getUpdatedAt);
        return orderMapper.selectList(query).stream()
                .map(entity -> entity.toDomain(listItems(entity.getId())))
                .toList();
    }

    @Override
    public List<Order> listByTenantAndBuyer(String tenantId, String buyerId) {
        LambdaQueryWrapper<OrderEntity> query = new LambdaQueryWrapper<>();
        query.eq(OrderEntity::getTenantId, tenantId)
                .eq(OrderEntity::getBuyerId, buyerId)
                .orderByDesc(OrderEntity::getUpdatedAt);
        return orderMapper.selectList(query).stream()
                .map(entity -> entity.toDomain(listItems(entity.getId())))
                .toList();
    }

    @Override
    public List<Order> listByStatusCreatedBefore(OrderStatus status, Instant cutoff, int limit) {
        LambdaQueryWrapper<OrderEntity> query = new LambdaQueryWrapper<>();
        query.eq(OrderEntity::getStatus, status)
                .lt(OrderEntity::getCreatedAt, toLocalDateTime(cutoff))
                .orderByAsc(OrderEntity::getCreatedAt);
        if (limit > 0) {
            query.last("limit " + limit);
        }
        return orderMapper.selectList(query).stream()
                .map(entity -> entity.toDomain(listItems(entity.getId())))
                .toList();
    }

    private List<OrderItem> listItems(String orderId) {
        LambdaQueryWrapper<OrderItemEntity> query = new LambdaQueryWrapper<>();
        query.eq(OrderItemEntity::getOrderId, orderId);
        return orderItemMapper.selectList(query).stream()
                .map(OrderItemEntity::toDomain)
                .toList();
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
