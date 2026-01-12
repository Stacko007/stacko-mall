package com.stacko.mall.infra.catalog;

import com.stacko.mall.domain.catalog.Product;
import com.stacko.mall.domain.catalog.ProductId;
import com.stacko.mall.domain.catalog.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProductRepository implements ProductRepository {
    private final Map<String, Map<String, Product>> store = new ConcurrentHashMap<>();

    @Override
    public Product save(Product product) {
        store
                .computeIfAbsent(product.getTenantId(), tenant -> new ConcurrentHashMap<>())
                .put(product.getId().value(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(String tenantId, ProductId id) {
        Map<String, Product> tenantStore = store.get(tenantId);
        if (tenantStore == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tenantStore.get(id.value()));
    }

    @Override
    public List<Product> listByTenant(String tenantId) {
        Map<String, Product> tenantStore = store.get(tenantId);
        if (tenantStore == null) {
            return List.of();
        }
        return new ArrayList<>(tenantStore.values());
    }
}
