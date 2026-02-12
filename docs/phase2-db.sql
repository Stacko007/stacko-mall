-- Phase 2 DB schema additions (payment, after_sales, idempotency)

-- Payment table
CREATE TABLE IF NOT EXISTS mall_payment (
  id            VARCHAR(64)   NOT NULL PRIMARY KEY,
  tenant_id     VARCHAR(64)   NOT NULL,
  order_id      VARCHAR(64)   NOT NULL,
  amount        DECIMAL(18,2) NOT NULL,
  status        VARCHAR(32)   NOT NULL,
  channel       VARCHAR(32)   NOT NULL,
  trade_no      VARCHAR(64)   NULL,
  raw_callback  TEXT          NULL,
  created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_payment_trade_no (trade_no),
  KEY idx_payment_order_id (order_id),
  KEY idx_payment_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- After-sales table
CREATE TABLE IF NOT EXISTS mall_after_sales (
  id            VARCHAR(64)  NOT NULL PRIMARY KEY,
  tenant_id     VARCHAR(64)  NOT NULL,
  order_id      VARCHAR(64)  NOT NULL,
  payment_id    VARCHAR(64)  NULL,
  type          VARCHAR(32)  NOT NULL,
  reason        VARCHAR(256) NOT NULL,
  status        VARCHAR(32)  NOT NULL,
  remark        VARCHAR(256) NULL,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_after_sales_order_id (order_id),
  KEY idx_after_sales_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Idempotency table
CREATE TABLE IF NOT EXISTS mall_idempotency (
  id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id     VARCHAR(64)  NOT NULL,
  idempotency_key VARCHAR(64) NOT NULL,
  biz_type      VARCHAR(32)  NOT NULL,
  biz_id        VARCHAR(64)  NULL,
  status        VARCHAR(32)  NOT NULL,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_idempotency_key (tenant_id, idempotency_key, biz_type),
  KEY idx_idempotency_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Optional: order status history table for audit
CREATE TABLE IF NOT EXISTS mall_order_status_history (
  id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id     VARCHAR(32)  NOT NULL,
  order_id      VARCHAR(32)  NOT NULL,
  from_status   VARCHAR(32)  NOT NULL,
  to_status     VARCHAR(32)  NOT NULL,
  reason        VARCHAR(256) NULL,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_order_status_order_id (order_id),
  KEY idx_order_status_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
