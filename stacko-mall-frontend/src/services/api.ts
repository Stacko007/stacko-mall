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
  portalCode?: string;
};

export type RegisterRequest = {
  username: string;
  password: string;
  tenantId: string;
  portalCode?: string;
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
  applicationCode?: string;
  portalCode?: string;
  audience?: string;
};

export type UserSession = {
  accountId: number;
  membershipId: number;
  username: string;
  tenantId: string;
  applicationCode: string;
  portalCode: string;
  audience: string;
  roles: string[];
  permissions: string[];
};

export type Product = {
  id: string;
  tenantId?: string;
  categoryId?: string | null;
  name: string;
  description?: string;
  price: number;
  status?: ProductStatus;
  createdAt?: string;
  updatedAt?: string;
};

export type ProductCategory = {
  id: string;
  parentId?: string | null;
  name: string;
  sort: number;
  status: 'ENABLED' | 'DISABLED';
  level: number;
  path: string;
  children?: ProductCategory[];
};

export type ProductStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE';

export type Stock = {
  productId: string;
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

export type Order = {
  id: string;
  buyerId: string;
  status: OrderStatus;
  totalAmount: number;
  shippingCarrier?: string | null;
  trackingNo?: string | null;
  receiverName?: string | null;
  receiverPhone?: string | null;
  receiverProvince?: string | null;
  receiverCity?: string | null;
  receiverDistrict?: string | null;
  receiverAddress?: string | null;
  createdAt?: string;
  updatedAt?: string;
  shippedAt?: string | null;
  completedAt?: string | null;
  items?: OrderItem[];
};

export type OrderCreateRequest = {
  buyerId?: string;
  addressId: string;
  items: Array<{
    productId: string;
    productName: string;
    price: number;
    quantity: number;
  }>;
};

export type ShippingAddress = {
  id: string;
  receiverName: string;
  receiverPhone: string;
  province: string;
  city: string;
  district?: string | null;
  detailAddress: string;
  defaultAddress: boolean;
  createdAt?: string;
  updatedAt?: string;
};

export type ShippingAddressPayload = {
  receiverName: string;
  receiverPhone: string;
  province: string;
  city: string;
  district?: string;
  detailAddress: string;
  defaultAddress?: boolean;
};

export type MemberProfile = {
  id: string;
  accountId: number;
  membershipId: number;
  username?: string | null;
  nickname?: string | null;
  phone?: string | null;
  email?: string | null;
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
    userHttp.post<ApiResponse<AuthToken>>('/auth/login', {
      ...payload,
      portalCode: 'stacko-mall-web'
    }),

  register: (payload: RegisterRequest) =>
    userHttp.post<ApiResponse<AuthToken>>('/auth/register', {
      ...payload,
      portalCode: 'stacko-mall-web'
    }),

  currentSession: () =>
    userHttp.get<ApiResponse<UserSession>>('/users/me'),

  currentMember: () =>
    http.get<ApiResponse<MemberProfile>>('/c/members/me'),

  getProducts: (categoryId?: string) =>
    http.get<ApiResponse<Product[]>>('/c/products', {
      params: categoryId ? { categoryId } : undefined
    }),

  getCategories: () =>
    http.get<ApiResponse<ProductCategory[]>>('/c/categories'),

  getProduct: (id: string) => http.get<ApiResponse<Product>>(`/c/products/${id}`),

  getStock: (productId: string) =>
    http.get<ApiResponse<Stock>>(`/c/stocks/${productId}`),

  listAddresses: () =>
    http.get<ApiResponse<ShippingAddress[]>>('/c/addresses'),

  createAddress: (payload: ShippingAddressPayload) =>
    http.post<ApiResponse<ShippingAddress>>('/c/addresses', payload),

  updateAddress: (id: string, payload: ShippingAddressPayload) =>
    http.put<ApiResponse<ShippingAddress>>(`/c/addresses/${id}`, payload),

  deleteAddress: (id: string) =>
    http.delete<ApiResponse<void>>(`/c/addresses/${id}`),

  setDefaultAddress: (id: string) =>
    http.post<ApiResponse<ShippingAddress>>(`/c/addresses/${id}/default`),

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

  confirmOrder: (id: string, idempotencyKey?: string) =>
    http.post<ApiResponse<Order>>(`/c/orders/${id}/confirm`, null, {
      headers: {
        'X-Idempotency-Key':
          idempotencyKey || createIdempotencyKey(`CONFIRM:${id}`)
      }
    }),

  paymentCallback: (payload: PaymentCallbackRequest) =>
    http.post<ApiResponse<string>>('/c/payments/callback', payload)
};
