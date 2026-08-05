import { Button, Card, List, Popconfirm, Space, Tag, Tabs, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, Order } from '../services/api';
import { session } from '../store/session';
import { createIdempotencyKey } from '../utils/idempotency';
import { EmptyState, ErrorState } from '../components/State';
import { getErrorMessage } from '../utils/error';
import { ProductCategoryLink } from '../components/ProductCategoryLink';
import { useProductCategoryLookup } from '../hooks/useProductCategoryLookup';

const { Title } = Typography;
const statusMap: Record<
  string,
  { label: string; color: string }
> = {
  CREATED: { label: '待付款', color: 'orange' },
  PAID: { label: '待发货', color: 'blue' },
  SHIPPED: { label: '待收货', color: 'cyan' },
  COMPLETED: { label: '已完成', color: 'green' },
  CANCELLED: { label: '已取消', color: 'default' },
  CLOSED: { label: '已关闭', color: 'default' }
};

export default function Orders() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [status, setStatus] = useState('ALL');
  const [error, setError] = useState<string | null>(null);
  const { categoryMap, getProductCategoryId } = useProductCategoryLookup();

  const load = async () => {
    if (!session.getToken()) {
      message.warning('请先登录获取订单列表');
      setOrders([]);
      return;
    }
    try {
      const resp = await api.listOrders();
      if (resp.data.success) {
        setOrders(resp.data.data || []);
        setError(null);
      } else {
        const msg = resp.data.message || '订单加载失败';
        setError(msg);
        message.error(msg);
      }
    } catch (error) {
      const msg = getErrorMessage(error, '订单加载失败');
      setError(msg);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const filtered = status === 'ALL' ? orders : orders.filter((o) => o.status === status);

  const handlePay = async (id: string) => {
    try {
      const resp = await api.payOrder(id, createIdempotencyKey(`PAY:${id}`));
      if (resp.data.success) {
        message.success('支付成功');
        load();
      } else {
        message.error(resp.data.message || '支付失败');
      }
    } catch (error) {
      // global handler will notify
    }
  };

  const handleCancel = async (id: string) => {
    try {
      const resp = await api.cancelOrder(id, createIdempotencyKey(`CANCEL:${id}`));
      if (resp.data.success) {
        message.success('取消成功');
        load();
      } else {
        message.error(resp.data.message || '取消失败');
      }
    } catch (error) {
      // global handler will notify
    }
  };

  const handleConfirm = async (id: string) => {
    try {
      const resp = await api.confirmOrder(id, createIdempotencyKey(`CONFIRM:${id}`));
      if (resp.data.success) {
        message.success('已确认收货');
        load();
      } else {
        message.error(resp.data.message || '确认收货失败');
      }
    } catch (error) {
      // global handler will notify
    }
  };

  return (
    <div>
      <Title level={2}>订单</Title>
      <Tabs
        activeKey={status}
        onChange={(nextStatus) => {
          setStatus(nextStatus);
          load();
        }}
        items={[
          { key: 'ALL', label: '全部' },
          { key: 'CREATED', label: '待付款' },
          { key: 'PAID', label: '待发货' },
          { key: 'SHIPPED', label: '待收货' },
          { key: 'COMPLETED', label: '已完成' },
          { key: 'CANCELLED', label: '已取消' }
        ]}
      />
      {error ? (
        <ErrorState description={error} onRetry={load} />
      ) : filtered.length === 0 ? (
        <EmptyState title="暂无订单" />
      ) : (
        <List
          dataSource={filtered}
          pagination={{ pageSize: 6 }}
          renderItem={(order) => {
            const statusInfo = statusMap[order.status] || {
              label: order.status,
              color: 'default'
            };
            return (
              <List.Item>
                <Card
                  className="order-card"
                  title={
                    <Space wrap>
                      <span>订单号：</span>
                      <Link to={`/orders/${order.id}`}>{order.id}</Link>
                      <Tag color={statusInfo.color}>{statusInfo.label}</Tag>
                    </Space>
                  }
                  extra={<span>¥ {order.totalAmount}</span>}
                >
                  <List
                    dataSource={order.items || []}
                    locale={{ emptyText: '暂无商品明细' }}
                    renderItem={(item) => (
                      <List.Item className="order-item">
                        <div className="order-item-image" />
                        <div className="order-item-info">
                          <div className="order-item-name">{item.productName}</div>
                          <div className="order-item-meta">
                            ¥ {item.price} × {item.quantity}
                          </div>
                          <div className="order-item-meta">
                            类目：<ProductCategoryLink categoryId={getProductCategoryId(item.productId)} categoryMap={categoryMap} />
                          </div>
                        </div>
                        <div className="order-item-amount">¥ {item.price * item.quantity}</div>
                      </List.Item>
                    )}
                  />
                  <Space style={{ marginTop: 12 }}>
                    {order.status === 'CREATED' && (
                      <Button type="primary" onClick={() => handlePay(order.id)}>
                        去支付
                      </Button>
                    )}
                    {order.status === 'CREATED' && (
                      <Button danger onClick={() => handleCancel(order.id)}>
                        取消
                      </Button>
                    )}
                    {order.status === 'SHIPPED' && (
                      <Popconfirm
                        title="确认已收到商品？"
                        okText="确认收货"
                        cancelText="再等等"
                        onConfirm={() => handleConfirm(order.id)}
                      >
                        <Button type="primary">确认收货</Button>
                      </Popconfirm>
                    )}
                    {order.status !== 'CREATED' && (
                      <Button onClick={() => message.info('售后功能建设中')}>
                        申请售后
                      </Button>
                    )}
                    <Button type="link">
                      <Link to={`/orders/${order.id}`}>查看详情</Link>
                    </Button>
                  </Space>
                </Card>
              </List.Item>
            );
          }}
        />
      )}
    </div>
  );
}
