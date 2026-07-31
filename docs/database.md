# 商城数据库

## 1. 数据库边界

商城使用独立的 `stacko_mall` 数据库。它不与用户中心共表，也不通过跨库外键依赖用户中心。

## 2. 核心表

| 表 | 职责 |
| --- | --- |
| `mall_product` | 商品 |
| `catalog_stock` | 商品库存 |
| `mall_order` | 订单主表 |
| `mall_order_item` | 订单商品快照 |
| `mall_member` | 商城展示所需的成员投影 |
| `mall_payment` | 支付记录 |
| `mall_after_sales` | 售后单 |
| `mall_idempotency` | 幂等处理记录 |
| `mall_order_status_history` | 订单状态历史 |

业务主表均应包含或可严格关联 `tenant_id`，所有仓储查询必须保持租户条件。

## 3. 空库重建

当前完整基线：

```text
docs/database/rebuild-stacko-mall.sql
```

执行示例：

```bash
mysql -h <host> -u <user> -p < docs/database/rebuild-stacko-mall.sql
```

脚本会删除并重建表，只能用于确认可丢弃的空库或灾后重建，不能用于在线升级。

## 4. 权限目录

商城权限定义不在商城数据库维护。新增或修改商城权限时，由平台管理员在用户中心
“应用管理”的 `stacko-mall` 应用权限目录中维护，再配置到对应门户策略，最后由租户管理员
分配给角色或成员。

商城数据库只保存业务数据，不保存角色、权限和登录会话。

## 5. Flyway 现状

`stacko-mall-bootstrap/src/main/resources/db/migration` 中存在 V1-V4，但当前 Profile 明确设置 `spring.flyway.enabled=false`，且迁移链未覆盖所有现有表。

上线前必须完成：

1. 以当前重建脚本为基线核对实际生产结构。
2. 补齐支付、售后、幂等和状态历史等迁移。
3. 在测试库验证从基线到最新版的完整升级。
4. 启用 Flyway，后续迁移文件只增不改。
5. 为索引、数据回填和长事务制定上线窗口及回滚方案。

## 6. 备份与安全

- MySQL 不暴露公网，只允许应用和受控运维来源
- 使用独立最小权限账号，商城账号不能访问用户中心库
- 保留每日全量备份和 binlog
- 备份存放到独立位置并定期恢复演练
- 支付和售后数据按审计要求延长保留周期
- 执行结构脚本前先核对目标主机、端口和数据库名
