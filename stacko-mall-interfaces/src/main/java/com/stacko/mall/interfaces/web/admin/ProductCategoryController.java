package com.stacko.mall.interfaces.web.admin;

import com.stacko.mall.application.command.CreateProductCategoryCommand;
import com.stacko.mall.application.command.UpdateProductCategoryCommand;
import com.stacko.mall.application.service.ProductCategoryApplicationService;
import com.stacko.mall.domain.model.ProductCategory;
import com.stacko.mall.interfaces.web.ApiResponse;
import com.stacko.mall.interfaces.web.dto.ProductCategoryRequest;
import com.stacko.mall.interfaces.web.security.RequiresPermission;
import com.stacko.mall.interfaces.web.view.ProductCategoryResponse;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")
@Tag(name = "商城-管理端", description = "商品类目管理接口")
public class ProductCategoryController {
    private final ProductCategoryApplicationService categoryApplicationService;

    public ProductCategoryController(ProductCategoryApplicationService categoryApplicationService) {
        this.categoryApplicationService = categoryApplicationService;
    }

    @GetMapping
    @RequiresPermission("mall:category:list")
    public ApiResponse<List<ProductCategoryResponse>> list(@RequestHeader("X-Tenant-ID") String tenantId) {
        return ApiResponse.ok(toTree(categoryApplicationService.list(tenantId)));
    }

    @GetMapping("/{id}")
    @RequiresPermission("mall:category:read")
    public ApiResponse<ProductCategoryResponse> get(@RequestHeader("X-Tenant-ID") String tenantId,
                                                    @PathVariable("id") String id) {
        return ApiResponse.ok(ProductCategoryResponse.from(categoryApplicationService.get(tenantId, id)));
    }

    @PostMapping
    @RequiresPermission("mall:category:create")
    public ApiResponse<ProductCategoryResponse> create(@RequestHeader("X-Tenant-ID") String tenantId,
                                                       @Valid @RequestBody ProductCategoryRequest request) {
        CreateProductCategoryCommand command = new CreateProductCategoryCommand();
        copy(command, tenantId, request);
        ProductCategory category = categoryApplicationService.create(command);
        return ApiResponse.ok(ProductCategoryResponse.from(category));
    }

    @PutMapping("/{id}")
    @RequiresPermission("mall:category:update")
    public ApiResponse<ProductCategoryResponse> update(@RequestHeader("X-Tenant-ID") String tenantId,
                                                       @PathVariable("id") String id,
                                                       @Valid @RequestBody ProductCategoryRequest request) {
        UpdateProductCategoryCommand command = new UpdateProductCategoryCommand();
        copy(command, tenantId, request);
        command.setCategoryId(id);
        ProductCategory category = categoryApplicationService.update(command);
        return ApiResponse.ok(ProductCategoryResponse.from(category));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("mall:category:delete")
    public ApiResponse<Void> delete(@RequestHeader("X-Tenant-ID") String tenantId,
                                    @PathVariable("id") String id) {
        categoryApplicationService.delete(tenantId, id);
        return ApiResponse.ok(null);
    }

    private void copy(CreateProductCategoryCommand command, String tenantId, ProductCategoryRequest request) {
        command.setTenantId(tenantId);
        command.setParentId(request.getParentId());
        command.setName(request.getName());
        command.setSort(request.getSort());
        command.setStatus(request.getStatus());
    }

    private List<ProductCategoryResponse> toTree(List<ProductCategory> categories) {
        Map<String, ProductCategoryResponse> byId = new LinkedHashMap<>();
        for (ProductCategory category : categories) {
            byId.put(category.getId().value(), ProductCategoryResponse.from(category));
        }
        List<ProductCategoryResponse> roots = new java.util.ArrayList<>();
        for (ProductCategoryResponse item : byId.values()) {
            if (item.getParentId() == null || !byId.containsKey(item.getParentId())) {
                roots.add(item);
            } else {
                byId.get(item.getParentId()).getChildren().add(item);
            }
        }
        return roots;
    }
}
