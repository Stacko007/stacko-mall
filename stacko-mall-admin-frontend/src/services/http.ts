import axios from 'axios';
import { message } from 'antd';
import { session } from '../store/session';
import { getErrorMessage } from '../utils/error';

const createHttp = (baseURL: string) => {
  const client = axios.create({
    baseURL,
    timeout: 10000
  });

  client.interceptors.response.use(
    (resp) => resp,
    (error) => {
      const status = error?.response?.status;
      if (status === 401) {
        session.clearAll();
        if (window.location.pathname !== '/login') {
          const redirect = encodeURIComponent(
            window.location.pathname + window.location.search
          );
          window.location.href = `/login?redirect=${redirect}`;
        }
      } else if (status === 403 || status === 404 || status >= 500) {
        message.error(getErrorMessage(error, '请求失败'));
      }
      return Promise.reject(error);
    }
  );

  client.interceptors.request.use((config) => {
    const token = session.getToken();
    const tenantId = session.getTenantId() || 'stacko-mall';
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    config.headers['X-Tenant-ID'] = tenantId;
    return config;
  });

  return client;
};

const http = createHttp(import.meta.env.VITE_MALL_API_BASE ?? '/mall/api');
export const userHttp = createHttp(
  import.meta.env.VITE_USER_API_BASE ?? '/user/api'
);

export default http;
