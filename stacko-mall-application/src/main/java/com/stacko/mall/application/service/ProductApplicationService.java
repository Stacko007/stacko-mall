package com.stacko.mall.application.service;

import com.stacko.mall.application.command.CreateProductCommand;
import com.stacko.mall.application.command.UpdateProductCommand;
import com.stacko.mall.domain.model.Product;
import com.stacko.mall.domain.model.ProductId;
import com.stacko.mall.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductApplicationService {
    private final ProductRepository productRepository;

    public ProductApplicationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(CreateProductCommand command) {
        Product product = Product.create(
                command.getTenantId(),
                command.getName(),
                command.getDescription(),
                command.getPrice()
        );
        return productRepository.save(product);
    }

    public Product update(UpdateProductCommand command) {
        Product product = productRepository
                .findById(command.getTenantId(), new ProductId(command.getProductId()))
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.update(command.getName(), command.getDescription(), command.getPrice(), command.getStatus());
        return productRepository.save(product);
    }

    public Product get(String tenantId, String productId) {
        return productRepository
                .findById(tenantId, new ProductId(productId))
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public List<Product> list(String tenantId) {
        return productRepository.listByTenant(tenantId);
    }
}
