package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.command.CancelOrderCommand;
import com.stacko.mall.application.command.ConfirmReceiptCommand;
import com.stacko.mall.application.command.CreateOrderCommand;
import com.stacko.mall.application.command.OrderItemCommand;
import com.stacko.mall.application.command.PayOrderCommand;
import com.stacko.mall.application.service.OrderApplicationService;
import com.stacko.mall.application.service.MemberApplicationService;
import com.stacko.mall.application.service.ShippingAddressApplicationService;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.domain.model.Member;
import com.stacko.mall.domain.model.ShippingAddress;
import com.stacko.mall.interfaces.web.dto.OrderCreateRequest;
import com.stacko.mall.interfaces.web.dto.OrderItemRequest;
import com.stacko.mall.interfaces.web.security.CurrentUser;
import com.stacko.mall.interfaces.web.security.CurrentUserContext;
import com.stacko.mall.interfaces.web.view.OrderResponse;
import com.stacko.mall.interfaces.web.ApiResponse;
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
    private final MemberApplicationService memberApplicationService;
    private final CurrentUserContext currentUserContext;
    private final ShippingAddressApplicationService shippingAddressApplicationService;

    public OrderController(OrderApplicationService orderApplicationService,
                           MemberApplicationService memberApplicationService,
                           CurrentUserContext currentUserContext,
                           ShippingAddressApplicationService shippingAddressApplicationService) {
        this.orderApplicationService = orderApplicationService;
        this.memberApplicationService = memberApplicationService;
        this.currentUserContext = currentUserContext;
        this.shippingAddressApplicationService = shippingAddressApplicationService;
    }

    @PostMapping
    public ApiResponse<OrderResponse> create(@RequestHeader("X-Tenant-ID") String tenantId,
                                             @RequestHeader("X-Idempotency-Key") String idempotencyKey,
                                             @Valid @RequestBody OrderCreateRequest request) {
        CurrentUser currentUser = currentUserContext.require(tenantId);
        Member member = ensureMember(tenantId, currentUser);
        CreateOrderCommand command = new CreateOrderCommand();
        command.setTenantId(tenantId);
        command.setIdempotencyKey(idempotencyKey);
        command.setBuyerId(member.getId().value());
        ShippingAddress address = shippingAddressApplicationService.get(tenantId, member.getId().value(), request.getAddressId());
        command.setAddressId(address.getId().value());
        command.setReceiverName(address.getReceiverName());
        command.setReceiverPhone(address.getReceiverPhone());
        command.setReceiverProvince(address.getProvince());
        command.setReceiverCity(address.getCity());
        command.setReceiverDistrict(address.getDistrict());
        command.setReceiverAddress(address.getDetailAddress());
        command.setItems(request.getItems().stream().map(this::toItem).toList());
        Order order = orderApplicationService.create(command);
        return ApiResponse.ok(OrderResponse.from(order));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<OrderResponse> pay(@RequestHeader("X-Tenant-ID") String tenantId,
                                          @RequestHeader("X-Idempotency-Key") String idempotencyKey,
                                          @PathVariable("id") String id) {
        CurrentUser currentUser = currentUserContext.require(tenantId);
        Member member = ensureMember(tenantId, currentUser);
        ensureCurrentBuyer(orderApplicationService.get(tenantId, id), member);
        PayOrderCommand command = new PayOrderCommand();
        command.setTenantId(tenantId);
        command.setIdempotencyKey(idempotencyKey);
        command.setOrderId(id);
        Order order = orderApplicationService.pay(command);
        return ApiResponse.ok(OrderResponse.from(order));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@RequestHeader("X-Tenant-ID") String tenantId,
                                             @RequestHeader("X-Idempotency-Key") String idempotencyKey,
                                             @PathVariable("id") String id) {
        CurrentUser currentUser = currentUserContext.require(tenantId);
        Member member = ensureMember(tenantId, currentUser);
        ensureCurrentBuyer(orderApplicationService.get(tenantId, id), member);
        CancelOrderCommand command = new CancelOrderCommand();
        command.setTenantId(tenantId);
        command.setIdempotencyKey(idempotencyKey);
        command.setOrderId(id);
        Order order = orderApplicationService.cancel(command);
        return ApiResponse.ok(OrderResponse.from(order));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<OrderResponse> confirmReceipt(@RequestHeader("X-Tenant-ID") String tenantId,
                                                     @RequestHeader("X-Idempotency-Key") String idempotencyKey,
                                                     @PathVariable("id") String id) {
        CurrentUser currentUser = currentUserContext.require(tenantId);
        Member member = ensureMember(tenantId, currentUser);
        ensureCurrentBuyer(orderApplicationService.get(tenantId, id), member);
        ConfirmReceiptCommand command = new ConfirmReceiptCommand();
        command.setTenantId(tenantId);
        command.setIdempotencyKey(idempotencyKey);
        command.setOrderId(id);
        Order order = orderApplicationService.confirmReceipt(command);
        return ApiResponse.ok(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@RequestHeader("X-Tenant-ID") String tenantId,
                                          @PathVariable("id") String id) {
        CurrentUser currentUser = currentUserContext.require(tenantId);
        Member member = ensureMember(tenantId, currentUser);
        Order order = orderApplicationService.get(tenantId, id);
        ensureCurrentBuyer(order, member);
        return ApiResponse.ok(OrderResponse.from(order));
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> list(@RequestHeader("X-Tenant-ID") String tenantId,
                                                 @RequestParam(value = "buyerId", required = false) String ignoredBuyerId) {
        CurrentUser currentUser = currentUserContext.require(tenantId);
        Member member = ensureMember(tenantId, currentUser);
        List<OrderResponse> responseViews = orderApplicationService
                .listForBuyer(tenantId, member.getId().value()).stream()
                .map(OrderResponse::from)
                .toList();
        return ApiResponse.ok(responseViews);
    }

    private OrderItemCommand toItem(OrderItemRequest request) {
        OrderItemCommand command = new OrderItemCommand();
        command.setProductId(request.getProductId());
        command.setProductName(request.getProductName());
        command.setPrice(request.getPrice());
        command.setQuantity(request.getQuantity());
        return command;
    }

    private Member ensureMember(String tenantId, CurrentUser currentUser) {
        return memberApplicationService.ensureMember(
                tenantId,
                currentUser.getAccountId(),
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getPhone(),
                currentUser.getEmail()
        );
    }

    private void ensureCurrentBuyer(Order order, Member member) {
        if (!order.getBuyerId().equals(member.getId().value())) {
            throw new SecurityException("Order does not belong to current user");
        }
    }
}
