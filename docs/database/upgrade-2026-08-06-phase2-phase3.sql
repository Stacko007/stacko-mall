-- stacko-mall phase2/phase3 upgrade script for an existing database.
-- Apply this script to an existing stacko_mall database that was created before
-- shipping-address and product-category features.
--
-- Notes:
-- 1. This is an operational upgrade helper, not the final Flyway migration chain.
-- 2. Phase 8 will turn these structural changes into formal migration files.
-- 3. Review the target database before execution and back it up first.

USE stacko_mall;

CREATE TABLE IF NOT EXISTS mall_shipping_address (
  id VARCHAR(64) NOT NULL,
  tenant_id VARCHAR(64) NOT NULL,
  buyer_id VARCHAR(64) NOT NULL,
  receiver_name VARCHAR(64) NOT NULL,
  receiver_phone VARCHAR(32) NOT NULL,
  province VARCHAR(64) NOT NULL,
  city VARCHAR(64) NOT NULL,
  district VARCHAR(64) NULL,
  detail_address VARCHAR(256) NOT NULL,
  default_address TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_mall_shipping_address_buyer (tenant_id, buyer_id),
  KEY idx_mall_shipping_address_default (tenant_id, buyer_id, default_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS mall_product_category (
  id VARCHAR(64) NOT NULL,
  tenant_id VARCHAR(64) NOT NULL,
  parent_id VARCHAR(64) NULL,
  name VARCHAR(128) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL,
  level INT NOT NULL,
  path VARCHAR(512) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_mall_product_category_tenant_parent (tenant_id, parent_id),
  KEY idx_mall_product_category_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Idempotent column additions for MySQL 8.0.
SET @schema_name = DATABASE();

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE mall_order ADD COLUMN receiver_name VARCHAR(64) NULL AFTER tracking_no',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'mall_order' AND COLUMN_NAME = 'receiver_name'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE mall_order ADD COLUMN receiver_phone VARCHAR(32) NULL AFTER receiver_name',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'mall_order' AND COLUMN_NAME = 'receiver_phone'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE mall_order ADD COLUMN receiver_province VARCHAR(64) NULL AFTER receiver_phone',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'mall_order' AND COLUMN_NAME = 'receiver_province'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE mall_order ADD COLUMN receiver_city VARCHAR(64) NULL AFTER receiver_province',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'mall_order' AND COLUMN_NAME = 'receiver_city'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE mall_order ADD COLUMN receiver_district VARCHAR(64) NULL AFTER receiver_city',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'mall_order' AND COLUMN_NAME = 'receiver_district'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE mall_order ADD COLUMN receiver_address VARCHAR(256) NULL AFTER receiver_district',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'mall_order' AND COLUMN_NAME = 'receiver_address'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE mall_product ADD COLUMN category_id VARCHAR(64) NULL AFTER tenant_id',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'mall_product' AND COLUMN_NAME = 'category_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE mall_product ADD KEY idx_mall_product_category (tenant_id, category_id)',
    'SELECT 1')
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'mall_product' AND INDEX_NAME = 'idx_mall_product_category'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Optional bootstrap category for existing products.
-- Uncomment if you want all existing products to appear under a default category immediately.
--
-- SET @default_category_id = 'default-category';
-- INSERT INTO mall_product_category
--   (id, tenant_id, parent_id, name, sort, status, level, path, created_at, updated_at)
-- SELECT @default_category_id, tenant_id, NULL, '默认类目', 0, 'ENABLED', 1,
--        @default_category_id, NOW(6), NOW(6)
-- FROM mall_product
-- GROUP BY tenant_id
-- ON DUPLICATE KEY UPDATE updated_at = updated_at;
--
-- UPDATE mall_product
-- SET category_id = @default_category_id
-- WHERE category_id IS NULL;

-- Run the following section against the user-center database, not stacko_mall.
-- Replace `up_platform` if your user-center schema name is different.
--
-- USE up_platform;
--
-- INSERT INTO up_application_permissions
--   (permission_code, application_code, name, status)
-- VALUES
--   ('mall:category:create', 'stacko-mall', '创建商品类目', 'ENABLED'),
--   ('mall:category:update', 'stacko-mall', '更新商品类目', 'ENABLED'),
--   ('mall:category:delete', 'stacko-mall', '删除商品类目', 'ENABLED'),
--   ('mall:category:list', 'stacko-mall', '商品类目列表', 'ENABLED'),
--   ('mall:category:read', 'stacko-mall', '商品类目详情', 'ENABLED')
-- ON DUPLICATE KEY UPDATE
--   application_code = VALUES(application_code),
--   name = VALUES(name),
--   status = VALUES(status);
--
-- INSERT INTO up_portal_permission_scopes (portal_code, permission_code)
-- VALUES
--   ('stacko-mall-admin', 'mall:category:create'),
--   ('stacko-mall-admin', 'mall:category:update'),
--   ('stacko-mall-admin', 'mall:category:delete'),
--   ('stacko-mall-admin', 'mall:category:list'),
--   ('stacko-mall-admin', 'mall:category:read')
-- ON DUPLICATE KEY UPDATE permission_code = VALUES(permission_code);
