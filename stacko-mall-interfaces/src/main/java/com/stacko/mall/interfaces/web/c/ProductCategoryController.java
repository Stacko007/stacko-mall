package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.service.ProductCategoryApplicationService;
import com.stacko.mall.domain.model.ProductCategory;
import com.stacko.mall.interfaces.web.ApiResponse;
import com.stacko.mall.interfaces.web.view.ProductCategoryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController("cProductCategoryController")
@RequestMapping("/api/c/categories")
@Tag(name = "商城-C端", description = "商品类目浏览接口")
public class ProductCategoryController {
    private final ProductCategoryApplicationService categoryApplicationService;

    public ProductCategoryController(ProductCategoryApplicationService categoryApplicationService) {
        this.categoryApplicationService = categoryApplicationService;
    }

    @GetMapping
    public ApiResponse<List<ProductCategoryResponse>> list(@RequestHeader("X-Tenant-ID") String tenantId) {
        return ApiResponse.ok(toTree(categoryApplicationService.listEnabled(tenantId)));
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
