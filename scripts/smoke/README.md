# Phase2 Smoke Scripts

## Files
- `scripts/smoke/.env.example`: environment variables template
- `scripts/smoke/phase2_payment_after_sales.sh`: payment + after-sales smoke script

## Quick Start
1. Copy env template:
   ```bash
   cp scripts/smoke/.env.example scripts/smoke/.env
   ```
2. Fill token/product fields in `scripts/smoke/.env`
3. Run:
   ```bash
   bash scripts/smoke/phase2_payment_after_sales.sh
   ```

## Covered Flow
1. Create order
2. Pay order
3. Payment callback + callback replay idempotency
4. Admin query payment
5. C apply after-sales
6. Admin review approve
7. Admin refund
8. C query after-sales

## Notes
- The script requires `openssl` for callback signature generation.
- Keep `CALLBACK_SECRET` aligned with `payment.mock.callback-secret` in mall bootstrap config.
- For idempotency test consistency, script generates unique keys by timestamp.
