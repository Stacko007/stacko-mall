# 商城配置与启动

## 1. 配置文件

| 文件 | 用途 |
| --- | --- |
| `application.yml` | 端口、Nacos、Gateway 身份、订单任务和支付回调 |
| `application-jd.yml` | 本地开发，MySQL 使用 P6Spy |
| `application-prod.yml` | 生产数据源、关闭 Swagger 和配置校验 |

默认 Profile 为 `${STACKO_MALL_PROFILE:jd}`。公共配置先加载，再由 Profile 文件覆盖。

## 2. 环境变量

### 应用与数据库

| 变量 | 说明 | 本地默认 |
| --- | --- | --- |
| `STACKO_MALL_PROFILE` | Spring Profile | `jd` |
| `STACKO_MALL_PORT` | HTTP 端口 | `8081` |
| `STACKO_MALL_DB_URL` | 商城 JDBC URL | 本机 `stacko_mall` |
| `STACKO_MALL_DB_USERNAME` | 商城数据库账号 | `stacko_mall` |
| `STACKO_MALL_DB_PASSWORD` | 商城数据库密码 | 空 |
| `SQL_LOG_LEVEL` | P6Spy SQL 日志级别 | `warn` |

### Nacos

| 变量 | 说明 | 本地默认 |
| --- | --- | --- |
| `STACKO_NACOS_ENABLED` | 是否启用服务注册 | `true` |
| `STACKO_NACOS_SERVER_ADDR` | Nacos 地址 | `127.0.0.1:8848` |
| `STACKO_NACOS_NAMESPACE` | 命名空间 ID；public 留空 | 空 |
| `STACKO_NACOS_GROUP` | 服务分组 | `DEFAULT_GROUP` |
| `STACKO_NACOS_USERNAME` | Nacos 账号 | 空 |
| `STACKO_NACOS_PASSWORD` | Nacos 密码 | 空 |
| `STACKO_NACOS_HEALTH_INDICATOR_ENABLED` | Nacos 健康指标 | `true` |

### 身份和支付

| 变量 | 说明 | 要求 |
| --- | --- | --- |
| `STACKO_GATEWAY_IDENTITY_SECRET` | Gateway V4 身份签名密钥 | 与所有 Gateway 实例一致，生产至少 32 字符 |
| `STACKO_MALL_GATEWAY_IDENTITY_MAX_AGE` | 身份头最大年龄 | `30s` |
| `STACKO_MALL_GATEWAY_IDENTITY_CLOCK_SKEW` | 允许时钟偏差 | `5s` |
| `STACKO_MALL_PAYMENT_CALLBACK_SECRET` | 模拟支付回调密钥 | 生产至少 32 字符 |

身份签名密钥应由 Gateway 的密钥工具生成并通过 Secret 管理。商城不需要 Redis 和 Sa-Token 环境变量。

商城固定校验：

```text
applicationCode = stacko-mall
管理端 portalCode/audience = stacko-mall-admin
用户端 portalCode/audience = stacko-mall-web
```

这些值是服务安全边界，不是前端可选参数。

## 3. 订单超时任务

| Spring 属性 | 默认值 | 说明 |
| --- | --- | --- |
| `order.timeout.job-enabled` | `true` | 是否扫描超时订单 |
| `order.timeout.minutes` | `30` | 未支付订单超时分钟数 |
| `order.timeout.batch-size` | `200` | 单次扫描数量 |
| `order.timeout.fixed-delay-ms` | `60000` | 扫描间隔 |

多实例环境当前应只在一个实例启用任务，其他实例通过启动参数设置：

```text
--order.timeout.job-enabled=false
```

在引入分布式锁或统一调度前，不要让多个实例同时执行库存释放任务。

## 4. IDEA 启动

在 `StackoMallApplication` Run Configuration 设置数据库、Nacos 和两个密钥后启动。确认 Nacos 出现 `stacko-mall` 健康实例。

第二实例复制启动项并修改：

```text
STACKO_MALL_PORT=18081
```

数据库、Nacos和身份密钥必须共用。按上节要求只保留一个订单超时任务执行实例。

## 5. 前端

C 端：

```bash
cd stacko-mall-frontend
npm install
npm run dev
```

默认端口 `5173`。

管理端：

```bash
cd stacko-mall-admin-frontend
npm install
npm run dev
```

默认端口 `5174`。

两个前端默认把 `/user` 和 `/mall` 代理到 `http://localhost:8088`。覆盖 Gateway：

```bash
VITE_GATEWAY_TARGET=http://127.0.0.1:18088 npm run dev
```

前端不能直接连接商城 `8081` 或用户中心 `8080`。

管理端和用户端使用不同的 Local Storage 键。登录请求分别固定携带
`stacko-mall-admin` 和 `stacko-mall-web`，并通过 `/user/api/users/me` 再次确认门户
和 audience。

## 6. 生产校验

`prod` Profile 启动时会拒绝：

- P6Spy 数据源
- 缺失或弱数据库密码
- 未启用、未鉴权的 Nacos
- 少于 32 字符或常见默认值的身份签名密钥
- 少于 32 字符或常见默认值的支付回调密钥
- 开启 Swagger 或 Swagger 租户绕过

敏感值不写入 Git、镜像或 Nacos 明文配置。

## 7. 健康与日志

Actuator 暴露 `health`、`info` 和 `metrics`。生产中应由内网监控采集，不直接暴露公网。

Gateway 传入的 `X-Request-Id` 作为 traceId 贯穿商城日志。日志不得记录完整 Bearer Token、签名身份头、支付密钥或个人敏感信息。
