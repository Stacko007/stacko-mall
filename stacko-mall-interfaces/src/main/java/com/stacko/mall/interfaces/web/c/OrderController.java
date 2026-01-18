package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.command.CreateOrderCommand;
import com.stacko.mall.application.command.OrderItemCommand;
import com.stacko.mall.application.command.PayOrderCommand;
import com.stacko.mall.application.service.OrderApplicationService;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.interfaces.web.dto.OrderCreateRequest;
import com.stacko.mall.interfaces.web.dto.OrderItemRequest;
import com.stacko.mall.interfaces.web.view.OrderResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("cOrderController")
@RequestMapping("/api/c/orders")
@Tag(name = "商城-C端", description = "订单接口")
public class OrderController {
    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    public OrderResponse create(@RequestHeader("X-Tenant-ID") String tenantId,
                                @Valid @RequestBody OrderCreateRequest request) {
        CreateOrderCommand command = new CreateOrderCommand();
        command.setTenantId(tenantId);
        command.setBuyerId(request.getBuyerId());
        command.setItems(request.getItems().stream().map(this::toItem).toList());
        Order order = orderApplicationService.create(command);
        return OrderResponse.from(order);
    }

    @PostMapping("/{id}/pay")
    public OrderResponse pay(@RequestHeader("X-Tenant-ID") String tenantId,
                             @PathVariable("id") String id) {
        PayOrderCommand command = new PayOrderCommand();
        command.setTenantId(tenantId);
        command.setOrderId(id);
        Order order = orderApplicationService.pay(command);
        return OrderResponse.from(order);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@RequestHeader("X-Tenant-ID") String tenantId,
                             @PathVariable("id") String id) {
        Order order = orderApplicationService.get(tenantId, id);
        return OrderResponse.from(order);
    }

    @GetMapping
    public List<OrderResponse> list(@RequestHeader("X-Tenant-ID") String tenantId,
                                    @RequestParam("buyerId") String buyerId) {
        return orderApplicationService.listForBuyer(tenantId, buyerId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    private OrderItemCommand toItem(OrderItemRequest request) {
        OrderItemCommand command = new OrderItemCommand();
        command.setProductId(request.getProductId());
        command.setProductName(request.getProductName());
        command.setPrice(request.getPrice());
        command.setQuantity(request.getQuantity());
        return command;
    }
}
