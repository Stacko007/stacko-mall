import type { OrderStatus, ProductStatus } from '../services/api';

export const productStatusLabels: Record<ProductStatus, string> = {
  DRAFT: '草稿',
  ACTIVE: '上架',
  INACTIVE: '下架'
};

export const orderStatusLabels: Record<OrderStatus, string> = {
  CREATED: '待付款',
  PAID: '待发货',
  SHIPPED: '待收货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  CLOSED: '已关闭'
};
