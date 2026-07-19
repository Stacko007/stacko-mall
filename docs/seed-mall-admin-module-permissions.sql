-- 商城后台订单、库存、售后、支付查询权限初始化脚本。
-- 执行前将租户编码改为实际值；脚本只维护权限定义，不自动绑定角色或用户。
SET @tenant_id = 'stacko-mall';

INSERT INTO up_permissions (code, name, tenant_id)
VALUES ('mall:order:list', '订单列表', @tenant_id),
       ('mall:order:read', '订单详情', @tenant_id),
       ('mall:order:ship', '订单发货', @tenant_id),
       ('mall:order:close', '关闭订单', @tenant_id),
       ('mall:stock:list', '库存列表', @tenant_id),
       ('mall:stock:read', '库存详情', @tenant_id),
       ('mall:stock:set', '设置库存', @tenant_id),
       ('mall:stock:adjust', '调整库存', @tenant_id),
       ('mall:afterSales:read', '售后查询', @tenant_id),
       ('mall:afterSales:review', '售后审核', @tenant_id),
       ('mall:afterSales:refund', '售后退款', @tenant_id),
       ('mall:payment:read', '支付查询', @tenant_id)
ON DUPLICATE KEY UPDATE name = VALUES(name);

SELECT code, name, tenant_id
FROM up_permissions
WHERE tenant_id = @tenant_id
  AND code IN ('mall:order:list',
               'mall:order:read',
               'mall:order:ship',
               'mall:order:close',
               'mall:stock:list',
               'mall:stock:read',
               'mall:stock:set',
               'mall:stock:adjust',
               'mall:afterSales:read',
               'mall:afterSales:review',
               'mall:afterSales:refund',
               'mall:payment:read')
ORDER BY code;
