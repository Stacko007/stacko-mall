# C端前端开发进度（React）

> 用途：记录 C 端前端开发与联调进度，独立于服务端开发进度。

## 1. 当前状态
- 前端工程目录：`stacko-mall-frontend`
- 技术栈：React + TypeScript + Vite + Ant Design
- 接口文档基准：`docs/apifox-openapi.yaml`
- 本地联调目标：`http://localhost:8080`（通过 Vite 代理 `/api`）
- 前端开发服务：`http://localhost:5173`

## 2. 已完成
1. 工程初始化与基础结构
- 目录结构：`pages/`、`components/`、`services/`、`store/`、`utils/`
- Vite 配置与开发端口配置
- Ant Design 样式引入

2. 联调基础设施
- Axios 封装与基础错误处理
- Token 注入（Authorization: Bearer）
- 租户头注入（X-Tenant-ID）
- 幂等请求头工具（X-Idempotency-Key）
- API 类型与方法封装（基于 apifox-openapi）

3. 页面与联调（已接入）
- 登录页：`/login`（登录接口）
- 首页：`/`（商品列表 + 加购 + 骨架/空态）
- 分类页：`/category`（商品列表 + 骨架/空态）
- 搜索页：`/search`（前端搜索）
- 商品列表页：`/products`（加购 + 骨架/空态）
- 商品详情页：`/products/:id`（详情+库存+加购）
- 购物车页：`/cart`（多商品、数量调整、选择下单）
- 确认订单页：`/confirm`（创建订单）
- 支付页：`/payment/:id`（模拟支付 + 模拟回调）
- 支付结果页：`/payment-result/:id`（状态轮询 + 手动刷新）
- 订单页：`/orders`（状态筛选+支付/取消）
- 订单详情页：`/orders/:id`
- 个人中心：`/profile`（本地用户信息）

4. 移动端适配
- Header 菜单横向滚动
- 卡片/按钮圆角与紧凑间距
- 表格、输入框在小屏优化
- 搜索输入与按钮自适应换行

5. 体验优化
- 统一空态/错误态组件（`components/State.tsx`）
- 页面错误提示与重试入口
- 细化错误提示（根据 HTTP 状态与服务端 message）
- 全局异常提示（403/404/5xx 统一 toast）

## 3. 已接入接口清单
- `POST /api/auth/login`
- `GET /api/c/products`
- `GET /api/c/products/{id}`
- `GET /api/c/stocks/{productId}`
- `POST /api/c/orders`
- `GET /api/c/orders`
- `GET /api/c/orders/{id}`
- `POST /api/c/orders/{id}/pay`
- `POST /api/c/orders/{id}/cancel`
- `POST /api/c/payments/callback`

## 4. 当前阻塞
- 无硬性阻塞

## 5. 下一步计划
1. UI 细节继续打磨
2. 关键流程体验梳理

## 6. 关键文件
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/package.json`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/vite.config.ts`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/services/api.ts`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/services/http.ts`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/components/State.tsx`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/utils/error.ts`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/store/cart.ts`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/pages/ProductList.tsx`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/pages/ProductDetail.tsx`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/pages/ConfirmOrder.tsx`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/pages/Payment.tsx`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/pages/PaymentResult.tsx`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/pages/Orders.tsx`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/pages/OrderDetail.tsx`
- `/Users/wanghuidong/Documents/gitee_projects/stacko-mall/stacko-mall-frontend/src/pages/Cart.tsx`
