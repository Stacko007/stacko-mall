package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.command.ApplyAfterSalesCommand;
import com.stacko.mall.application.service.AfterSalesApplicationService;
import com.stacko.mall.domain.model.AfterSales;
import com.stacko.mall.interfaces.web.dto.AfterSalesApplyRequest;
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

    public AfterSalesController(AfterSalesApplicationService afterSalesApplicationService) {
        this.afterSalesApplicationService = afterSalesApplicationService;
    }

    @PostMapping
    public ApiResponse<AfterSalesResponse> apply(@RequestHeader("X-Tenant-ID") String tenantId,
                                                 @Valid @RequestBody AfterSalesApplyRequest request) {
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
        AfterSales afterSales = afterSalesApplicationService.get(tenantId, id);
        return ApiResponse.ok(AfterSalesResponse.from(afterSales));
    }
}
