package com.stacko.mall.application.service;

import com.stacko.mall.application.command.CreateProductCommand;
import com.stacko.mall.application.command.UpdateProductCommand;
import com.stacko.mall.domain.enums.ProductStatus;
import com.stacko.mall.domain.model.Product;
import com.stacko.mall.domain.model.ProductCategory;
import com.stacko.mall.domain.model.ProductId;
import com.stacko.mall.domain.model.Stock;
import com.stacko.mall.domain.repository.ProductRepository;
import com.stacko.mall.domain.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ProductApplicationService {
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final ProductCategoryApplicationService categoryApplicationService;

    public ProductApplicationService(ProductRepository productRepository,
                                     StockRepository stockRepository,
                                     ProductCategoryApplicationService categoryApplicationService) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.categoryApplicationService = categoryApplicationService;
    }

    @Transactional
    public Product create(CreateProductCommand command) {
        Product product = Product.create(
                command.getTenantId(),
                command.getName(),
                command.getDescription(),
                command.getPrice(),
                validatedCategoryId(command.getTenantId(), command.getCategoryId())
        );
        Product saved = productRepository.save(product);
        stockRepository.save(Stock.create(command.getTenantId(), saved.getId(), 0));
        return saved;
    }

    public Product update(UpdateProductCommand command) {
        Product product = productRepository
                .findById(command.getTenantId(), new ProductId(command.getProductId()))
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.update(command.getName(), command.getDescription(), command.getPrice(), command.getStatus(),
                validatedCategoryId(command.getTenantId(), command.getCategoryId()));
        return productRepository.save(product);
    }

    public Product get(String tenantId, String productId) {
        return productRepository
                .findById(tenantId, new ProductId(productId))
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public Product getActive(String tenantId, String productId) {
        Product product = get(tenantId, productId);
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("Product not available");
        }
        return product;
    }

    public List<Product> list(String tenantId) {
        return productRepository.listByTenant(tenantId);
    }

    public List<Product> listActive(String tenantId) {
        return productRepository.listByTenant(tenantId).stream()
                .filter(product -> product.getStatus() == ProductStatus.ACTIVE)
                .toList();
    }

    public List<Product> listActive(String tenantId, String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return listActive(tenantId);
        }
        ProductCategory category = categoryApplicationService.getEnabled(tenantId, categoryId);
        Set<String> categoryIds = categoryApplicationService.listEnabled(tenantId).stream()
                .filter(candidate -> candidate.getPath().equals(category.getPath())
                        || candidate.getPath().startsWith(category.getPath() + "/"))
                .map(candidate -> candidate.getId().value())
                .collect(java.util.stream.Collectors.toSet());
        return productRepository.listByTenant(tenantId).stream()
                .filter(product -> product.getStatus() == ProductStatus.ACTIVE)
                .filter(product -> product.getCategoryId() != null && categoryIds.contains(product.getCategoryId()))
                .toList();
    }

    private String validatedCategoryId(String tenantId, String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return null;
        }
        return categoryApplicationService.getEnabled(tenantId, categoryId).getId().value();
    }
}
