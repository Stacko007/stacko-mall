# stacko-mall 接入用户中心

更新日期：2026-07-24。

## 当前架构

`stacko-mall` 是独立业务服务，不依赖用户中心代码模块，不保存账号密码和 RBAC 主数据。

- 用户中心负责注册、登录、租户成员和角色权限。
- Gateway 读取用户中心 Sa-Token Session，完成认证和租户校验。
- Gateway 为通过认证的请求生成包含权限集合的 V3 签名身份信封。
- 商城验证签名身份并执行商城管理接口授权，不连接认证 Redis，也不使用 Sa-Token。
- 商城继续负责订单归属、库存、支付、售后等业务规则。

浏览器只能通过 Gateway 调用后端：

```text
前端 -> Gateway -> 用户中心
前端 -> Gateway -> 商城
```

## 可信身份协议

Gateway 请求头：

```text
X-Stacko-Identity: base64url(payload).base64url(HMAC-SHA256(payload))
X-Tenant-ID: stacko-mall
X-Request-Id: ...
```

V3 payload 字段：

```json
{
  "version": 3,
  "accountId": "3",
  "membershipId": "7",
  "tenantId": "stacko-mall",
  "username": "alice",
  "permissions": [
    "mall:product:list"
  ],
  "issuedAt": 1784793600,
  "method": "GET",
  "path": "/mall/api/admin/products"
}
```

商城验签时必须同时满足：

1. HMAC-SHA256 签名正确。
2. `version` 等于 3。
3. `issuedAt` 未超过 30 秒，未来时钟偏差不超过 5 秒。
4. HTTP 方法与信封一致。
5. `/mall + 商城当前请求路径` 与信封原始路径一致。
6. `X-Tenant-ID` 与信封租户一致。

V1 没有绑定方法和路径，V2 没有权限集合；商城只接受当前 V3。

## 权限边界

Gateway 不维护商城业务接口到权限码的映射。商城从签名身份读取权限，通过
`@RequiresPermission` 和 `PermissionAspect` 执行管理端功能授权。

商城已经删除旧实现：

- 旧的 Sa-Token `PermissionAspect`
- `LocalPermissionChecker`
- `MallStpInterface`
- `SaTokenCurrentUserProvider`
- Sa-Token 和认证 Redis 依赖

商城仍保留：

- 所有 `/api/admin/**` 和 `/api/c/orders/**` 必须携带有效 Gateway 身份。
- `/api/admin/**` 方法必须声明 `@RequiresPermission`，漏注解默认返回 403。
- 订单只能由当前商城会员查询、支付和取消。
- 售后申请和查询必须关联当前商城会员自己的订单。
- 租户 ID 必须与签名身份一致。
- 库存、支付、订单、售后的领域状态转换校验。

## 配置

Gateway 与商城必须使用相同的身份签名密钥：

```text
STACKO_GATEWAY_IDENTITY_SECRET
```

商城配置：

```yaml
stacko:
  mall:
    gateway-identity:
      signing-secret: ${STACKO_GATEWAY_IDENTITY_SECRET}
      max-age: 30s
      allowed-clock-skew: 5s
      gateway-path-prefix: /mall
      protected-paths:
        - /api/admin/**
        - /api/c/orders/**
        - /api/c/after-sales/**
```

生产环境密钥必须由密钥管理系统注入，至少 32 字节。商城不再需要 `spring.data.redis` 或 `sa-token` 配置。

## 本地联调

端口约定：

- Gateway：`8088`，第二实例 `18088`
- 用户中心：`8080`
- 商城：`8081`
- 商城 C 端前端：`5173`
- 商城管理端前端：`5174`
- 用户中心管理前端：`5175`

前端继续携带 Bearer Token，但商城不解析该 Token。Gateway 完成认证后注入可信身份。

## 商城权限码

| 模块 | 权限码 | 用途 |
| --- | --- | --- |
| 商品 | `mall:product:list` | 商品列表 |
| 商品 | `mall:product:read` | 商品详情 |
| 商品 | `mall:product:create` | 创建商品 |
| 商品 | `mall:product:update` | 更新商品 |
| 订单 | `mall:order:list` | 订单列表 |
| 订单 | `mall:order:read` | 订单详情 |
| 订单 | `mall:order:ship` | 订单发货 |
| 订单 | `mall:order:close` | 关闭订单 |
| 库存 | `mall:stock:list` | 库存列表 |
| 库存 | `mall:stock:read` | 库存详情 |
| 库存 | `mall:stock:set` | 设置库存 |
| 库存 | `mall:stock:adjust` | 调整库存 |
| 售后 | `mall:afterSales:read` | 售后详情 |
| 售后 | `mall:afterSales:review` | 售后审核 |
| 售后 | `mall:afterSales:refund` | 售后退款 |
| 支付 | `mall:payment:read` | 支付详情 |

权限初始化脚本仍为 `docs/seed-mall-admin-module-permissions.sql`。

## C 端会员

C 端订单接口从签名身份取得 `accountId` 和 `membershipId`，再查找或创建 `mall_member`。商城表分别保存 `account_id` 和 `membership_id`，其中 `(tenant_id, membership_id)` 唯一。

订单统一使用 `mall_member.id` 作为 `buyerId`。订单查询、支付、取消和售后只按该商城会员 ID 执行归属校验，不再兼容旧用户中心 ID。

## 部署顺序

1. 确认 Gateway 与商城使用相同签名密钥。
2. 执行 `phase-h0-mall-member-identity.sql`，确认没有未解析身份。
3. 重启全部 Gateway，再重启商城。
4. 完成通过 Gateway、绕过 Gateway、伪造头、跨路径重放和 C 端订单验收。
5. 生产网络只暴露 Gateway，禁止客户端访问商城服务端口。

阶段 F 详细记录见 `phase-f-trusted-gateway-identity.md`。
当前授权边界与新功能开发规则见
`../../stacko-gateway/docs/phase-i-authorization-boundary.md`。
