package com.stacko.mall.interfaces.web.c;

import com.stacko.mall.application.service.StockApplicationService;
import com.stacko.mall.domain.model.Stock;
import com.stacko.mall.interfaces.web.view.StockResponse;
import com.stacko.user.contract.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("cStockController")
@RequestMapping("/api/c/stocks")
@Tag(name = "商城-C端", description = "库存查询接口")
public class StockController {
    private final StockApplicationService stockApplicationService;

    public StockController(StockApplicationService stockApplicationService) {
        this.stockApplicationService = stockApplicationService;
    }

    @GetMapping("/{productId}")
    public ApiResponse<StockResponse> get(@RequestHeader("X-Tenant-ID") String tenantId,
                                          @PathVariable("productId") String productId) {
        Stock stock = stockApplicationService.get(tenantId, productId);
        return ApiResponse.ok(StockResponse.from(stock));
    }
}
