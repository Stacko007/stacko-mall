-- ----------------------------
-- Table structure for up_tenants
-- ----------------------------

CREATE TABLE `up_tenants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` varchar(64) NOT NULL,
  `name` varchar(128) NOT NULL,
  `type` varchar(32) NOT NULL DEFAULT 'TOB',
  `status` varchar(32) NOT NULL DEFAULT 'ENABLED',
  `remark` varchar(255) DEFAULT NULL,
  `config_json` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenants_tenant_id` (`tenant_id`),
  KEY `idx_tenants_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of up_tenants
-- ----------------------------
BEGIN;
INSERT INTO `up_tenants` (`id`, `tenant_id`, `name`, `type`, `status`, `remark`, `config_json`, `created_at`, `updated_at`) VALUES (7, 'platform-root', 'Platform Root', 'TOB', 'ENABLED', 'Bootstrap platform tenant', NULL, '2025-12-28 06:33:22', '2025-12-28 06:33:22');
INSERT INTO `up_tenants` (`id`, `tenant_id`, `name`, `type`, `status`, `remark`, `config_json`, `created_at`, `updated_at`) VALUES (8, 'stacko-mall', '斯塔克商城', 'TOB', 'ENABLED', '', NULL, '2025-12-28 06:38:06', '2025-12-28 06:38:06');
COMMIT;





BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for up_permissions
-- ----------------------------
DROP TABLE IF EXISTS `up_permissions`;
CREATE TABLE `up_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(64) NOT NULL,
  `name` varchar(128) NOT NULL,
  `tenant_id` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_tenant` (`code`,`tenant_id`),
  KEY `fk_permissions_tenant` (`tenant_id`),
  CONSTRAINT `fk_permissions_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `up_tenants` (`tenant_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of up_permissions
-- ----------------------------
BEGIN;
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:role:create', '创建角色', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:role:list', '查看角色', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:role:update', '更新角色', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:role:delete', '删除角色', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:role:grant', '分配角色权限', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:permission:create', '创建权限', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:permission:list', '查看权限', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:permission:update', '更新权限', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:permission:delete', '删除权限', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:user:assign', '分配用户角色/权限', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:read', '读取用户信息', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:create', '创建用户', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:update', '更新用户信息', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:status', '更新用户状态', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:reset-password', '重置用户密码', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:delete', '删除用户', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'auth:kick', '踢出用户', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'auth:ban', '封禁用户', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'platform:tenant:create', '创建租户', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'platform:tenant:list', '查询租户', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'platform:tenant:update', '更新租户', 'platform-root');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:role:create', '创建角色', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:role:list', '查看角色', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:role:update', '更新角色', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:role:delete', '删除角色', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:role:grant', '分配角色权限', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:permission:create', '创建权限', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:permission:list', '查看权限', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:permission:update', '更新权限', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:permission:delete', '删除权限', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'rbac:user:assign', '分配用户角色/权限', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:read', '读取用户信息', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:create', '创建用户', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:update', '更新用户信息', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:status', '更新用户状态', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:reset-password', '重置用户密码', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'user:delete', '删除用户', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'auth:kick', '踢出用户', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'auth:ban', '封禁用户', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'mall:order:create', '创建订单', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'mall:order:list', '查询订单', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'mall:order:edit', '修改订单', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'mall:product:list', '商品浏览', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'mall:cart:use', '购物车使用', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'mall:cart:addproduct', '购物车添加商品', 'stacko-mall');
INSERT INTO `up_permissions` ( `code`, `name`, `tenant_id`) VALUES ( 'mall:cart:delproduct', '购物车删除商品', 'stacko-mall');
COMMIT;

-- ----------------------------
-- Table structure for up_role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `up_role_permissions`;
CREATE TABLE `up_role_permissions` (
  `role_code` varchar(64) NOT NULL,
  `tenant_id` varchar(64) NOT NULL,
  `permission_code` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of up_role_permissions
-- ----------------------------
BEGIN;
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'user:status');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'user:delete');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'rbac:role:create');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'user:reset-password');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'rbac:permission:delete');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'user:update');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'auth:ban');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'rbac:role:list');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'rbac:permission:update');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'rbac:role:update');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'user:create');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'rbac:role:grant');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'user:read');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'rbac:permission:list');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'rbac:role:delete');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'rbac:permission:create');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'rbac:user:assign');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'platform-root', 'auth:kick');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'user:status');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'user:delete');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'rbac:role:create');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'user:reset-password');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'rbac:permission:delete');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'user:update');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'auth:ban');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'rbac:role:list');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'rbac:permission:update');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'rbac:role:update');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'user:create');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'rbac:role:grant');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'user:read');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'rbac:permission:list');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'rbac:role:delete');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'rbac:permission:create');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'rbac:user:assign');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('tenant-admin', 'stacko-mall', 'auth:kick');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-order', 'stacko-mall', 'mall:order:create');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-order', 'stacko-mall', 'mall:order:list');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-order', 'stacko-mall', 'mall:order:edit');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('platform-admin', 'platform-root', 'platform:tenant:create');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('platform-admin', 'platform-root', 'platform:tenant:list');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('platform-admin', 'platform-root', 'platform:tenant:update');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-users', 'stacko-mall', 'mall:order:create');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-users', 'stacko-mall', 'mall:cart:addproduct');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-users', 'stacko-mall', 'mall:order:list');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-users', 'stacko-mall', 'mall:product:list');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-users', 'stacko-mall', 'mall:cart:use');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-oldusers', 'stacko-mall', 'mall:order:create');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-oldusers', 'stacko-mall', 'mall:cart:addproduct');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-oldusers', 'stacko-mall', 'mall:order:list');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-oldusers', 'stacko-mall', 'mall:order:edit');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-oldusers', 'stacko-mall', 'mall:cart:delproduct');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-oldusers', 'stacko-mall', 'mall:product:list');
INSERT INTO `up_role_permissions` (`role_code`, `tenant_id`, `permission_code`) VALUES ('stacko-mall-oldusers', 'stacko-mall', 'mall:cart:use');
COMMIT;

-- ----------------------------
-- Table structure for up_roles
-- ----------------------------
DROP TABLE IF EXISTS `up_roles`;
CREATE TABLE `up_roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(64) NOT NULL,
  `name` varchar(128) NOT NULL,
  `tenant_id` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_tenant` (`code`,`tenant_id`),
  KEY `fk_roles_tenant` (`tenant_id`),
  CONSTRAINT `fk_roles_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `up_tenants` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of up_roles
-- ----------------------------
BEGIN;
INSERT INTO `up_roles` (`id`, `code`, `name`, `tenant_id`) VALUES (8, 'tenant-admin', '租户管理员', 'platform-root');
INSERT INTO `up_roles` (`id`, `code`, `name`, `tenant_id`) VALUES (9, 'platform-admin', '平台管理员', 'platform-root');
INSERT INTO `up_roles` (`id`, `code`, `name`, `tenant_id`) VALUES (10, 'tenant-admin', '租户管理员', 'stacko-mall');
INSERT INTO `up_roles` (`id`, `code`, `name`, `tenant_id`) VALUES (11, 'stacko-mall-order', '斯塔克商城订单管理员', 'stacko-mall');
INSERT INTO `up_roles` (`id`, `code`, `name`, `tenant_id`) VALUES (12, 'stacko-mall-users', '注册用户', 'stacko-mall');
INSERT INTO `up_roles` (`id`, `code`, `name`, `tenant_id`) VALUES (14, 'stacko-mall-oldusers', '老用户', 'stacko-mall');
COMMIT;



-- ----------------------------
-- Table structure for up_user_permissions
-- ----------------------------
DROP TABLE IF EXISTS `up_user_permissions`;
CREATE TABLE `up_user_permissions` (
  `user_id` bigint NOT NULL,
  `tenant_id` varchar(64) NOT NULL,
  `permission_code` varchar(64) NOT NULL,
  PRIMARY KEY (`user_id`,`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of up_user_permissions
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for up_user_roles
-- ----------------------------
DROP TABLE IF EXISTS `up_user_roles`;
CREATE TABLE `up_user_roles` (
  `user_id` bigint NOT NULL,
  `tenant_id` varchar(64) NOT NULL,
  `role_code` varchar(64) NOT NULL,
  PRIMARY KEY (`user_id`,`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of up_user_roles
-- ----------------------------
BEGIN;
INSERT INTO `up_user_roles` (`user_id`, `tenant_id`, `role_code`) VALUES (7, 'platform-root', 'platform-admin');
INSERT INTO `up_user_roles` (`user_id`, `tenant_id`, `role_code`) VALUES (7, 'platform-root', 'tenant-admin');
INSERT INTO `up_user_roles` (`user_id`, `tenant_id`, `role_code`) VALUES (8, 'stacko-mall', 'tenant-admin');
INSERT INTO `up_user_roles` (`user_id`, `tenant_id`, `role_code`) VALUES (9, 'stacko-mall', 'stacko-mall-order');
COMMIT;

-- ----------------------------
-- Table structure for up_users
-- ----------------------------
DROP TABLE IF EXISTS `up_users`;
CREATE TABLE `up_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `phone` varchar(64) DEFAULT NULL,
  `email` varchar(128) DEFAULT NULL,
  `status` varchar(32) NOT NULL,
  `tenant_id` varchar(64) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tenant` (`username`,`tenant_id`),
  KEY `fk_users_tenant` (`tenant_id`),
  CONSTRAINT `fk_users_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `up_tenants` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of up_users
-- ----------------------------
BEGIN;
INSERT INTO `up_users` (`id`, `username`, `password_hash`, `phone`, `email`, `status`, `tenant_id`, `created_at`, `updated_at`) VALUES (7, 'platform-admin', '$2a$10$2LO0.PmkrxY63l1THR0nt.D/7llDXDFkK.YncU465YgWZ2t60t95W', NULL, NULL, 'ACTIVE', 'platform-root', '2025-12-28 06:33:25', '2025-12-28 06:33:25');
INSERT INTO `up_users` (`id`, `username`, `password_hash`, `phone`, `email`, `status`, `tenant_id`, `created_at`, `updated_at`) VALUES (8, 'stacko-mall-admin', '$2a$10$DPREkxsq71T588TKq7ZdJeYn9VJRGjgvb../sVsguy8lW0NAvXFBy', '', '', 'ACTIVE', 'stacko-mall', '2025-12-28 06:38:08', '2025-12-28 06:38:08');
INSERT INTO `up_users` (`id`, `username`, `password_hash`, `phone`, `email`, `status`, `tenant_id`, `created_at`, `updated_at`) VALUES (9, 'stacko-mall-order-manage', '$2a$10$cTWZVkLnuYTXzC2FhCdCEeFm1G6EhFDCXBFX0zy4rdgcv.MVR9XyS', '13603455555', 'test@gmail.com', 'ACTIVE', 'stacko-mall', '2025-12-28 06:38:57', '2026-01-06 15:35:43');
INSERT INTO `up_users` (`id`, `username`, `password_hash`, `phone`, `email`, `status`, `tenant_id`, `created_at`, `updated_at`) VALUES (10, 'stacko001', '$2a$10$dVrd59w60K5ML5W4Rr4VIOB.C51wFSBFHPky4JzQN8xIXcbt9SgqG', NULL, NULL, 'ACTIVE', 'stacko-mall', '2026-01-17 08:01:26', '2026-01-17 08:01:26');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
