CREATE TABLE IF NOT EXISTS mall_member (
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
    INDEX idx_mall_member_account (account_id),
    INDEX idx_mall_member_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
