import type { AxiosError } from 'axios';

export function getErrorMessage(error: unknown, fallback = '请求失败') {
  const err = error as AxiosError<{ message?: string }>;
  if (err?.response) {
    const serverMsg = err.response.data?.message;
    if (serverMsg) return serverMsg;
    const status = err.response.status;
    if (status === 401) return '未登录或登录已过期';
    if (status === 403) return '无权限访问';
    if (status === 404) return '接口不存在';
    if (status >= 500) return '服务端异常';
  }
  if (err?.message) return err.message;
  return fallback;
}
