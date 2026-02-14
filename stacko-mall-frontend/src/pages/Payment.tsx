import { Button, Card, List, Space, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api, Order } from '../services/api';
import { createIdempotencyKey } from '../utils/idempotency';
import { getErrorMessage } from '../utils/error';

const { Title, Paragraph } = Typography;

export default function Payment() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    const load = async () => {
      try {
        const resp = await api.getOrder(id);
        if (resp.data.success) {
          setOrder(resp.data.data);
        } else {
          message.error(resp.data.message || '订单加载失败');
        }
      } catch (error) {
        // global handler will notify
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  const handlePay = async () => {
    if (!id) return;
    try {
      const resp = await api.payOrder(id, createIdempotencyKey(`PAY:${id}`));
      if (resp.data.success) {
        message.success('支付成功');
        navigate(`/payment-result/${id}`);
      } else {
        message.error(resp.data.message || '支付失败');
      }
    } catch (error) {
      // global handler will notify
    }
  };

  const handleCallback = async () => {
    if (!id) return;
    try {
      const resp = await api.paymentCallback({
        callbackId: createIdempotencyKey('CALLBACK'),
        orderId: id,
        tradeNo: createIdempotencyKey('TRADE'),
        status: 'PAID',
        signature: 'mock-signature',
        rawPayload: 'mock'
      });
      if (resp.data.success) {
        message.success('回调已触发');
      } else {
        message.error(resp.data.message || '回调失败');
      }
    } catch (error) {
      // global handler will notify
    }
  };

  if (!id) {
    return <div>缺少订单参数</div>;
  }

  const statusLabel: Record<string, string> = {
    CREATED: '待付款',
    PAID: '待发货',
    SHIPPED: '待收货',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    CLOSED: '已关闭'
  };

  return (
    <div>
      <Title level={2}>支付</Title>
      {loading ? (
        <div>加载中...</div>
      ) : order ? (
        <Card>
          <Paragraph>
            订单号：{order.id} <Tag>{statusLabel[order.status] || order.status}</Tag>
          </Paragraph>
          <Paragraph>金额：¥ {order.totalAmount}</Paragraph>
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
                </div>
                <div className="order-item-amount">¥ {item.price * item.quantity}</div>
              </List.Item>
            )}
          />
          <Space>
            <Button type="primary" onClick={handlePay}>
              模拟支付
            </Button>
            <Button onClick={handleCallback}>模拟回调</Button>
            <Button onClick={() => navigate('/orders')}>返回订单</Button>
          </Space>
        </Card>
      ) : (
        <div>订单不存在</div>
      )}
    </div>
  );
}
