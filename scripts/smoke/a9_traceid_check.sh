#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="${1:-scripts/smoke/.env}"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "[ERROR] env file not found: $ENV_FILE"
  echo "Copy scripts/smoke/.env.example to scripts/smoke/.env first."
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

require_var BASE_URL
require_var TENANT_ID

request_and_check_trace() {
  local method="$1"
  local url="$2"
  local token="${3:-}"

  local header_file body_file
  header_file="$(mktemp)"
  body_file="$(mktemp)"

  local -a headers
  headers=(-H "X-Tenant-ID: ${TENANT_ID}")
  if [[ -n "$token" ]]; then
    headers+=(-H "Authorization: Bearer ${token}")
  fi

  local status
  status="$(curl -sS -D "$header_file" -o "$body_file" -w "%{http_code}" -X "$method" "$url" "${headers[@]}")"

  local trace_id
  trace_id="$(awk 'BEGIN{IGNORECASE=1} /^X-Request-Id:/ {print $2}' "$header_file" | tr -d '\r' | head -n1)"

  if [[ -z "$trace_id" ]]; then
    echo "[ERROR] missing X-Request-Id: $method $url (HTTP $status)"
    echo "--- headers ---"
    cat "$header_file"
    echo "--- body ---"
    cat "$body_file"
    rm -f "$header_file" "$body_file"
    exit 1
  fi

  echo "[OK] $method $url -> HTTP $status, X-Request-Id=$trace_id"
  rm -f "$header_file" "$body_file"
}

echo "[A9] traceId check start"

# Public endpoint (should be anonymously accessible)
request_and_check_trace GET "${BASE_URL}/api/c/products"

# Auth-required endpoint: still must carry trace id even when unauthorized
request_and_check_trace GET "${BASE_URL}/api/c/orders?buyerId=10"

# Auth-required with token (optional)
if [[ -n "${C_TOKEN:-}" && "${C_TOKEN}" != "replace_with_c_token" ]]; then
  request_and_check_trace GET "${BASE_URL}/api/c/orders?buyerId=10" "$C_TOKEN"
fi

if [[ -n "${ADMIN_TOKEN:-}" && "${ADMIN_TOKEN}" != "replace_with_admin_token" ]]; then
  request_and_check_trace GET "${BASE_URL}/api/admin/orders" "$ADMIN_TOKEN"
fi

echo "[DONE] A9 traceId check passed"
