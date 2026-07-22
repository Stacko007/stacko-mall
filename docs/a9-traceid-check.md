# A9 TraceId Consistency Check

## Goal
Ensure every request path returns `X-Request-Id` and logs can correlate by `traceId`.

## Preconditions
- 商城和用户中心均使用各自 Web 接口模块中的 `TraceIdFilter`。
- `X-Tenant-ID` provided in request headers.

## Quick Verify
```bash
bash scripts/smoke/a9_traceid_check.sh
```

## Covered Checks
1. Public C endpoint (`/api/c/products`) contains `X-Request-Id`.
2. Auth-required endpoint also contains `X-Request-Id` when unauthorized.
3. Auth-required endpoint contains `X-Request-Id` with valid user token.
4. Admin endpoint contains `X-Request-Id` with valid admin token.

## Expected Result
- All checks print `[OK] ... X-Request-Id=<non-empty>`.
- Script exits with `[DONE] A9 traceId check passed`.

## Notes
- If any endpoint misses `X-Request-Id`, check:
  - 当前服务自己的 `TraceIdFilter` 是否被 Spring 扫描。
  - Response path is not bypassing servlet filter chain.
  - Upstream reverse proxy does not strip response headers.

商城调用用户中心的 ACL 和当前用户接口时会透传 `X-Request-Id`，因此两边控制台可使用同一个 traceId 检索一次请求。启动日志、定时任务等非 HTTP 日志显示 `traceId=N/A`。
