import type { OrderStatus } from '../services/api';

export const orderStatusLabels: Record<OrderStatus, string> = {
  CREATED: '待付款',
  PAID: '待发货',
  SHIPPED: '待收货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  CLOSED: '已关闭'
};

export const orderStatusColors: Record<OrderStatus, string> = {
  CREATED: 'default',
  PAID: 'blue',
  SHIPPED: 'geekblue',
  COMPLETED: 'green',
  CANCELLED: 'orange',
  CLOSED: 'volcano'
};
