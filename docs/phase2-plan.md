Phase 2 Development Plan - Order Hardening + Payment + After-Sales

Goal
- Strengthen order lifecycle reliability and move from mock payment to real payment flow.
- Add essential after-sales (cancel/refund) to complete a basic commerce loop.

Scope
- Domains: order, inventory, payment, after-sales, user (existing stacko-user).
- Interfaces: /api/c/** for C side, /api/admin/** for admin side.
- Non-goals (this phase): promotions, coupons, reviews, search, advanced finance.

Milestones
M1. Order hardening (cancel/timeout/rollback, idempotency)
M2. Payment integration (real payment + callback + state machine)
M3. After-sales (refund/cancel flow, admin audit)
M4. Observability + automation (logging, metrics, smoke tests)

Current Progress Snapshot (updated: 2026-02-14)
- Overall: Phase2 in progress. M1 done; M2 mostly done; M3 started (foundation only); M4 not started.
- M1 status: DONE
- Completed: A1 (order transition rules), A2 (stock rollback on cancel/close), A3 (idempotency for create/pay/cancel), A4 (timeout close scheduled job).
- Verified: cancel/close endpoints available and tested; idempotency repeat behavior verified.
- M2 status: IN PROGRESS
- Completed: A5 (Payment aggregate + repository + mapper), A6 (pay flow creates/updates payment), A7 (payment callback + signature verify + callback idempotency), A8 (admin payment query API + response DTO).
- Verified: pay API sets order to PAID; callback API returns success and is idempotent on callback replay.
- Hotfix done: adjusted DB length for UUID-related fields (`biz_id` etc.) to avoid "Data too long" conflict.
- M3 status: IN PROGRESS (B5/B6 done)
- Completed: B5 (AfterSales aggregate + repository + mapper), B6 (AfterSalesApplicationService apply/review/refund flow), B7 (after-sales C/admin endpoints + DTO + OpenAPI update), B8 (payment + after-sales smoke scripts).
- Completed: A9 (traceId consistency smoke check script + checklist doc).
- Pending next: A10 basic metrics, then B9 end-to-end smoke and idempotency regression.
- M4 status: TODO

Common Conventions
- Tenant header: X-Tenant-ID required.
- Auth: JWT/Sa-Token via stacko-user; admin endpoints use @RequiresPermission.
- API prefixes: /api/c/** and /api/admin/**.
- Status enums: explicit status fields for order, payment, fulfillment, after-sales.
- Idempotency: idempotency key required for create/pay/cancel (X-Idempotency-Key).

Domain Breakdown and Tasks

1) Order Hardening
C endpoints (/api/c/orders)
- Cancel order: POST /api/c/orders/{id}/cancel
- Confirm receipt: POST /api/c/orders/{id}/confirm
Admin endpoints (/api/admin/orders)
- Close order: POST /api/admin/orders/{id}/close
Tasks
- Add order close/cancel transitions with business rules.
- Implement timeout close job (simple scheduled task).
- Stock rollback on cancel/close.
- Idempotency for create/pay/cancel.

2) Payment Integration
C endpoints
- Pay order: POST /api/c/orders/{id}/pay
- Payment callback: POST /api/c/payments/callback (provider webhook)
Admin endpoints
- Payment query/audit: GET /api/admin/payments/{id}
Tasks
- New Payment aggregate (id, orderId, status, amount, channel, tradeNo).
- Payment status transitions (CREATED -> PAID/FAILED -> REFUNDED).
- Signature verify + callback idempotency.
- Store raw callback payload for audit.

3) After-Sales (Refund/Cancel)
C endpoints
- Apply refund: POST /api/c/after-sales
- Get after-sales: GET /api/c/after-sales/{id}
Admin endpoints
- Review after-sales: POST /api/admin/after-sales/{id}/review
- Refund confirm: POST /api/admin/after-sales/{id}/refund
Tasks
- AfterSales aggregate (type, reason, status).
- Link to order/payment for refund.
- Refund execution strategy (provider API or mock fallback).

4) Observability + Automation
- Add metrics for order/payment/after-sales counts.
- TraceId required for all requests.
- Standardized error response via GlobalExceptionHandler (from stacko-user-shared-web).
- Add smoke test scripts (curl collection) for M1-M3.

Execution Plan (by milestone)

M1. Order hardening
- Domain: order status transitions, cancel/close/confirm.
- Application: idempotency + stock rollback.
- Infra: timeout job (scheduled), idempotency table.
- Interfaces: new endpoints for cancel/confirm/close.

M2. Payment integration
- Domain: Payment aggregate + status flow.
- Application: payment create + callback handling.
- Infra: payment tables + mapper.
- Interfaces: pay + callback + admin query.

M3. After-sales
- Domain: AfterSales aggregate.
- Application: apply/review/refund.
- Infra: after-sales tables + mapper.
- Interfaces: C apply/query, admin review/refund.

M4. Observability + automation
- Logging: traceId in logs and response headers.
- Metrics: basic counters.
- Tests: curl smoke tests for order/payment/after-sales.

Permissions (admin)
- Order: mall:order:close|confirm|cancel
- Payment: mall:payment:read|refund
- After-sales: mall:afterSales:read|review|refund

Deliverables
- Code: domain/application/infra/interfaces for order/payment/after-sales.
- DB scripts in db/migration.
- Swagger groups: mall-c, mall-admin.
- Smoke scripts in docs/ or scripts/.

Quick Verification (M1)
- C cancel order: POST /api/c/orders/{id}/cancel
- C confirm receipt: POST /api/c/orders/{id}/confirm
- Admin close order: POST /api/admin/orders/{id}/close

Quick Verification (M2)
- C pay order: POST /api/c/orders/{id}/pay
- Payment callback: POST /api/c/payments/callback
- Admin payment query: GET /api/admin/payments/{id}

Quick Verification (M3)
- C apply refund: POST /api/c/after-sales
- Admin review: POST /api/admin/after-sales/{id}/review
- Admin refund: POST /api/admin/after-sales/{id}/refund

Detailed Task List (by module)

1) Domain (stacko-mall-domain)
- Add Payment aggregate: Payment, PaymentId, PaymentStatus, PaymentChannel.
- Add AfterSales aggregate: AfterSales, AfterSalesId, AfterSalesStatus, AfterSalesType.
- Extend Order aggregate: cancel/close/confirm transitions with rules; link to payment/after-sales if needed.
- Add domain events (optional, if event-driven).

2) Application (stacko-mall-application)
- OrderApplicationService: add cancel/close/confirm methods; handle stock rollback.
- PaymentApplicationService: create payment, handle callback, query payment.
- AfterSalesApplicationService: apply, review, refund; link to order/payment.
- IdempotencyService: create/check idempotent records for create/pay/cancel/refund.
- Add DTOs/commands for new flows (CancelOrderCommand, CloseOrderCommand, RefundApplyCommand, etc.).

3) Infra (stacko-mall-infra)
- MyBatis mappers for payment, after_sales, idempotency tables.
- Repository implementations for Payment/AfterSales/Idempotency.
- Scheduled job for order timeout close (simple scan by status + createdAt).
- Integrate payment provider client (or mock adapter first).

4) Interfaces (stacko-mall-interfaces)
- C endpoints: cancel/confirm orders; apply refund; get after-sales; payment callback.
- Admin endpoints: close order; review/refund after-sales; payment query/audit.
- Request/response DTOs with validation annotations.
- Swagger tags/operations for new endpoints.
- Permission annotations for admin endpoints.

5) Bootstrap (stacko-mall-bootstrap)
- Register payment provider config (keys/urls) in application-*.yml.
- If needed: enable scheduling, configure idempotency/timeout params.
- Confirm component scan for new packages.

6) DB/Migration (db/migration or docs/sql)
- Add tables: payment, after_sales, idempotency, order_status_history (optional).
- Add indexes: order_id, tenant_id, status, created_at.
- Add refund fields to order/payment if needed.

7) Tests / Smoke Scripts (docs/ or scripts/)
- Curl scripts for cancel/confirm/close.
- Payment flow: create -> pay -> callback -> query.
- After-sales flow: apply -> review -> refund -> query.

Acceptance Checklist (per milestone)

M1 Order hardening
- Cancel/close/confirm endpoints return ApiResponse.
- Stock rollback verified for cancel/close.
- Timeout close job closes overdue orders.
- Idempotency for create/pay/cancel works (repeat request safe).

M2 Payment integration
- Payment record created and updated on callback.
- Callback idempotency verified.
- Admin payment query returns accurate status.

M3 After-sales
- Apply/refund flow end-to-end.
- Admin review required before refund.
- Refund updates payment/order status correctly.

M4 Observability + automation
- traceId present in logs and response header.
- Basic metrics or counters available.
- Smoke scripts run with expected responses.

Executable Issue List (by person/week)

Assumptions
- 2 devs (A/B), 2 weeks per milestone (adjust as needed).
- Each issue is deliverable and testable.

Week 1 (Dev A) - Order hardening core
- Issue A1: Add order cancel/close/confirm transitions + rules in domain.
- Issue A2: Add stock rollback on cancel/close.
- Issue A3: Add idempotency model and service (create/pay/cancel).
- Issue A4: Add order timeout close job (scheduled task + config).

Week 1 (Dev B) - Interfaces + docs
- Issue B1: Add C endpoints for cancel/confirm; admin close endpoint.
- Issue B2: Add DTO validation for new endpoints.
- Issue B3: Update Swagger tags/operations and API docs.
- Issue B4: Add curl smoke scripts for order hardening.

Week 2 (Dev A) - Payment integration
- Issue A5: Add Payment aggregate + repository + mapper.
- Issue A6: Add payment create flow + status transitions.
- Issue A7: Implement payment callback (signature verify + idempotency).
- Issue A8: Add admin payment query endpoint + response DTO.

Week 2 (Dev B) - After-sales flow
- Issue B5: Add AfterSales aggregate + repository + mapper.
- Issue B6: Implement apply/review/refund flow in application service.
- Issue B7: Add C endpoints for apply/query and admin review/refund.
- Issue B8: Add smoke scripts for payment and after-sales.

Week 3 (Dev A) - Observability + cleanup
- Issue A9: Ensure traceId present in responses and logs for all endpoints.
- Issue A10: Add basic metrics (order/payment/after-sales counters).
- Issue A11: Refactor error messages for consistency.

Week 3 (Dev B) - QA + hardening
- Issue B9: End-to-end smoke tests for all flows.
- Issue B10: Verify idempotency under repeated requests.
- Issue B11: Add order status history persistence (optional).

Notes
- If only 1 dev, execute A then B in sequence.
- If timeline is shorter, skip optional items (order status history, metrics).
