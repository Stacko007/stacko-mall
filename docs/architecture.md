# 商城架构设计

## 1. 定位与边界

商城负责：

- 商品和库存
- 订单及订单状态历史
- 模拟支付、回调幂等
- 售后申请、审核和退款
- 商城业务成员展示投影

商城不负责账号密码、登录、租户成员、角色和权限分配，也不直接读取 Sa-Token Redis。

## 2. 系统拓扑

```mermaid
flowchart LR
    C["商城 C 端"] --> GW["Stacko Gateway"]
    A["商城管理端"] --> GW
    GW -->|"登录、租户切换"| USER["Stacko User"]
    GW -->|"签名身份 + /mall 路由"| MALL["Stacko Mall"]
    MALL --> MDB[("stacko_mall MySQL")]
    USER --> UDB[("用户中心 MySQL")]
    USER --> REDIS[("认证 Redis")]
    GW --> REDIS
    GW -. "Nacos 发现" .-> NACOS[("Nacos")]
    MALL -. "注册实例" .-> NACOS
```

所有外部请求从 Gateway 进入。Gateway 验证 Bearer Token 后生成 `X-Stacko-Identity`，商城只接受校验通过的身份。

## 3. 分层结构

```mermaid
flowchart TD
    BOOT["bootstrap\n启动与生产配置校验"] --> IFACE["interfaces\nController、Filter、Aspect"]
    BOOT --> INFRA["infra\nMapper、Repository"]
    IFACE --> APP["application\n用例、事务、任务"]
    INFRA --> APP
    APP --> DOMAIN["domain\n模型、状态规则、仓储接口"]
```

业务状态流转放在领域和应用层，Controller 不直接操作 Mapper。

## 4. 请求安全链

```mermaid
sequenceDiagram
    participant C as Client
    participant G as Gateway
    participant F as GatewayIdentityFilter
    participant P as PermissionAspect
    participant S as Application Service

    C->>G: Bearer Token + X-Tenant-ID
    G->>G: 解析认证 Session
    G->>G: V3 HMAC 签名身份
    G->>F: 转发请求，移除 Authorization
    F->>F: 校验签名、时效、method、path、tenant
    F->>P: 绑定 CurrentUserContext
    P->>P: 检查 @RequiresPermission
    P->>S: 执行业务用例
    S-->>C: 经 Gateway 返回
```

`GatewayIdentityFilter` 负责认证身份的真实性，`PermissionAspect` 负责方法所需权限。二者职责不同，缺一不可。

受保护前缀默认为：

- `/api/admin/**`
- `/api/c/orders/**`
- `/api/c/after-sales/**`

这些路径缺少合法签名身份时默认拒绝。管理端 Controller 的所有 public 方法必须声明 `@RequiresPermission`，`AdminPermissionCoverageTest` 用于防止新增接口漏标。

Gateway 不再维护 URL 到业务权限码的映射。新增功能只需在商城方法声明权限，并把权限码加入用户中心权限目录。

## 5. 多租户规则

- Gateway 身份中包含 `tenantId`、`accountId` 和 `membershipId`。
- 请求的 `X-Tenant-ID` 必须与签名身份租户一致。
- 商城主表必须包含 `tenant_id`，仓储查询必须带租户条件。
- `buyer_id` 等主体引用不能替代 `tenant_id`。
- 用户名等展示字段可做本地快照，但不参与鉴权。

## 6. 业务模块

```mermaid
flowchart LR
    PRODUCT["商品"] --> STOCK["库存"]
    PRODUCT --> ORDER["订单"]
    STOCK --> ORDER
    ORDER --> PAYMENT["支付"]
    PAYMENT --> AFTER["售后"]
    ORDER --> HISTORY["状态历史"]
    ORDER --> IDEMP["幂等记录"]
```

### 商品与库存

商品和库存分表维护。创建商品不会自动代表库存充足，需要显式设置或调整库存。下单时校验并扣减库存，关闭订单或退款流程按业务规则释放库存。

### 订单

订单保存商品名称和价格快照，避免商品后续修改影响历史订单。定时任务扫描超时 `CREATED` 订单并关闭。

### 支付与售后

当前支付是模拟实现，回调通过独立 HMAC 密钥校验并使用幂等记录防止重复处理。售后覆盖申请、审核和退款主流程。

## 7. 权限目录

当前管理端权限包括：

- `mall:product:create/update/read/list`
- `mall:stock:set/adjust/read/list`
- `mall:order:list/read/ship/close`
- `mall:payment:read`
- `mall:afterSales:read/review/refund`

权限定义保存于用户中心数据库。种子 SQL 见 `docs/database/seed-mall-admin-permissions.sql`。

## 8. 当前遗留点

1. **数据库迁移不完整**：资源中已有 V1-V4，但 Flyway 被关闭，且后续支付、售后等表未形成完整迁移链。
2. **支付仍为 Mock**：未接真实支付渠道、异步通知验签证书、对账、退款单和资金审计。
3. **多实例定时任务**：每个实例都会扫描超时订单，目前没有分布式锁或统一调度；并发关闭可能重复释放库存，生产前必须处理。
4. **并发库存**：需要继续用压力测试验证扣减、释放及退款场景的锁和幂等策略。
5. **用户投影同步**：商城成员展示信息没有可靠事件流，账号资料变更可能延迟或不一致。
6. **身份密钥轮换**：只支持单 HMAC 密钥，缺少 `keyId` 和新旧密钥并行验证。
7. **事件可靠性**：订单、支付、售后之间尚无 Outbox 或消息最终一致性方案。
8. **生产韧性**：尚缺限流、熔断、业务指标告警、压测基线和完整灾备演练。
9. **端口隔离**：商城服务端口必须只允许 Gateway 和运维网络访问。
