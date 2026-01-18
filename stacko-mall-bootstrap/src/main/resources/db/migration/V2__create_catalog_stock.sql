CREATE TABLE IF NOT EXISTS catalog_stock (
    product_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (product_id),
    INDEX idx_catalog_stock_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
