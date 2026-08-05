# Stacko Mall

Stacko Mall 是接入 Stacko 用户中心的多租户商城业务服务，负责商品、库存、订单、支付和售后。账号、登录、租户成员和权限分配由用户中心负责。

## 文档

- [架构设计](docs/architecture.md)：业务边界、请求安全链和数据流
- [配置与启动](docs/configuration.md)：环境变量、Profile、双实例和双前端
- [数据库说明](docs/database.md)：表职责、建库和变更规则
- [业务开发计划](docs/development-plan.md)：商城功能完善阶段、范围和验收点
- [待完善事项](docs/roadmap.md)：上线前必须完成和后续演进项
- [冒烟测试](scripts/smoke/README.md)

## 工程结构

| 模块 | 职责 |
| --- | --- |
| `stacko-mall-domain` | 商品、库存、订单、支付和售后领域模型 |
| `stacko-mall-application` | 业务用例、事务和定时任务 |
| `stacko-mall-infra` | MyBatis-Plus 持久化实现 |
| `stacko-mall-interfaces` | C 端、管理端 HTTP 接口和 Gateway 身份校验 |
| `stacko-mall-bootstrap` | Spring Boot 启动、配置和生产校验 |
| `stacko-mall-frontend` | 商城 C 端，默认端口 `5173` |
| `stacko-mall-admin-frontend` | 商城管理端，默认端口 `5174` |

## 本地启动

后端依赖 Java 17、MySQL 和 Nacos：

```bash
mvn clean package -DskipTests
```

在 IDEA 中启动 `StackoMallApplication`，默认 Profile 为 `jd`、端口为 `8081`。客户端必须通过 Gateway 的 `/mall/**` 路由访问。

前端：

```bash
cd stacko-mall-frontend
npm install
npm run dev
```

```bash
cd stacko-mall-admin-frontend
npm install
npm run dev
```

详细环境变量见[配置与启动](docs/configuration.md)。

## 验证

```bash
mvn test
```

生产环境不得把商城端口直接暴露给公网，否则攻击者可能绕过 Gateway 的统一认证入口。
