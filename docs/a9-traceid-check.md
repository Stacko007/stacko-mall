# A9 TraceId Consistency Check

## Goal
Ensure every request path returns `X-Request-Id` and logs can correlate by `traceId`.

## Preconditions
- Service started with current `stacko-user` dependency containing `TraceIdFilter`.
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
  - `stacko-user` `TraceIdFilter` is loaded.
  - Response path is not bypassing servlet filter chain.
  - Upstream reverse proxy does not strip response headers.
