import http from './http';
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

export type AuthToken = {
  token: string;
  refreshToken?: string;
  expiresAt?: string;
  refreshExpiresAt?: string;
  userId?: number;
  tenantId?: string;
};

export type ProductStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE';

export type Product = {
  id: string;
  tenantId?: string;
  name: string;
  description?: string;
  price: number;
  status: ProductStatus;
  createdAt?: string;
  updatedAt?: string;
};

export type Stock = {
  productId: string;
  productName?: string;
  quantity: number;
  updatedAt?: string;
};

export type OrderStatus =
  | 'CREATED'
  | 'PAID'
  | 'SHIPPED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'CLOSED';

export type OrderItem = {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
  amount?: number;
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

export type Order = {
  id: string;
  buyerId: string;
  buyerName?: string;
  status: OrderStatus;
  totalAmount: number;
  shippingCarrier?: string | null;
  trackingNo?: string | null;
  createdAt?: string;
  updatedAt?: string;
  shippedAt?: string | null;
  completedAt?: string | null;
  items?: OrderItem[];
};

export type PaymentStatus = 'CREATED' | 'PAID' | 'FAILED' | 'REFUNDED';
export type PaymentChannel = 'MOCK' | 'ALIPAY' | 'WECHAT';

export type PaymentCallbackRequest = {
  callbackId: string;
  orderId: string;
  tradeNo: string;
  status: 'PAID' | 'FAILED';
  signature: string;
  rawPayload?: string;
};

export type Payment = {
  id: string;
  orderId: string;
  amount: number;
  status: PaymentStatus;
  channel: PaymentChannel;
  tradeNo?: string | null;
  rawCallback?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

export type AfterSalesStatus =
  | 'APPLIED'
  | 'APPROVED'
  | 'REJECTED'
  | 'REFUNDED'
  | 'CANCELLED';

export type AfterSalesType = 'REFUND_ONLY' | 'RETURN_REFUND';

export type AfterSales = {
  id: string;
  orderId: string;
  paymentId?: string | null;
  type: AfterSalesType;
  reason?: string | null;
  status: AfterSalesStatus;
  remark?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

export const adminApi = {
  login: (payload: LoginRequest) =>
    http.post<ApiResponse<AuthToken>>('/auth/login', payload),

  listProducts: () => http.get<ApiResponse<Product[]>>('/admin/products'),
  getProduct: (id: string) =>
    http.get<ApiResponse<Product>>(`/admin/products/${id}`),
  createProduct: (payload: {
    name: string;
    description?: string;
    price: number;
  }) => http.post<ApiResponse<Product>>('/admin/products', payload),
  updateProduct: (
    id: string,
    payload: {
      name: string;
      description?: string;
      price: number;
      status?: ProductStatus;
    }
  ) => http.put<ApiResponse<Product>>(`/admin/products/${id}`, payload),

  listOrders: (buyerId?: string) =>
    http.get<ApiResponse<Order[]>>('/admin/orders', {
      params: buyerId ? { buyerId } : undefined
    }),
  getOrder: (id: string) => http.get<ApiResponse<Order>>(`/admin/orders/${id}`),
  shipOrder: (id: string, payload: { carrier: string; trackingNo: string }) =>
    http.post<ApiResponse<Order>>(`/admin/orders/${id}/ship`, payload),
  closeOrder: (id: string) =>
    http.post<ApiResponse<Order>>(`/admin/orders/${id}/close`),

  listStocks: () => http.get<ApiResponse<Stock[]>>('/admin/stocks'),
  getStock: (productId: string) =>
    http.get<ApiResponse<Stock>>(`/admin/stocks/${productId}`),
  setStock: (productId: string, payload: { quantity: number }) =>
    http.put<ApiResponse<Stock>>(`/admin/stocks/${productId}`, payload),
  adjustStock: (productId: string, payload: { delta: number }) =>
    http.post<ApiResponse<Stock>>(`/admin/stocks/${productId}/adjust`, payload),

  getPayment: (id: string) =>
    http.get<ApiResponse<Payment>>(`/admin/payments/${id}`),

  getAfterSales: (id: string) =>
    http.get<ApiResponse<AfterSales>>(`/admin/after-sales/${id}`),
  reviewAfterSales: (id: string, payload: { approved: boolean; remark?: string }) =>
    http.post<ApiResponse<AfterSales>>(`/admin/after-sales/${id}/review`, payload),
  refundAfterSales: (id: string, payload: { remark?: string }) =>
    http.post<ApiResponse<AfterSales>>(`/admin/after-sales/${id}/refund`, payload)
};

export const api = {
  login: (payload: LoginRequest) =>
    http.post<ApiResponse<AuthToken>>('/auth/login', payload),

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
