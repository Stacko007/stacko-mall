package com.stacko.mall.interfaces.web.admin;

import com.stacko.mall.application.service.ProductApplicationService;
import com.stacko.mall.application.command.CreateProductCommand;
import com.stacko.mall.application.command.UpdateProductCommand;
import com.stacko.mall.domain.model.Product;
import com.stacko.mall.interfaces.web.dto.ProductCreateRequest;
import com.stacko.mall.interfaces.web.security.RequiresPermission;
import com.stacko.mall.interfaces.web.view.ProductResponse;
import com.stacko.mall.interfaces.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.stacko.mall.interfaces.web.dto.ProductUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("adminProductController")
@RequestMapping("/api/admin/products")
@Tag(name = "商城-管理端", description = "商品管理接口")
public class ProductController {
    private final ProductApplicationService productApplicationService;

    public ProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @PostMapping
    @RequiresPermission("mall:product:create")
    @Operation(summary = "创建商品", description = "创建一个新的商品")
    public ApiResponse<ProductResponse> create(@RequestHeader("X-Tenant-ID") String tenantId,
                                               @Valid @RequestBody ProductCreateRequest request) {
        CreateProductCommand command = new CreateProductCommand();
        command.setTenantId(tenantId);
        command.setName(request.getName());
        command.setDescription(request.getDescription());
        command.setPrice(request.getPrice());
        Product product = productApplicationService.create(command);
        return ApiResponse.ok(ProductResponse.from(product));
    }

    @PutMapping("/{id}")
    @RequiresPermission("mall:product:update")
    @Operation(summary = "更新商品", description = "更新一个已存在的商品")
    public ApiResponse<ProductResponse> update(@RequestHeader("X-Tenant-ID") String tenantId,
                                               @PathVariable("id") String id,
                                               @Valid @RequestBody ProductUpdateRequest request) {
        UpdateProductCommand command = new UpdateProductCommand();
        command.setTenantId(tenantId);
        command.setProductId(id);
        command.setName(request.getName());
        command.setDescription(request.getDescription());
        command.setPrice(request.getPrice());
        command.setStatus(request.getStatus());
        Product product = productApplicationService.update(command);
        return ApiResponse.ok(ProductResponse.from(product));
    }

    @GetMapping("/{id}")
    @RequiresPermission("mall:product:read")
    public ApiResponse<ProductResponse> get(@RequestHeader("X-Tenant-ID") String tenantId,
                                            @PathVariable("id") String id) {
        Product product = productApplicationService.get(tenantId, id);
        return ApiResponse.ok(ProductResponse.from(product));
    }

    @GetMapping
    @RequiresPermission("mall:product:list")
    public ApiResponse<List<ProductResponse>> list(@RequestHeader("X-Tenant-ID") String tenantId) {
        List<ProductResponse> responses = productApplicationService.list(tenantId).stream()
                .map(ProductResponse::from)
                .toList();
        return ApiResponse.ok(responses);
    }
}
