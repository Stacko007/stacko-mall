# 阶段 F：商城消费 Gateway 可信身份

更新日期：2026-07-24。

## 1. 当前状态

代码、配置、依赖清理和自动化测试完成；核心身份链路运行验收通过。不涉及数据库迁移。

## 2. 实施内容

Gateway：

- 身份信封当前为 V3；V3 在 V2 的请求绑定字段上增加签名权限集合。
- V3 签名绑定账号、成员、租户、权限、签发时间、HTTP 方法和原始 Gateway 路径。
- 继续清理客户端传入的 `X-Stacko-Identity`。

商城：

- 新增 `GatewayIdentityFilter` 和 `GatewayIdentityVerifier`。
- `/api/admin/**`、`/api/c/orders/**`、`/api/c/after-sales/**` 默认要求有效 V3 身份。
- `CurrentUserContext` 只读取过滤器写入的请求身份。
- C 端订单接口不再读取 `Authorization`。
- 商城使用本地 `@RequiresPermission` 校验 V3 中的签名权限集合。
- `/api/admin/**` 缺少权限注解时默认拒绝。
- 删除旧 Sa-Token 权限切面、Provider、`MallStpInterface` 和认证诊断器。
- 删除 Sa-Token、Redis starter、Redis DAO 和全部认证 Redis 配置。

## 3. 安全边界

- 签名算法：HMAC-SHA256。
- 身份最大年龄：默认 30 秒，可配置范围 1 到 60 秒。
- 允许未来时钟偏差：默认 5 秒，可配置范围 0 到 10 秒。
- 方法或路径不一致时返回 401。
- 租户不一致时返回 403。
- 公开接口不要求身份，但携带伪造身份头时仍返回 401。
- 商城 `PermissionAspect` 负责管理端功能权限。
- 商城负责资源归属和领域状态校验。
- C 端售后申请和查询校验关联订单属于当前商城会员。

签名身份不能替代网络隔离。生产环境必须禁止公网或客户端直连商城端口，签名密钥只能注入 Gateway 和商城。

## 4. 自动化验证

```text
stacko-gateway: BUILD SUCCESS
stacko-mall: BUILD SUCCESS
```

覆盖：

- V3 字段、权限、方法和原始路径签发。
- 正确验签、篡改签名、过期、未来时间、旧版本拒绝。
- 跨方法和跨路径重放拒绝。
- 受保护路径无身份 401。
- 租户不匹配 403。
- 公开路径无身份放行、伪造身份拒绝。
- `CurrentUserContext` 请求身份与租户隔离。
- 管理接口权限允许、权限拒绝和漏注解默认拒绝。
- 现有管理 Controller 权限注解覆盖检查。
- C 端售后不能操作其他买家的订单。

Reactor 依赖树确认不存在：

```text
cn.dev33:*
spring-boot-starter-data-redis
spring-data-redis
```

## 5. IDEA 发布顺序

必须使用相同的 `STACKO_GATEWAY_IDENTITY_SECRET`：

1. 先重启 `8088`、`18088` 两个 Gateway。
2. 再重启商城。
3. 用户中心不需要重启。
4. 前端不需要修改，仍然访问 Gateway。

不要只重启一侧。V3 商城会拒绝 V2 身份，旧商城也不能解析 V3 权限字段完成本地授权。

## 6. 运行验收

1. 使用有商品和订单权限的商城管理员通过 `8088`、`18088` 请求商品、订单列表，均应返回 200。
2. 使用同一管理员请求没有权限的库存列表，应由商城权限切面返回 403。
3. 使用租户管理员通过 Gateway 请求管理接口，应正常工作。
4. 直接请求 `http://localhost:8081/api/admin/products`，即使携带有效 Bearer Token也应返回 401。
5. 直接请求商城并伪造 `X-Stacko-Identity`，应返回 401。
6. 直接请求商城公开商品接口，不携带身份应正常返回。
7. 通过 Gateway 完成 C 端订单列表、创建、详情、支付或取消流程，订单归属校验应保持正常。
8. 检查商城启动依赖和日志，不应创建 Redis连接、Sa-Token Bean或 Redis健康检查。
9. 可选停止认证 Redis：商城进程和直连公开接口应继续工作；Gateway 缓存未命中认证请求应按 E2 规则返回 503。
10. 生产部署确认负载均衡、防火墙或容器网络只暴露 Gateway，不暴露商城端口。

### 6.1 2026-07-23 实测结果

- `8088`、`18088` Gateway 和 `8081` 商城健康检查均为 `UP`。
- 有效租户管理员令牌经 `8088` 请求商品列表返回 200。
- 同一令牌经 `18088` 请求订单列表返回 200，证明两个 Gateway 实例均可签发商城接受的可信身份。
- 直连 `8081/api/admin/products` 时，即使携带有效 Bearer Token 也返回 401。
- 直连商城并携带伪造 `X-Stacko-Identity` 返回 401。
- 直连 `8081/api/c/orders` 时，仅携带有效 Bearer Token 返回 401。
- 直连公开商品接口、不携带身份时返回 200。
- `8081/actuator/health/redis` 返回 404，商城未装配 Redis 健康组件。
- 响应中的 `X-Request-Id` 未重复。

待补充：

- 原受限管理员令牌已失效，无法复验商品/订单 200、库存 403 的权限组合；需要该账号重新登录后补验。
- C 端订单写流程和停止 Redis 的故障场景尚未在本次验收中执行。

## 7. H0 最终收口

- Gateway 的商城路由已删除下游 `Authorization`。
- Gateway 不再兼容缺少 `accountId` 或 `membershipId` 的旧 Session。
- 商城已删除旧买家 ID 查询和归属兼容。
- `mall_member` 已改为明确保存 `account_id` 和 `membership_id`。
- 系统尚未上线，不保留旧认证版本回滚分支。

## 8. 下一阶段

执行 H0 数据库脚本并完成重新登录、成员、订单和售后冒烟。阶段 G 事件架构暂缓。

## 9. 阶段 I 更新

Gateway 业务权限注册表已删除，可信身份升级到 V3。商城管理端使用
`PermissionAspect` 和 `@RequiresPermission` 执行功能权限。
后续新增商城管理接口不再修改 Gateway，完整开发规则见
`../../stacko-gateway/docs/phase-i-authorization-boundary.md`。
