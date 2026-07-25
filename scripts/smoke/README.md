# 商城冒烟测试

## 准备

```bash
cp scripts/smoke/.env.example scripts/smoke/.env
```

填写租户、Token、商品和支付回调密钥。`BASE_URL` 必须指向 Gateway 的商城路由，默认是：

```text
http://localhost:8088/mall
```

## 支付与售后

```bash
bash scripts/smoke/payment_after_sales.sh
```

覆盖下单、支付、回调幂等、支付查询、售后申请、审核、退款和查询。脚本依赖 `curl` 与 `openssl`。

## TraceId

```bash
bash scripts/smoke/traceid_check.sh
```

验证公开、未认证和已认证请求都返回 `X-Request-Id`。日志中的同一请求应使用相同 traceId。
