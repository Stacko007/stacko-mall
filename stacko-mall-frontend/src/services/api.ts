import http, { userHttp } from './http';
import { createIdempotencyKey } from '../utils/idempotency';

export type ApiResponse<T> = {
  success: boolean;
  message?: string | null;
  data: T;
};

export type LoginRequest = {
  username: string;
  password: string;
  tenantId: string;
  withRefresh?: boolean;
};

export type RegisterRequest = {
  username: string;
  password: string;
  tenantId: string;
  phone?: string;
  email?: string;
};

export type AuthToken = {
  token: string;
  refreshToken?: string;
  expiresAt?: string;
  refreshExpiresAt?: string;
  userId?: number;
  tenantId?: string;
};

export type Product = {
  id: string;
  tenantId?: string;
  name: string;
  description?: string;
  price: number;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type Stock = {
  productId: string;
  quantity: number;
  updatedAt?: string;
};

export type OrderItem = {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
  amount?: number;
};

export type Order = {
  id: string;
  buyerId: string;
  status: 'CREATED' | 'PAID' | 'SHIPPED' | 'COMPLETED' | 'CANCELLED' | 'CLOSED';
  totalAmount: number;
  shippingCarrier?: string | null;
  trackingNo?: string | null;
  createdAt?: string;
  updatedAt?: string;
  shippedAt?: string | null;
  completedAt?: string | null;
  items?: OrderItem[];
};

export type OrderCreateRequest = {
  buyerId?: string;
  items: Array<{
    productId: string;
    productName: string;
    price: number;
    quantity: number;
  }>;
};

export type PaymentCallbackRequest = {
  callbackId: string;
  orderId: string;
  tradeNo: string;
  status: 'PAID' | 'FAILED';
  signature: string;
  rawPayload?: string;
};

export const api = {
  login: (payload: LoginRequest) =>
    userHttp.post<ApiResponse<AuthToken>>('/auth/login', payload),

  register: (payload: RegisterRequest) =>
    userHttp.post<ApiResponse<AuthToken>>('/auth/register', payload),

  getProducts: () => http.get<ApiResponse<Product[]>>('/c/products'),

  getProduct: (id: string) => http.get<ApiResponse<Product>>(`/c/products/${id}`),

  getStock: (productId: string) =>
    http.get<ApiResponse<Stock>>(`/c/stocks/${productId}`),

  createOrder: (payload: OrderCreateRequest, idempotencyKey?: string) =>
    http.post<ApiResponse<Order>>('/c/orders', payload, {
      headers: {
        'X-Idempotency-Key':
          idempotencyKey || createIdempotencyKey('ORDER_CREATE')
      }
    }),

  listOrders: () => http.get<ApiResponse<Order[]>>('/c/orders'),

  getOrder: (id: string) => http.get<ApiResponse<Order>>(`/c/orders/${id}`),

  payOrder: (id: string, idempotencyKey?: string) =>
    http.post<ApiResponse<Order>>(`/c/orders/${id}/pay`, null, {
      headers: {
        'X-Idempotency-Key': idempotencyKey || createIdempotencyKey(`PAY:${id}`)
      }
    }),

  cancelOrder: (id: string, idempotencyKey?: string) =>
    http.post<ApiResponse<Order>>(`/c/orders/${id}/cancel`, null, {
      headers: {
        'X-Idempotency-Key':
          idempotencyKey || createIdempotencyKey(`CANCEL:${id}`)
      }
    }),

  paymentCallback: (payload: PaymentCallbackRequest) =>
    http.post<ApiResponse<string>>('/c/payments/callback', payload)
};
