const TOKEN_KEY = 'stacko_token';
const BUYER_ID_KEY = 'stacko_buyer_id';
const TENANT_ID_KEY = 'stacko_tenant_id';

export const session = {
  getToken() {
    return localStorage.getItem(TOKEN_KEY) || '';
  },
  setToken(token: string) {
    localStorage.setItem(TOKEN_KEY, token);
  },
  clearToken() {
    localStorage.removeItem(TOKEN_KEY);
  },
  getBuyerId() {
    return localStorage.getItem(BUYER_ID_KEY) || '';
  },
  setBuyerId(buyerId: string) {
    localStorage.setItem(BUYER_ID_KEY, buyerId);
  },
  getTenantId() {
    return localStorage.getItem(TENANT_ID_KEY) || '';
  },
  setTenantId(tenantId: string) {
    localStorage.setItem(TENANT_ID_KEY, tenantId);
  },
  clearAll() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(BUYER_ID_KEY);
    localStorage.removeItem(TENANT_ID_KEY);
  }
};
