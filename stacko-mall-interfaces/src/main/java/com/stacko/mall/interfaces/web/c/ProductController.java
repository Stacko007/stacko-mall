package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.service.ProductApplicationService;
import com.stacko.mall.domain.model.Product;
import com.stacko.mall.interfaces.web.view.ProductResponse;
import com.stacko.user.contract.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("cProductController")
@RequestMapping("/api/c/products")
@Tag(name = "商城-C端", description = "商品浏览接口")
public class ProductController {
    private final ProductApplicationService productApplicationService;

    public ProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> list(@RequestHeader("X-Tenant-ID") String tenantId) {
        List<ProductResponse> responses = productApplicationService.list(tenantId).stream()
                .map(ProductResponse::from)
                .toList();
        return ApiResponse.ok(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@RequestHeader("X-Tenant-ID") String tenantId,
                                            @PathVariable("id") String id) {
        Product product = productApplicationService.get(tenantId, id);
        return ApiResponse.ok(ProductResponse.from(product));
    }
}
