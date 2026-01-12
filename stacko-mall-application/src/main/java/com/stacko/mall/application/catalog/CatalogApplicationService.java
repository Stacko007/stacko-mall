package com.stacko.mall.application.catalog;

import com.stacko.mall.domain.catalog.Product;
import com.stacko.mall.domain.catalog.ProductId;
import com.stacko.mall.domain.catalog.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogApplicationService {
    private final ProductRepository productRepository;

    public CatalogApplicationService(ProductRepository productRepository) {
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
