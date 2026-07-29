import axios from 'axios';
import { message } from 'antd';
import { session } from '../store/session';
import { getErrorMessage } from '../utils/error';

const userApiBase = import.meta.env.VITE_USER_API_BASE ?? '/user/api';
let refreshPromise: Promise<string> | null = null;

const refreshAccessToken = () => {
  if (refreshPromise) return refreshPromise;
  refreshPromise = (async () => {
    const refreshToken = session.getRefreshToken();
    const tenantId = session.getTenantId();
    if (!refreshToken || !tenantId) {
      throw new Error('登录已失效');
    }
    const response = await axios.post<{
      success: boolean;
      message?: string;
      data?: { token?: string; refreshToken?: string };
    }>(
      `${userApiBase}/auth/refresh`,
      { refreshToken, tenantId },
      { headers: { 'X-Tenant-ID': tenantId } }
    );
    const auth = response.data?.data;
    if (!response.data?.success || !auth?.token) {
      throw new Error(response.data?.message || '登录已失效');
    }
    session.setToken(auth.token);
    if (auth.refreshToken) {
      session.setRefreshToken(auth.refreshToken);
    }
    return auth.token;
  })().finally(() => {
    refreshPromise = null;
  });
  return refreshPromise;
};

const redirectToLogin = () => {
  session.clearAll();
  if (window.location.pathname !== '/login') {
    const redirect = encodeURIComponent(
      window.location.pathname + window.location.search
    );
    window.location.href = `/login?redirect=${redirect}&expired=1`;
  }
};

const tenantIdFromBody = (data: unknown) => {
  if (!data) return '';
  if (typeof data === 'string') {
    try {
      const parsed = JSON.parse(data) as { tenantId?: string };
      return parsed.tenantId || '';
    } catch {
      return '';
    }
  }
  if (typeof data === 'object' && 'tenantId' in data) {
    return String((data as { tenantId?: unknown }).tenantId || '');
  }
  return '';
};

const createHttp = (baseURL: string) => {
  const client = axios.create({
    baseURL,
    timeout: 10000
  });

  client.interceptors.response.use(
    (resp) => resp,
    async (error) => {
      const status = error?.response?.status;
      if (status === 401) {
        const original = error?.config as (typeof error.config & { _retry?: boolean }) | undefined;
        const isAuthEntry = original?.url?.includes('/auth/login') ||
          original?.url?.includes('/auth/register') ||
          original?.url?.includes('/auth/refresh');
        if (original && !original._retry && !isAuthEntry) {
          original._retry = true;
          try {
            const token = await refreshAccessToken();
            original.headers = original.headers || {};
            original.headers.Authorization = `Bearer ${token}`;
            return client(original);
          } catch {
            redirectToLogin();
          }
        } else if (!isAuthEntry) {
          redirectToLogin();
        }
      } else if (status >= 400) {
        message.error(getErrorMessage(error, '请求失败'));
      }
      return Promise.reject(error);
    }
  );

  client.interceptors.request.use((config) => {
    const token = session.getToken();
    const authEntry = config.url?.includes('/auth/login') ||
      config.url?.includes('/auth/register') ||
      config.url?.includes('/auth/refresh');
    const tenantId = (authEntry ? tenantIdFromBody(config.data) : '') ||
      session.getTenantId() ||
      'stacko-mall';
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    config.headers['X-Tenant-ID'] = tenantId;
    return config;
  });

  return client;
};

const http = createHttp(import.meta.env.VITE_MALL_API_BASE ?? '/mall/api');
export const userHttp = createHttp(userApiBase);

export default http;
