-- stacko-mall stacko_mall rebuild script
-- Use this only when the database is empty or has already been confirmed lost.
-- It creates every table currently accessed by stacko-mall.

CREATE DATABASE IF NOT EXISTS stacko_mall
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE stacko_mall;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS mall_order_status_history;
DROP TABLE IF EXISTS mall_idempotency;
DROP TABLE IF EXISTS mall_after_sales;
DROP TABLE IF EXISTS mall_payment;
DROP TABLE IF EXISTS mall_member;
DROP TABLE IF EXISTS mall_order_item;
DROP TABLE IF EXISTS mall_order;
DROP TABLE IF EXISTS catalog_stock;
DROP TABLE IF EXISTS mall_product;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE mall_product (
  id VARCHAR(64) NOT NULL,
  tenant_id VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(512) NULL,
  price DECIMAL(18, 2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_catalog_product_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE catalog_stock (
  product_id VARCHAR(64) NOT NULL,
  tenant_id VARCHAR(64) NOT NULL,
  quantity INT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (product_id),
  KEY idx_catalog_stock_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mall_order (
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
  KEY idx_mall_order_tenant (tenant_id),
  KEY idx_mall_order_buyer (buyer_id),
  KEY idx_mall_order_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mall_order_item (
  id VARCHAR(64) NOT NULL,
  order_id VARCHAR(64) NOT NULL,
  product_id VARCHAR(64) NOT NULL,
  product_name VARCHAR(128) NOT NULL,
  price DECIMAL(18, 2) NOT NULL,
  quantity INT NOT NULL,
  amount DECIMAL(18, 2) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_mall_order_item_order (order_id),
  KEY idx_mall_order_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mall_member (
  id VARCHAR(64) NOT NULL,
  tenant_id VARCHAR(64) NOT NULL,
  account_id BIGINT NOT NULL,
  membership_id BIGINT NOT NULL,
  username VARCHAR(64) NULL,
  nickname VARCHAR(128) NULL,
  phone VARCHAR(64) NULL,
  email VARCHAR(128) NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mall_member_membership (tenant_id, membership_id),
  KEY idx_mall_member_account (account_id),
  KEY idx_mall_member_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mall_payment (
  id VARCHAR(64) NOT NULL,
  tenant_id VARCHAR(64) NOT NULL,
  order_id VARCHAR(64) NOT NULL,
  amount DECIMAL(18, 2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  channel VARCHAR(32) NOT NULL,
  trade_no VARCHAR(64) NULL,
  raw_callback TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payment_trade_no (trade_no),
  KEY idx_payment_order_id (order_id),
  KEY idx_payment_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mall_after_sales (
  id VARCHAR(64) NOT NULL,
  tenant_id VARCHAR(64) NOT NULL,
  order_id VARCHAR(64) NOT NULL,
  payment_id VARCHAR(64) NULL,
  type VARCHAR(32) NOT NULL,
  reason VARCHAR(256) NOT NULL,
  status VARCHAR(32) NOT NULL,
  remark VARCHAR(256) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_after_sales_order_id (order_id),
  KEY idx_after_sales_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mall_idempotency (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id VARCHAR(64) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  biz_type VARCHAR(32) NOT NULL,
  biz_id VARCHAR(64) NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_idempotency_key (tenant_id, idempotency_key, biz_type),
  KEY idx_idempotency_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mall_order_status_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id VARCHAR(64) NOT NULL,
  order_id VARCHAR(64) NOT NULL,
  from_status VARCHAR(32) NOT NULL,
  to_status VARCHAR(32) NOT NULL,
  reason VARCHAR(256) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_order_status_order_id (order_id),
  KEY idx_order_status_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
