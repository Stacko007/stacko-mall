const TOKEN_KEY = 'stacko_mall_web_token';
const BUYER_ID_KEY = 'stacko_mall_web_membership_id';
const TENANT_ID_KEY = 'stacko_mall_web_tenant_id';
const PROFILE_KEY = 'stacko_mall_web_profile';

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
  getProfile<T>() {
    const value = localStorage.getItem(PROFILE_KEY);
    if (!value) return null;
    try {
      return JSON.parse(value) as T;
    } catch {
      return null;
    }
  },
  setProfile(profile: unknown) {
    localStorage.setItem(PROFILE_KEY, JSON.stringify(profile));
  },
  clearAll() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(BUYER_ID_KEY);
    localStorage.removeItem(TENANT_ID_KEY);
    localStorage.removeItem(PROFILE_KEY);
  }
};
