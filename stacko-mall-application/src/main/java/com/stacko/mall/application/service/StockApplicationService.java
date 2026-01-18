package com.stacko.mall.application.service;

import com.stacko.mall.application.command.AdjustStockCommand;
import com.stacko.mall.application.command.SetStockCommand;
import com.stacko.mall.domain.model.ProductId;
import com.stacko.mall.domain.model.Stock;
import com.stacko.mall.domain.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockApplicationService {
    private final StockRepository stockRepository;

    public StockApplicationService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
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

    public List<Stock> list(String tenantId) {
        return stockRepository.listByTenant(tenantId);
    }
}
