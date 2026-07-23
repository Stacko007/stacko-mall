package com.stacko.mall.interfaces.web.admin;

import com.stacko.mall.application.service.PaymentApplicationService;
import com.stacko.mall.domain.model.Payment;
import com.stacko.mall.interfaces.web.view.PaymentResponse;
import com.stacko.mall.interfaces.web.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminPaymentController")
@RequestMapping("/api/admin/payments")
@Validated
@Tag(name = "商城-管理端", description = "支付管理接口")
public class PaymentController {
    private final PaymentApplicationService paymentApplicationService;

    public PaymentController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> get(@RequestHeader("X-Tenant-ID") @NotBlank String tenantId,
                                            @PathVariable("id") @NotBlank String id) {
        Payment payment = paymentApplicationService.get(tenantId, id);
        return ApiResponse.ok(PaymentResponse.from(payment));
    }
}
