CREATE TABLE IF NOT EXISTS mall_order (
    id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    buyer_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_amount DECIMAL(18, 2) NOT NULL,
    shipping_carrier VARCHAR(64) NULL,
    tracking_no VARCHAR(128) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    shipped_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_mall_order_tenant (tenant_id),
    INDEX idx_mall_order_buyer (buyer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS mall_order_item (
    id VARCHAR(64) NOT NULL,
    order_id VARCHAR(64) NOT NULL,
    product_id VARCHAR(64) NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    price DECIMAL(18, 2) NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_mall_order_item_order (order_id),
    INDEX idx_mall_order_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
