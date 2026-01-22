package com.stacko.mall.interfaces.web.admin;

import com.stacko.mall.application.command.AdjustStockCommand;
import com.stacko.mall.application.command.SetStockCommand;
import com.stacko.mall.application.service.StockApplicationService;
import com.stacko.mall.domain.model.Stock;
import com.stacko.mall.interfaces.web.dto.StockAdjustRequest;
import com.stacko.mall.interfaces.web.dto.StockSetRequest;
import com.stacko.mall.interfaces.web.view.StockResponse;
import com.stacko.user.contract.security.RequiresPermission;
import com.stacko.user.contract.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController("adminStockController")
@RequestMapping("/api/admin/stocks")
@Tag(name = "商城-管理端", description = "库存管理接口")
public class StockController {
    private final StockApplicationService stockApplicationService;

    public StockController(StockApplicationService stockApplicationService) {
        this.stockApplicationService = stockApplicationService;
    }

    @PutMapping("/{productId}")
    @RequiresPermission("mall:stock:set")
    public ApiResponse<StockResponse> set(@RequestHeader("X-Tenant-ID") String tenantId,
                                          @PathVariable("productId") String productId,
                                          @Valid @RequestBody StockSetRequest request) {
        SetStockCommand command = new SetStockCommand();
        command.setTenantId(tenantId);
        command.setProductId(productId);
        command.setQuantity(request.getQuantity());
        Stock stock = stockApplicationService.set(command);
        return ApiResponse.ok(StockResponse.from(stock));
    }

    @PostMapping("/{productId}/adjust")
    @RequiresPermission("mall:stock:adjust")
    public ApiResponse<StockResponse> adjust(@RequestHeader("X-Tenant-ID") String tenantId,
                                             @PathVariable("productId") String productId,
                                             @Valid @RequestBody StockAdjustRequest request) {
        AdjustStockCommand command = new AdjustStockCommand();
        command.setTenantId(tenantId);
        command.setProductId(productId);
        command.setDelta(request.getDelta());
        Stock stock = stockApplicationService.adjust(command);
        return ApiResponse.ok(StockResponse.from(stock));
    }

    @GetMapping("/{productId}")
    @RequiresPermission("mall:stock:read")
    public ApiResponse<StockResponse> get(@RequestHeader("X-Tenant-ID") String tenantId,
                                          @PathVariable("productId") String productId) {
        Stock stock = stockApplicationService.get(tenantId, productId);
        return ApiResponse.ok(StockResponse.from(stock));
    }

    @GetMapping
    @RequiresPermission("mall:stock:list")
    public ApiResponse<List<StockResponse>> list(@RequestHeader("X-Tenant-ID") String tenantId) {
        List<StockResponse> responses = stockApplicationService.list(tenantId).stream()
                .map(StockResponse::from)
                .toList();
        return ApiResponse.ok(responses);
    }
}
