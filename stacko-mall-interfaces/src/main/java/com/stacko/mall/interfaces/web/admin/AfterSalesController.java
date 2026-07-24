package com.stacko.mall.interfaces.web.admin;

import com.stacko.mall.application.command.RefundAfterSalesCommand;
import com.stacko.mall.application.command.ReviewAfterSalesCommand;
import com.stacko.mall.application.service.AfterSalesApplicationService;
import com.stacko.mall.domain.model.AfterSales;
import com.stacko.mall.interfaces.web.dto.AfterSalesRefundRequest;
import com.stacko.mall.interfaces.web.dto.AfterSalesReviewRequest;
import com.stacko.mall.interfaces.web.security.RequiresPermission;
import com.stacko.mall.interfaces.web.view.AfterSalesResponse;
import com.stacko.mall.interfaces.web.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminAfterSalesController")
@RequestMapping("/api/admin/after-sales")
@Validated
@Tag(name = "商城-管理端", description = "售后管理接口")
public class AfterSalesController {
    private final AfterSalesApplicationService afterSalesApplicationService;

    public AfterSalesController(AfterSalesApplicationService afterSalesApplicationService) {
        this.afterSalesApplicationService = afterSalesApplicationService;
    }

    @GetMapping("/{id}")
    @RequiresPermission("mall:afterSales:read")
    public ApiResponse<AfterSalesResponse> get(@RequestHeader("X-Tenant-ID") @NotBlank String tenantId,
                                               @PathVariable("id") @NotBlank String id) {
        AfterSales afterSales = afterSalesApplicationService.get(tenantId, id);
        return ApiResponse.ok(AfterSalesResponse.from(afterSales));
    }

    @PostMapping("/{id}/review")
    @RequiresPermission("mall:afterSales:review")
    public ApiResponse<AfterSalesResponse> review(@RequestHeader("X-Tenant-ID") @NotBlank String tenantId,
                                                  @PathVariable("id") @NotBlank String id,
                                                  @Valid @RequestBody AfterSalesReviewRequest request) {
        ReviewAfterSalesCommand command = new ReviewAfterSalesCommand();
        command.setTenantId(tenantId);
        command.setAfterSalesId(id);
        command.setApproved(Boolean.TRUE.equals(request.getApproved()));
        command.setRemark(request.getRemark());
        AfterSales afterSales = afterSalesApplicationService.review(command);
        return ApiResponse.ok(AfterSalesResponse.from(afterSales));
    }

    @PostMapping("/{id}/refund")
    @RequiresPermission("mall:afterSales:refund")
    public ApiResponse<AfterSalesResponse> refund(@RequestHeader("X-Tenant-ID") @NotBlank String tenantId,
                                                  @PathVariable("id") @NotBlank String id,
                                                  @Valid @RequestBody(required = false) AfterSalesRefundRequest request) {
        RefundAfterSalesCommand command = new RefundAfterSalesCommand();
        command.setTenantId(tenantId);
        command.setAfterSalesId(id);
        command.setRemark(request == null ? null : request.getRemark());
        AfterSales afterSales = afterSalesApplicationService.refund(command);
        return ApiResponse.ok(AfterSalesResponse.from(afterSales));
    }
}
