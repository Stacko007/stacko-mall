#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="${1:-scripts/smoke/.env}"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "[ERROR] env file not found: $ENV_FILE"
  echo "Copy scripts/smoke/.env.example to scripts/smoke/.env and fill values."
  exit 1
fi

source "$ENV_FILE"

require_var() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    echo "[ERROR] missing env var: $name"
    exit 1
  fi
}

for v in BASE_URL TENANT_ID C_TOKEN ADMIN_TOKEN BUYER_ID PRODUCT_ID PRODUCT_NAME PRODUCT_PRICE PRODUCT_QTY CALLBACK_SECRET; do
  require_var "$v"
done

if ! command -v openssl >/dev/null 2>&1; then
  echo "[ERROR] openssl is required"
  exit 1
fi

compact_json() {
  tr -d '\n\r\t ' < "$1"
}

assert_success() {
  local body_file="$1"
  local compact
  compact="$(compact_json "$body_file")"
  if [[ "$compact" != *'"success":true'* ]]; then
    echo "[ERROR] api failed"
    cat "$body_file"
    exit 1
  fi
}

extract_by_regex() {
  local body_file="$1"
  local regex="$2"
  local compact
  compact="$(compact_json "$body_file")"
  echo "$compact" | sed -n "s/${regex}/\\1/p"
}

request_api() {
  local method="$1"
  local url="$2"
  local token="$3"
  local idempotency_key="$4"
  local body="$5"

  local body_file
  body_file="$(mktemp)"

  local -a headers
  headers=(-H "X-Tenant-ID: ${TENANT_ID}" -H "Content-Type: application/json")
  if [[ -n "$token" ]]; then
    headers+=(-H "Authorization: Bearer ${token}")
  fi
  if [[ -n "$idempotency_key" ]]; then
    headers+=(-H "X-Idempotency-Key: ${idempotency_key}")
  fi

  local status
  if [[ -n "$body" ]]; then
    status="$(curl -sS -o "$body_file" -w "%{http_code}" -X "$method" "$url" "${headers[@]}" -d "$body")"
  else
    status="$(curl -sS -o "$body_file" -w "%{http_code}" -X "$method" "$url" "${headers[@]}")"
  fi

  if [[ "$status" != "200" ]]; then
    echo "[ERROR] $method $url -> HTTP $status"
    cat "$body_file"
    rm -f "$body_file"
    exit 1
  fi

  assert_success "$body_file"
  echo "$body_file"
}

echo "[SMOKE] Step1 create order"
CREATE_KEY="ORDER_CREATE_SMOKE_$(date +%s)"
CREATE_BODY="{\"buyerId\":\"${BUYER_ID}\",\"items\":[{\"productId\":\"${PRODUCT_ID}\",\"productName\":\"${PRODUCT_NAME}\",\"price\":${PRODUCT_PRICE},\"quantity\":${PRODUCT_QTY}}]}"
create_body_file="$(request_api POST "${BASE_URL}/api/c/orders" "$C_TOKEN" "$CREATE_KEY" "$CREATE_BODY")"
ORDER_ID="$(extract_by_regex "$create_body_file" '.*"data":\{"id":"\([^"]*\)".*')"
rm -f "$create_body_file"
if [[ -z "$ORDER_ID" ]]; then
  echo "[ERROR] failed to extract ORDER_ID"
  exit 1
fi
echo "[OK] ORDER_ID=$ORDER_ID"

echo "[SMOKE] Step2 pay order"
PAY_KEY="PAY:${ORDER_ID}:${BUYER_ID}:$(date +%s)"
pay_body_file="$(request_api POST "${BASE_URL}/api/c/orders/${ORDER_ID}/pay" "$C_TOKEN" "$PAY_KEY" "")"
rm -f "$pay_body_file"

echo "[SMOKE] Step3 callback paid + idempotent replay"
CALLBACK_ID="cb_smoke_${ORDER_ID}_001"
TRADE_NO="MOCK-TN-SMOKE-001"
CALLBACK_STATUS="PAID"
SIGN_TEXT="${CALLBACK_ID}|${ORDER_ID}|${TRADE_NO}|${CALLBACK_STATUS}"
SIGNATURE="$(printf "%s" "$SIGN_TEXT" | openssl dgst -sha256 -hmac "$CALLBACK_SECRET" | sed 's/^.*= //')"
CALLBACK_BODY="{\"callbackId\":\"${CALLBACK_ID}\",\"orderId\":\"${ORDER_ID}\",\"tradeNo\":\"${TRADE_NO}\",\"status\":\"${CALLBACK_STATUS}\",\"signature\":\"${SIGNATURE}\",\"rawPayload\":\"{\\\"smoke\\\":true,\\\"event\\\":\\\"PAY_SUCCESS\\\"}\"}"
cb_body_file="$(request_api POST "${BASE_URL}/api/c/payments/callback" "$C_TOKEN" "" "$CALLBACK_BODY")"
PAYMENT_ID="$(extract_by_regex "$cb_body_file" '.*"data":"SUCCESS:\([^"]*\)".*')"
rm -f "$cb_body_file"
if [[ -z "$PAYMENT_ID" ]]; then
  echo "[ERROR] failed to extract PAYMENT_ID"
  exit 1
fi
echo "[OK] PAYMENT_ID=$PAYMENT_ID"

# Replay callback with same callbackId
cb_replay_file="$(request_api POST "${BASE_URL}/api/c/payments/callback" "$C_TOKEN" "" "$CALLBACK_BODY")"
PAYMENT_ID_REPLAY="$(extract_by_regex "$cb_replay_file" '.*"data":"SUCCESS:\([^"]*\)".*')"
rm -f "$cb_replay_file"
if [[ "$PAYMENT_ID_REPLAY" != "$PAYMENT_ID" ]]; then
  echo "[ERROR] callback replay payment id mismatch: $PAYMENT_ID_REPLAY != $PAYMENT_ID"
  exit 1
fi
echo "[OK] callback idempotency replay paymentId unchanged"

echo "[SMOKE] Step4 admin query payment"
payment_query_file="$(request_api GET "${BASE_URL}/api/admin/payments/${PAYMENT_ID}" "$ADMIN_TOKEN" "" "")"
PAYMENT_STATUS="$(extract_by_regex "$payment_query_file" '.*"status":"\([^"]*\)".*')"
rm -f "$payment_query_file"
if [[ "$PAYMENT_STATUS" != "PAID" ]]; then
  echo "[ERROR] payment status expect PAID, got: $PAYMENT_STATUS"
  exit 1
fi
echo "[OK] payment status PAID"

echo "[SMOKE] Step5 apply after-sales"
APPLY_BODY="{\"orderId\":\"${ORDER_ID}\",\"type\":\"REFUND_ONLY\",\"reason\":\"smoke test refund\"}"
after_apply_file="$(request_api POST "${BASE_URL}/api/c/after-sales" "$C_TOKEN" "" "$APPLY_BODY")"
AFTER_SALES_ID="$(extract_by_regex "$after_apply_file" '.*"data":\{"id":"\([^"]*\)".*')"
rm -f "$after_apply_file"
if [[ -z "$AFTER_SALES_ID" ]]; then
  echo "[ERROR] failed to extract AFTER_SALES_ID"
  exit 1
fi
echo "[OK] AFTER_SALES_ID=$AFTER_SALES_ID"

echo "[SMOKE] Step6 admin review approve"
REVIEW_BODY='{"approved":true,"remark":"smoke approve"}'
after_review_file="$(request_api POST "${BASE_URL}/api/admin/after-sales/${AFTER_SALES_ID}/review" "$ADMIN_TOKEN" "" "$REVIEW_BODY")"
rm -f "$after_review_file"

echo "[SMOKE] Step7 admin refund"
REFUND_BODY='{"remark":"smoke refund"}'
after_refund_file="$(request_api POST "${BASE_URL}/api/admin/after-sales/${AFTER_SALES_ID}/refund" "$ADMIN_TOKEN" "" "$REFUND_BODY")"
rm -f "$after_refund_file"

echo "[SMOKE] Step8 c query after-sales"
after_get_file="$(request_api GET "${BASE_URL}/api/c/after-sales/${AFTER_SALES_ID}" "$C_TOKEN" "" "")"
AFTER_STATUS="$(extract_by_regex "$after_get_file" '.*"status":"\([^"]*\)".*')"
rm -f "$after_get_file"
if [[ "$AFTER_STATUS" != "REFUNDED" ]]; then
  echo "[ERROR] after-sales status expect REFUNDED, got: $AFTER_STATUS"
  exit 1
fi
echo "[OK] after-sales status REFUNDED"

echo "[DONE] smoke passed: order=$ORDER_ID payment=$PAYMENT_ID afterSales=$AFTER_SALES_ID"
