package com.stacko.mall.interfaces.web.admin;

import com.stacko.mall.application.command.CloseOrderCommand;
import com.stacko.mall.application.command.ShipOrderCommand;
import com.stacko.mall.application.service.MemberApplicationService;
import com.stacko.mall.application.service.OrderApplicationService;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.interfaces.web.dto.OrderShipRequest;
import com.stacko.mall.interfaces.web.view.OrderResponse;
import com.stacko.mall.interfaces.web.security.RequiresPermission;
import com.stacko.mall.interfaces.web.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@RestController("adminOrderController")
@RequestMapping("/api/admin/orders")
@Validated
@Tag(name = "商城-管理端", description = "订单管理接口")
public class OrderController {
    private final OrderApplicationService orderApplicationService;
    private final MemberApplicationService memberApplicationService;

    public OrderController(OrderApplicationService orderApplicationService,
                           MemberApplicationService memberApplicationService) {
        this.orderApplicationService = orderApplicationService;
        this.memberApplicationService = memberApplicationService;
    }

    @GetMapping
    @RequiresPermission("mall:order:list")
    public ApiResponse<List<OrderResponse>> list(@RequestHeader("X-Tenant-ID") @NotBlank String tenantId,
                                                 @RequestParam(value = "buyerId", required = false) String buyerId) {
        List<Order> orders = buyerId == null || buyerId.isBlank()
                ? orderApplicationService.listForTenant(tenantId)
                : orderApplicationService.listForBuyer(tenantId, buyerId);
        Map<String, String> buyerNames = memberApplicationService.getBuyerNames(tenantId);
        List<OrderResponse> responses = orders.stream()
                .map(order -> OrderResponse.from(order, buyerNames.get(order.getBuyerId())))
                .toList();
        return ApiResponse.ok(responses);
    }

    @GetMapping("/{id}")
    @RequiresPermission("mall:order:read")
    public ApiResponse<OrderResponse> get(@RequestHeader("X-Tenant-ID") @NotBlank String tenantId,
                                          @PathVariable("id") @NotBlank String id) {
        Order order = orderApplicationService.get(tenantId, id);
        String buyerName = memberApplicationService.getBuyerNames(tenantId).get(order.getBuyerId());
        return ApiResponse.ok(OrderResponse.from(order, buyerName));
    }

    @PostMapping("/{id}/ship")
    @RequiresPermission("mall:order:ship")
    public ApiResponse<OrderResponse> ship(@RequestHeader("X-Tenant-ID") @NotBlank String tenantId,
                                           @PathVariable("id") @NotBlank String id,
                                           @Valid @RequestBody OrderShipRequest request) {
        ShipOrderCommand command = new ShipOrderCommand();
        command.setTenantId(tenantId);
        command.setOrderId(id);
        command.setCarrier(request.getCarrier());
        command.setTrackingNo(request.getTrackingNo());
        Order order = orderApplicationService.ship(command);
        return ApiResponse.ok(OrderResponse.from(order));
    }

    @PostMapping("/{id}/close")
    @RequiresPermission("mall:order:close")
    public ApiResponse<OrderResponse> close(@RequestHeader("X-Tenant-ID") @NotBlank String tenantId,
                                            @PathVariable("id") @NotBlank String id) {
        CloseOrderCommand command = new CloseOrderCommand();
        command.setTenantId(tenantId);
        command.setOrderId(id);
        Order order = orderApplicationService.close(command);
        return ApiResponse.ok(OrderResponse.from(order));
    }
}
