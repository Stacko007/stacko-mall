package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.command.CreateShippingAddressCommand;
import com.stacko.mall.application.command.UpdateShippingAddressCommand;
import com.stacko.mall.application.service.MemberApplicationService;
import com.stacko.mall.application.service.ShippingAddressApplicationService;
import com.stacko.mall.domain.model.Member;
import com.stacko.mall.domain.model.ShippingAddress;
import com.stacko.mall.interfaces.web.ApiResponse;
import com.stacko.mall.interfaces.web.dto.ShippingAddressRequest;
import com.stacko.mall.interfaces.web.security.CurrentUser;
import com.stacko.mall.interfaces.web.security.CurrentUserContext;
import com.stacko.mall.interfaces.web.view.ShippingAddressResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/c/addresses")
@Tag(name = "商城-C端", description = "收货地址接口")
public class ShippingAddressController {
    private final ShippingAddressApplicationService shippingAddressApplicationService;
    private final MemberApplicationService memberApplicationService;
    private final CurrentUserContext currentUserContext;

    public ShippingAddressController(ShippingAddressApplicationService shippingAddressApplicationService,
                                     MemberApplicationService memberApplicationService,
                                     CurrentUserContext currentUserContext) {
        this.shippingAddressApplicationService = shippingAddressApplicationService;
        this.memberApplicationService = memberApplicationService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping
    public ApiResponse<List<ShippingAddressResponse>> list(@RequestHeader("X-Tenant-ID") String tenantId) {
        Member member = currentMember(tenantId);
        return ApiResponse.ok(shippingAddressApplicationService.list(tenantId, member.getId().value()).stream()
                .map(ShippingAddressResponse::from)
                .toList());
    }

    @PostMapping
    public ApiResponse<ShippingAddressResponse> create(@RequestHeader("X-Tenant-ID") String tenantId,
                                                       @Valid @RequestBody ShippingAddressRequest request) {
        Member member = currentMember(tenantId);
        CreateShippingAddressCommand command = toCreateCommand(tenantId, member.getId().value(), request);
        ShippingAddress address = shippingAddressApplicationService.create(command);
        return ApiResponse.ok(ShippingAddressResponse.from(address));
    }

    @PutMapping("/{id}")
    public ApiResponse<ShippingAddressResponse> update(@RequestHeader("X-Tenant-ID") String tenantId,
                                                       @PathVariable("id") String id,
                                                       @Valid @RequestBody ShippingAddressRequest request) {
        Member member = currentMember(tenantId);
        UpdateShippingAddressCommand command = new UpdateShippingAddressCommand();
        copy(command, tenantId, member.getId().value(), request);
        command.setAddressId(id);
        ShippingAddress address = shippingAddressApplicationService.update(command);
        return ApiResponse.ok(ShippingAddressResponse.from(address));
    }

    @PostMapping("/{id}/default")
    public ApiResponse<ShippingAddressResponse> setDefault(@RequestHeader("X-Tenant-ID") String tenantId,
                                                           @PathVariable("id") String id) {
        Member member = currentMember(tenantId);
        ShippingAddress address = shippingAddressApplicationService.setDefault(tenantId, member.getId().value(), id);
        return ApiResponse.ok(ShippingAddressResponse.from(address));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@RequestHeader("X-Tenant-ID") String tenantId,
                                    @PathVariable("id") String id) {
        Member member = currentMember(tenantId);
        shippingAddressApplicationService.delete(tenantId, member.getId().value(), id);
        return ApiResponse.ok(null);
    }

    private Member currentMember(String tenantId) {
        CurrentUser currentUser = currentUserContext.require(tenantId);
        return memberApplicationService.ensureMember(
                tenantId,
                currentUser.getAccountId(),
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getPhone(),
                currentUser.getEmail()
        );
    }

    private CreateShippingAddressCommand toCreateCommand(String tenantId, String buyerId, ShippingAddressRequest request) {
        CreateShippingAddressCommand command = new CreateShippingAddressCommand();
        copy(command, tenantId, buyerId, request);
        return command;
    }

    private void copy(CreateShippingAddressCommand command, String tenantId, String buyerId, ShippingAddressRequest request) {
        command.setTenantId(tenantId);
        command.setBuyerId(buyerId);
        command.setReceiverName(request.getReceiverName());
        command.setReceiverPhone(request.getReceiverPhone());
        command.setProvince(request.getProvince());
        command.setCity(request.getCity());
        command.setDistrict(request.getDistrict());
        command.setDetailAddress(request.getDetailAddress());
        command.setDefaultAddress(Boolean.TRUE.equals(request.getDefaultAddress()));
    }
}
