package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.command.ApplyAfterSalesCommand;
import com.stacko.mall.application.service.AfterSalesApplicationService;
import com.stacko.mall.application.service.MemberApplicationService;
import com.stacko.mall.application.service.OrderApplicationService;
import com.stacko.mall.domain.model.AfterSales;
import com.stacko.mall.domain.model.Member;
import com.stacko.mall.domain.model.Order;
import com.stacko.mall.interfaces.web.dto.AfterSalesApplyRequest;
import com.stacko.mall.interfaces.web.security.CurrentUser;
import com.stacko.mall.interfaces.web.security.CurrentUserContext;
import com.stacko.mall.interfaces.web.view.AfterSalesResponse;
import com.stacko.mall.interfaces.web.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("cAfterSalesController")
@RequestMapping("/api/c/after-sales")
@Tag(name = "商城-C端", description = "售后接口")
public class AfterSalesController {
    private final AfterSalesApplicationService afterSalesApplicationService;
    private final OrderApplicationService orderApplicationService;
    private final MemberApplicationService memberApplicationService;
    private final CurrentUserContext currentUserContext;

    public AfterSalesController(AfterSalesApplicationService afterSalesApplicationService,
                                OrderApplicationService orderApplicationService,
                                MemberApplicationService memberApplicationService,
                                CurrentUserContext currentUserContext) {
        this.afterSalesApplicationService = afterSalesApplicationService;
        this.orderApplicationService = orderApplicationService;
        this.memberApplicationService = memberApplicationService;
        this.currentUserContext = currentUserContext;
    }

    @PostMapping
    public ApiResponse<AfterSalesResponse> apply(@RequestHeader("X-Tenant-ID") String tenantId,
                                                 @Valid @RequestBody AfterSalesApplyRequest request) {
        CurrentUser currentUser = currentUserContext.require(tenantId);
        ensureCurrentBuyer(
                orderApplicationService.get(tenantId, request.getOrderId()),
                ensureMember(tenantId, currentUser));
        ApplyAfterSalesCommand command = new ApplyAfterSalesCommand();
        command.setTenantId(tenantId);
        command.setOrderId(request.getOrderId());
        command.setType(request.getType());
        command.setReason(request.getReason());
        AfterSales afterSales = afterSalesApplicationService.apply(command);
        return ApiResponse.ok(AfterSalesResponse.from(afterSales));
    }

    @GetMapping("/{id}")
    public ApiResponse<AfterSalesResponse> get(@RequestHeader("X-Tenant-ID") String tenantId,
                                               @PathVariable("id") String id) {
        CurrentUser currentUser = currentUserContext.require(tenantId);
        Member member = ensureMember(tenantId, currentUser);
        AfterSales afterSales = afterSalesApplicationService.get(tenantId, id);
        ensureCurrentBuyer(
                orderApplicationService.get(tenantId, afterSales.getOrderId().value()),
                member);
        return ApiResponse.ok(AfterSalesResponse.from(afterSales));
    }

    private Member ensureMember(String tenantId, CurrentUser currentUser) {
        return memberApplicationService.ensureMember(
                tenantId,
                currentUser.getAccountId(),
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getPhone(),
                currentUser.getEmail());
    }

    private void ensureCurrentBuyer(Order order, Member member) {
        if (!order.getBuyerId().equals(member.getId().value())) {
            throw new SecurityException("Order does not belong to current user");
        }
    }
}
