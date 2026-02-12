package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.command.PaymentCallbackCommand;
import com.stacko.mall.application.service.PaymentApplicationService;
import com.stacko.mall.domain.model.Payment;
import com.stacko.mall.interfaces.web.dto.PaymentCallbackRequest;
import com.stacko.user.contract.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("cPaymentController")
@RequestMapping("/api/c/payments")
@Tag(name = "商城-C端", description = "支付接口")
public class PaymentController {
    private final PaymentApplicationService paymentApplicationService;

    public PaymentController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping("/callback")
    public ApiResponse<String> callback(@RequestHeader("X-Tenant-ID") String tenantId,
                                        @Valid @RequestBody PaymentCallbackRequest request) {
        PaymentCallbackCommand command = new PaymentCallbackCommand();
        command.setTenantId(tenantId);
        command.setCallbackId(request.getCallbackId());
        command.setOrderId(request.getOrderId());
        command.setTradeNo(request.getTradeNo());
        command.setStatus(request.getStatus());
        command.setSignature(request.getSignature());
        command.setRawPayload(request.getRawPayload());
        Payment payment = paymentApplicationService.handleCallback(command);
        return ApiResponse.ok("SUCCESS:" + payment.getId().value());
    }
}
