# stacko-mall 接入用户中心记录

## 当前方向

`stacko-mall` 不再嵌入 `stacko-user-starter` 或 `stacko-user-interfaces`。商城作为独立业务应用运行，用户和 RBAC 主数据仍由用户中心维护。

用户中心已统一签发 Sa-Token。商城 C 端身份和管理端权限均通过认证 Redis 本地读取 SaSession，正常业务请求不再逐请求调用 `/api/auth/current` 或 `/api/acl/check`。

完整迁移阶段和验收记录见 `stacko-user/docs/sa-token-distributed-auth-refactor-plan.md`。认证迁移已经收口，商城后端不再保留远程鉴权回退链路。

## 已调整内容

- `stacko-mall-bootstrap` 去掉 `stacko-user-starter`、`stacko-user-interfaces` 依赖。
- `StackoMallApplication` 只扫描 `com.stacko.mall`。
- `stacko-mall-interfaces` 去掉 `stacko-user-contract`、`stacko-user-shared-web` 依赖。
- 商城侧新增本地 `ApiResponse`，避免 API 响应结构依赖用户中心代码包。
- 商城侧新增本地 `RequiresPermission`，管理端 controller 继续用注解表达业务权限。
- 商城侧身份和权限只使用共享 Redis 中的 Sa-Token Session。

## 认证配置

商城后端不再需要 `mall.user-center.*` 配置。用户中心与商城必须连接同一个认证 Redis，并统一使用：

- Sa-Token 账号体系：固定为 `stacko-user`
- Token Header：`Authorization`
- Token 前缀：`Bearer`
- Sa-Token Redis DAO：`sa-token-redis-jackson`

## 本地联调启动

当前联调端口约定：

- Gateway：`http://localhost:8088`
- 用户中心后端：`http://localhost:8080`
- 商城后端：`http://localhost:8081`
- 商城 C 端前端：`http://localhost:5173`
- 商城后台前端：`http://localhost:5174`
- 用户中心管理前端：`http://localhost:5175`

三个前端统一通过 Gateway 访问后端。Vite 只代理以下两个前缀到 `http://localhost:8088`：

- `/user/api/**` 由 Gateway 路由到用户中心。
- `/mall/api/**` 由 Gateway 路由到商城。

登录、注册等认证请求使用 `/user/api/**`，商品、订单等业务请求使用 `/mall/api/**`。Nacos 只负责服务注册与发现，不会自动改写浏览器请求；前端不能再直接代理到 `8080` 或 `8081`。

本地默认 Gateway 地址为 `http://localhost:8088`，需要覆盖时在对应前端的 `.env.local` 中设置 `VITE_GATEWAY_TARGET`。`VITE_USER_API_BASE` 和 `VITE_MALL_API_BASE` 只用于部署路径确有差异时覆盖默认的 `/user/api`、`/mall/api`，日常本地联调不需要设置。

当前联调用户中心使用 MySQL + MyBatis-Plus + Sa-Token + Redis，联调账号为：

- 租户：`stacko-mall`
- 用户名：`stacko001`
- 密码：`123456`

账号、租户和权限维护在用户中心 MySQL；会话保存在 Redis。验证商城后台接口前，需要在用户中心为账号分配对应商城业务权限码。

## 权限校验流程

1. 管理端接口标注商城本地 `@RequiresPermission("mall:xxx")`。
2. `PermissionAspect` 从当前请求读取：
   - `Authorization`
   - `X-Tenant-ID`
3. `CurrentUserContext` 本地读取共享 Redis 中的 SaSession，并校验 token、状态和租户。
4. `MallStpInterface` 向 Sa-Token提供当前 Session中的角色和权限，`LocalPermissionChecker` 通过 `StpLogic` 校验。
5. `SaStrategy.hasElement` 保持完整权限码精确匹配，不启用 Sa-Token默认通配匹配。
6. 匹配成功才允许执行业务接口；同一请求复用已经解析的当前用户。

角色授权、撤权、角色权限修改、用户禁用等操作会在用户中心清理受影响的 SaSession，用户需要重新登录并获得最新权限快照。权限维护必须通过用户中心应用接口完成，不能直接修改数据库绕过会话失效逻辑。

用户中心 `/api/acl/check` 仍可供其他系统兼容或诊断，但商城已经删除对应客户端。

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
2. 商城后端通过 `CurrentUserContext` 本地读取 Sa-Token Session；同一请求只解析一次，并由 `MallStpInterface` 复用该身份完成权限校验。
3. 商城后端按用户中心返回的 `id` 查找或创建 `mall_member`。
4. 商城后端使用 `mall_member.id` 作为订单 `buyerId`。
5. 订单查询、支付、取消前都会校验订单 `buyerId` 等于当前会员 ID；历史订单兼容旧的用户中心 userId。

本地身份解析会校验 Token 有效性、用户状态和租户一致性。Redis 不可用返回 503；无效 Token 返回 401；跨租户访问返回 403。

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

1. 前端彻底移除历史 `buyerId` localStorage。
2. 增加会员管理接口和管理端页面，维护昵称、等级、状态等商城业务字段。
3. 增加真实 Redis 联调 smoke 测试：登录用户中心、调用商城管理端、撤权后旧 Token 失效。
4. 部署环境由负载均衡将 `/user/**` 和 `/mall/**` 统一转发到 Gateway 集群。
