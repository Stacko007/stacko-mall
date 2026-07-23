-- Phase H0: normalize mall_member identity columns.
-- Run as a MySQL account that can read up_platform and alter stacko_mall.
-- Adjust schema names if your databases use different names.

USE stacko_mall;

ALTER TABLE mall_member
  CHANGE COLUMN stacko_user_id membership_id BIGINT NOT NULL,
  ADD COLUMN account_id BIGINT NULL AFTER tenant_id;

UPDATE stacko_mall.mall_member mm
JOIN up_platform.up_tenant_memberships um
  ON um.id = mm.membership_id
 AND um.tenant_id = mm.tenant_id
SET mm.account_id = um.account_id
WHERE mm.account_id IS NULL;

-- This result must be 0 before executing the final ALTER TABLE.
SELECT COUNT(*) AS unresolved_member_identities
FROM mall_member
WHERE account_id IS NULL;

ALTER TABLE mall_member
  MODIFY COLUMN account_id BIGINT NOT NULL,
  DROP INDEX uk_mall_member_user,
  ADD UNIQUE KEY uk_mall_member_membership (tenant_id, membership_id),
  ADD INDEX idx_mall_member_account (account_id);

SELECT id, tenant_id, account_id, membership_id, username, status
FROM mall_member
ORDER BY tenant_id, id;
