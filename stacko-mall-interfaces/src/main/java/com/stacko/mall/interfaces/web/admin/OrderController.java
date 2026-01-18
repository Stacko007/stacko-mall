package com.stacko.mall.interfaces.web.admin;

import com.stacko.mall.application.command.ShipOrderCommand;
import com.stacko.mall.application.service.OrderApplicationService;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.interfaces.web.dto.OrderShipRequest;
import com.stacko.mall.interfaces.web.view.OrderResponse;
import com.stacko.user.contract.security.RequiresPermission;
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

@RestController("adminOrderController")
@RequestMapping("/api/admin/orders")
@Tag(name = "商城-管理端", description = "订单管理接口")
public class OrderController {
    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @GetMapping
    @RequiresPermission("mall:order:list")
    public List<OrderResponse> list(@RequestHeader("X-Tenant-ID") String tenantId,
                                    @RequestParam(value = "buyerId", required = false) String buyerId) {
        List<Order> orders = buyerId == null || buyerId.isBlank()
                ? orderApplicationService.listForTenant(tenantId)
                : orderApplicationService.listForBuyer(tenantId, buyerId);
        return orders.stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{id}")
    @RequiresPermission("mall:order:read")
    public OrderResponse get(@RequestHeader("X-Tenant-ID") String tenantId,
                             @PathVariable("id") String id) {
        Order order = orderApplicationService.get(tenantId, id);
        return OrderResponse.from(order);
    }

    @PostMapping("/{id}/ship")
    @RequiresPermission("mall:order:ship")
    public OrderResponse ship(@RequestHeader("X-Tenant-ID") String tenantId,
                              @PathVariable("id") String id,
                              @Valid @RequestBody OrderShipRequest request) {
        ShipOrderCommand command = new ShipOrderCommand();
        command.setTenantId(tenantId);
        command.setOrderId(id);
        command.setCarrier(request.getCarrier());
        command.setTrackingNo(request.getTrackingNo());
        Order order = orderApplicationService.ship(command);
        return OrderResponse.from(order);
    }
}
