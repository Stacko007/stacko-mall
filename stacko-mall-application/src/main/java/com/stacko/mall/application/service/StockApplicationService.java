package com.stacko.mall.application.service;

import com.stacko.mall.application.command.AdjustStockCommand;
import com.stacko.mall.application.command.SetStockCommand;
import com.stacko.mall.application.result.StockListItem;
import com.stacko.mall.domain.model.Product;
import com.stacko.mall.domain.model.ProductId;
import com.stacko.mall.domain.model.Stock;
import com.stacko.mall.domain.repository.ProductRepository;
import com.stacko.mall.domain.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StockApplicationService {
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;

    public StockApplicationService(StockRepository stockRepository,
                                   ProductRepository productRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
    }

    public Stock set(SetStockCommand command) {
        ProductId productId = new ProductId(command.getProductId());
        Stock stock = stockRepository
                .findByProductId(command.getTenantId(), productId)
                .orElseGet(() -> Stock.create(command.getTenantId(), productId, command.getQuantity()));
        stock.setQuantity(command.getQuantity());
        return stockRepository.save(stock);
    }

    public Stock adjust(AdjustStockCommand command) {
        ProductId productId = new ProductId(command.getProductId());
        Stock stock = stockRepository
                .findByProductId(command.getTenantId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));
        int nextQuantity = stock.getQuantity() + command.getDelta();
        if (nextQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        stock.adjust(command.getDelta());
        return stockRepository.save(stock);
    }

    public Stock get(String tenantId, String productId) {
        return stockRepository
                .findByProductId(tenantId, new ProductId(productId))
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));
    }

    @Transactional
    public List<StockListItem> list(String tenantId) {
        List<Stock> stocks = new ArrayList<>(stockRepository.listByTenant(tenantId));
        Set<ProductId> stockedProductIds = new HashSet<>();
        stocks.forEach(stock -> stockedProductIds.add(stock.getProductId()));

        List<Product> products = productRepository.listByTenant(tenantId);
        for (Product product : products) {
            if (stockedProductIds.add(product.getId())) {
                Stock stock = Stock.create(tenantId, product.getId(), 0);
                stocks.add(stockRepository.save(stock));
            }
        }

        Map<String, String> productNames = products.stream()
                .collect(Collectors.toMap(product -> product.getId().value(), Product::getName));
        return stocks.stream()
                .map(stock -> new StockListItem(
                        stock,
                        productNames.get(stock.getProductId().value())
                ))
                .toList();
    }
}
