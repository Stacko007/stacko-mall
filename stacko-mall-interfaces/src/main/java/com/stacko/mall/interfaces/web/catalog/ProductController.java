package com.stacko.mall.interfaces.web.catalog;

import com.stacko.mall.application.catalog.CatalogApplicationService;
import com.stacko.mall.application.catalog.CreateProductCommand;
import com.stacko.mall.application.catalog.UpdateProductCommand;
import com.stacko.mall.domain.catalog.Product;
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

@RestController
@RequestMapping("/mall/api/products")
public class ProductController {
    private final CatalogApplicationService catalogApplicationService;

    public ProductController(CatalogApplicationService catalogApplicationService) {
        this.catalogApplicationService = catalogApplicationService;
    }

    @PostMapping
    public ProductResponse create(@RequestHeader("X-Tenant-ID") String tenantId,
                                  @Valid @RequestBody ProductCreateRequest request) {
        CreateProductCommand command = new CreateProductCommand();
        command.setTenantId(tenantId);
        command.setName(request.getName());
        command.setDescription(request.getDescription());
        command.setPrice(request.getPrice());
        Product product = catalogApplicationService.create(command);
        return ProductResponse.from(product);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@RequestHeader("X-Tenant-ID") String tenantId,
                                  @PathVariable("id") String id,
                                  @Valid @RequestBody ProductUpdateRequest request) {
        UpdateProductCommand command = new UpdateProductCommand();
        command.setTenantId(tenantId);
        command.setProductId(id);
        command.setName(request.getName());
        command.setDescription(request.getDescription());
        command.setPrice(request.getPrice());
        command.setStatus(request.getStatus());
        Product product = catalogApplicationService.update(command);
        return ProductResponse.from(product);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@RequestHeader("X-Tenant-ID") String tenantId,
                               @PathVariable("id") String id) {
        Product product = catalogApplicationService.get(tenantId, id);
        return ProductResponse.from(product);
    }

    @GetMapping
    public List<ProductResponse> list(@RequestHeader("X-Tenant-ID") String tenantId) {
        return catalogApplicationService.list(tenantId).stream()
                .map(ProductResponse::from)
                .toList();
    }
}
