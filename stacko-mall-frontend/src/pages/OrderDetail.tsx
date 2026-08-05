import { Button, Card, Descriptions, List, Popconfirm, Skeleton, Space, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api, Order } from '../services/api';
import { EmptyState, ErrorState } from '../components/State';
import { getErrorMessage } from '../utils/error';
import { createIdempotencyKey } from '../utils/idempotency';
import { orderStatusLabels } from '../utils/status';
import { ProductCategoryLink } from '../components/ProductCategoryLink';
import { useProductCategoryLookup } from '../hooks/useProductCategoryLookup';

const { Title } = Typography;

export default function OrderDetail() {
  const { id } = useParams();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { categoryMap, getProductCategoryId } = useProductCategoryLookup();

  useEffect(() => {
    if (!id) return;
    load();
  }, [id]);

  const load = async () => {
    if (!id) return;
    try {
      const resp = await api.getOrder(id);
      if (resp.data.success) {
        setOrder(resp.data.data);
        setError(null);
      } else {
        const msg = resp.data.message || '订单加载失败';
        setError(msg);
        message.error(msg);
      }
    } catch (error) {
      const msg = getErrorMessage(error, '订单加载失败');
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async () => {
    if (!id) return;
    try {
      const resp = await api.confirmOrder(id, createIdempotencyKey(`CONFIRM:${id}`));
      if (resp.data.success) {
        message.success('已确认收货');
        setOrder(resp.data.data);
        setError(null);
      } else {
        message.error(resp.data.message || '确认收货失败');
      }
    } catch (error) {
      // global handler will notify
    }
  };

  if (!id) return <div>缺少订单参数</div>;
  if (loading) return <Skeleton active />;
  if (error) return <ErrorState description={error} />;

  return (
    <div>
      <Link to="/orders">返回订单列表</Link>
      <Title level={2} style={{ marginTop: 12 }}>订单详情</Title>
      {order ? (
        <Card>
          <Descriptions bordered column={1}>
            <Descriptions.Item label="订单号">{order.id}</Descriptions.Item>
            <Descriptions.Item label="状态">
              {orderStatusLabels[order.status] || order.status}
            </Descriptions.Item>
            <Descriptions.Item label="金额">¥ {order.totalAmount}</Descriptions.Item>
            <Descriptions.Item label="收货人">
              {order.receiverName ? `${order.receiverName} ${order.receiverPhone || ''}` : '暂无'}
            </Descriptions.Item>
            <Descriptions.Item label="收货地址">
              {[
                order.receiverProvince,
                order.receiverCity,
                order.receiverDistrict,
                order.receiverAddress
              ].filter(Boolean).join(' ') || '暂无'}
            </Descriptions.Item>
            <Descriptions.Item label="物流公司">
              {order.shippingCarrier || '暂无'}
            </Descriptions.Item>
            <Descriptions.Item label="物流单号">
              {order.trackingNo || '暂无'}
            </Descriptions.Item>
            <Descriptions.Item label="发货时间">
              {order.shippedAt || '暂无'}
            </Descriptions.Item>
            <Descriptions.Item label="完成时间">
              {order.completedAt || '暂无'}
            </Descriptions.Item>
          </Descriptions>
          {order.status === 'SHIPPED' ? (
            <Space style={{ marginTop: 16 }}>
              <Popconfirm
                title="确认已收到商品？"
                okText="确认收货"
                cancelText="再等等"
                onConfirm={handleConfirm}
              >
                <Button type="primary">确认收货</Button>
              </Popconfirm>
            </Space>
          ) : null}
          <Title level={4} style={{ marginTop: 16 }}>商品</Title>
          {order.items && order.items.length > 0 ? (
            <List
              bordered
              dataSource={order.items || []}
              renderItem={(item) => (
                <List.Item>
                  <Space wrap>
                    <span>{item.productName} × {item.quantity}（¥ {item.price}）</span>
                    <span>
                      类目：<ProductCategoryLink categoryId={getProductCategoryId(item.productId)} categoryMap={categoryMap} />
                    </span>
                  </Space>
                </List.Item>
              )}
            />
          ) : (
            <EmptyState title="暂无商品明细" />
          )}
        </Card>
      ) : (
        <EmptyState title="订单不存在" />
      )}
    </div>
  );
}
