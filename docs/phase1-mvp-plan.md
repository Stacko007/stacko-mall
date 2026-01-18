Phase 1 (MVP) Development Plan - C端与管理端

Goal
- Build a minimal end-to-end shopping flow: product publish -> browse -> order -> pay (mock) -> fulfill -> complete.

Scope
- Domains: catalog (product), inventory (stock), order, payment (mock), fulfillment (shipment), user (existing stacko-user).
- Interfaces: /api/c/** for C端, /api/admin/** for 管理端.
- Non-goals: promotions, coupons, search, reviews, after-sale, finance, multi-store operations beyond basic tenant header.

Milestones
M1. Catalog ready (admin + c browse)
M2. Inventory ready (admin adjustments + c read)
M3. Order flow ready (c create/pay, admin fulfill)
M4. MVP hardening (auth, validation, logging, docs)

Common Conventions
- Tenant header: X-Tenant-ID required.
- Auth: JWT/Sa-Token via stacko-user; admin endpoints must use @RequiresPermission.
- API prefixes: /api/c/** and /api/admin/**.
- Status enums: use explicit status fields for order, payment, fulfillment.

Domain Breakdown and Tasks

1) Catalog (Product)
Admin endpoints (/api/admin/products)
- Create product (name, description, price, status, stock? if separate inventory then no stock here)
- Update product (name, description, price, status)
- Get product by id
- List products (paging optional in MVP)
C端 endpoints (/api/c/products)
- List products (by tenant)
- Get product detail
Tasks
- Ensure product status supports DRAFT/ONLINE/OFFLINE.
- Add DTOs for C端 list/detail if needed.

2) Inventory (Stock)
Admin endpoints (/api/admin/stocks)
- Set stock for product (absolute)
- Adjust stock (delta)
- Get stock by product id
C端 endpoints (/api/c/stocks)
- Get stock by product id (read-only)
Tasks
- New aggregate: Stock (productId, quantity, updatedAt).
- Repository + MyBatis persistence + table.

3) Order
C端 endpoints (/api/c/orders)
- Create order (productId, quantity, addressId or simple address fields)
- Get order by id
- List orders (by buyer)
- Pay order (mock payment)
Admin endpoints (/api/admin/orders)
- Get order by id
- List orders
- Update order status (confirm, cancel, ship)
Tasks
- New aggregate: Order with items, status, totalAmount.
- Inventory reservation on create (simple reduce stock).
- Payment: mock service, mark order as PAID.
- Fulfillment: mark order as SHIPPED, COMPLETED.

4) Payment (Mock)
C端 endpoints
- /api/c/orders/{id}/pay (mock)
Admin endpoints
- Optional: /api/admin/payments/{id} for audit (MVP skip)
Tasks
- Payment status field on Order or separate Payment aggregate (MVP: embed in Order).

5) Fulfillment (Shipment)
Admin endpoints
- /api/admin/orders/{id}/ship (carrier, trackingNo)
C端 endpoints
- /api/c/orders/{id} should return shipment info
Tasks
- Shipment info embedded in Order (MVP).

6) User Integration
- Use stacko-user token and tenant context.
- Admin endpoints require @RequiresPermission with mall:product:* and mall:order:*.

Execution Plan (by milestone)

M1. Catalog ready
- Admin: product CRUD + permission annotations
- C端: list/detail
- DB: catalog_product table (done) + indexes
- Docs: OpenAPI tags

M2. Inventory ready
- Domain: Stock aggregate + repository
- Infra: MyBatis mapper + table catalog_stock
- Admin: set/adjust endpoints
- C端: read endpoint

Quick Verification (M2)
- Admin set stock: PUT /api/admin/stocks/{productId}
- Admin adjust stock: POST /api/admin/stocks/{productId}/adjust
- C get stock: GET /api/c/stocks/{productId}

M3. Order flow ready
- Domain: Order aggregate, OrderItem, status transitions
- Application: create/list/get/pay/ship services
- Infra: order tables + mappers
- C端: create/list/get/pay
- Admin: list/get/ship

M4. MVP hardening
- Validation (request DTO constraints)
- Error handling (consistent API error response)
- Logging/traceId (optional)
- Integration test smoke scripts

Permissions (admin)
- Product: mall:product:create|update|read|list|status
- Inventory: mall:stock:set|adjust|read|list
- Order: mall:order:read|list|ship|cancel

Deliverables
- Code: domain/application/infra/interfaces for each area.
- DB scripts in db/migration.
- Swagger groups: mall-c, mall-admin.

Quick Verification (M1)
- Admin create product: POST /api/admin/products (RequiresPermission + X-Tenant-ID)
- Admin list products: GET /api/admin/products
- C list products: GET /api/c/products
- C product detail: GET /api/c/products/{id}

Notes
- Prefer simple designs; optimize later.
- Keep consistent tenant handling across all endpoints.
