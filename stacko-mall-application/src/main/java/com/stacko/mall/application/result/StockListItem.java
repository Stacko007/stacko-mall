package com.stacko.mall.application.result;

import com.stacko.mall.domain.model.Stock;

public record StockListItem(Stock stock, String productName) {
}
