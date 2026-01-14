CREATE TABLE IF NOT EXISTS catalog_product (
    id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512) NULL,
    price DECIMAL(18, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_catalog_product_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
