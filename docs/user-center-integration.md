# stacko-mall 接入用户中心记录

## 当前方向

`stacko-mall` 不再嵌入 `stacko-user-starter` 或 `stacko-user-interfaces`。商城作为独立业务应用运行，只通过用户中心 HTTP API 完成鉴权和权限校验。

## 已调整内容

- `stacko-mall-bootstrap` 去掉 `stacko-user-starter`、`stacko-user-interfaces` 依赖。
- `StackoMallApplication` 只扫描 `com.stacko.mall`。
- `stacko-mall-interfaces` 去掉 `stacko-user-contract`、`stacko-user-shared-web` 依赖。
- 商城侧新增本地 `ApiResponse`，避免 API 响应结构依赖用户中心代码包。
- 商城侧新增本地 `RequiresPermission`，管理端 controller 继续用注解表达业务权限。
- 商城侧新增 `UserCenterAclClient`，通过 `/api/acl/check` 调用户中心校验权限。

## 用户中心配置

```yaml
mall:
  user-center:
    enabled: true
    base-url: http://localhost:8080
    acl-check-path: /api/acl/check
    current-user-path: /api/auth/current
```

`base-url` 指向 `stacko-user-bootstrap` 的服务地址。

`enabled=false` 不会放行管理端权限，而是直接拒绝权限校验，避免误配置导致管理端绕过用户中心。

## 本地联调启动

当前联调端口约定：

- 用户中心后端：`http://localhost:8080`
- 商城后端：`http://localhost:8081`
- 商城 C 端前端：`http://localhost:5173`
- 商城后台前端：`http://localhost:5174`
- 用户中心管理前端：`http://localhost:5175`

商城两个前端的 Vite 代理规则：

- `/api/auth/**` 转发到用户中心后端。
- 其他 `/api/**` 转发到商城后端。

这样登录、注册、刷新 token 由用户中心处理，商品、订单等业务接口由商城处理。

本次联调用户中心使用 memory 模式启动，演示账号为：

- 租户：`stacko-mall`
- 用户名：`stacko001`
- 密码：`123456`

注意：memory 模式数据随进程停止丢失。当前演示账号可用于登录和 C 端当前用户解析；如需完整验证商城后台受保护接口，需要在用户中心为该账号补齐商城业务权限码。

## 权限校验流程

1. 管理端接口标注商城本地 `@RequiresPermission("mall:xxx")`。
2. `PermissionAspect` 从当前请求读取：
   - `Authorization`
   - `X-Tenant-ID`
3. `UserCenterAclClient` 透传 token 和租户，调用用户中心 `/api/acl/check`。
4. 用户中心负责解析 token、校验租户一致性、判断权限码。
5. 用户中心返回 `true` 才允许执行业务接口。

用户中心只使用 token 确认当前用户身份，权限判断会按用户 ID 实时读取数据库中的直接权限和角色权限。因此角色授权或撤权后不需要重新登录即可生效，已禁用用户也不能继续通过 ACL 校验。

`/api/acl/check` 只要求有效登录态，并限制只能检查 token 所属用户在同一租户下的权限，不再要求额外的 `acl:check` 权限。

历史数据库中若已存在 `acl:check`，它不会影响运行。确认没有其他系统依赖后，可选执行以下 SQL 清理：

```sql
DELETE FROM up_role_permissions WHERE permission_code = 'acl:check';
DELETE FROM up_user_permissions WHERE permission_code = 'acl:check';
DELETE FROM up_permissions WHERE code = 'acl:check';
```

## 商城后台模块权限

库存、售后和支付查询模块当前使用以下权限码：

| 模块 | 权限码 | 用途 |
| --- | --- | --- |
| 订单 | `mall:order:list` | 订单列表 |
| 订单 | `mall:order:read` | 订单详情 |
| 订单 | `mall:order:ship` | 订单发货 |
| 订单 | `mall:order:close` | 关闭订单 |
| 库存 | `mall:stock:list` | 库存列表 |
| 库存 | `mall:stock:read` | 库存详情 |
| 库存 | `mall:stock:set` | 设置库存 |
| 库存 | `mall:stock:adjust` | 调整库存 |
| 售后 | `mall:afterSales:read` | 售后详情查询 |
| 售后 | `mall:afterSales:review` | 售后审核 |
| 售后 | `mall:afterSales:refund` | 售后退款 |
| 支付 | `mall:payment:read` | 支付详情查询 |

执行 `docs/seed-mall-admin-module-permissions.sql` 可将上述权限定义写入用户中心的 `up_permissions` 表。脚本可重复执行，只更新权限名称，不会自动给角色或用户授权；执行后在用户中心后台按租户分配即可。

## C 端当前用户流程

C 端订单接口不再信任请求体里的 `buyerId`：

1. C 端请求携带 `Authorization` 和 `X-Tenant-ID`。
2. 商城后端调用用户中心 `GET /api/auth/current`。
3. 商城后端按用户中心返回的 `id` 查找或创建 `mall_member`。
4. 商城后端使用 `mall_member.id` 作为订单 `buyerId`。
5. 订单查询、支付、取消前都会校验订单 `buyerId` 等于当前会员 ID；历史订单兼容旧的用户中心 userId。

`OrderCreateRequest.buyerId` 暂时保留为兼容字段，但后端忽略它。后续前端和接口文档稳定后可以删除。

## 数据边界

商城库不保存账号密码和 RBAC 主数据。

商城库已新增业务用户表：

```sql
CREATE TABLE mall_member (
  id VARCHAR(64) NOT NULL,
  tenant_id VARCHAR(64) NOT NULL,
  stacko_user_id BIGINT NOT NULL,
  username VARCHAR(64) NULL,
  nickname VARCHAR(128) NULL,
  phone VARCHAR(64) NULL,
  email VARCHAR(128) NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mall_member_user (tenant_id, stacko_user_id),
  INDEX idx_mall_member_tenant_status (tenant_id, status)
);
```

当前实现：

- `mall_member.id` 是商城业务会员 ID。
- `mall_member.stacko_user_id` 保存用户中心账号 ID。
- 新订单的 `mall_order.buyer_id` 保存 `mall_member.id`。
- 为兼容历史数据，C 端订单列表和订单归属校验暂时同时兼容旧的用户中心 userId。
- 业务表不跨库建外键。

## 后续待做

1. 可增加 `CurrentUserContext`，统一提供当前 `tenantId/userId/memberId/permissions`，减少 controller 重复调用。
2. 前端彻底移除历史 `buyerId` localStorage。
3. 增加会员管理接口和管理端页面，维护昵称、等级、状态等商城业务字段。
4. 根据实际部署环境配置 `mall.user-center.base-url`。
5. 补充 smoke 测试：登录用户中心、调用商城管理端、缺权限返回 403、有权限返回业务数据。
