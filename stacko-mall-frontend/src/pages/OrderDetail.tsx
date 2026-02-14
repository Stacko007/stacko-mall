import { Card, Descriptions, List, Skeleton, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api, Order } from '../services/api';
import { EmptyState, ErrorState } from '../components/State';
import { getErrorMessage } from '../utils/error';

const { Title } = Typography;

export default function OrderDetail() {
  const { id } = useParams();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    const load = async () => {
      try {
        const resp = await api.getOrder(id);
        if (resp.data.success) {
          setOrder(resp.data.data);
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
    load();
  }, [id]);

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
            <Descriptions.Item label="状态">{order.status}</Descriptions.Item>
            <Descriptions.Item label="金额">¥ {order.totalAmount}</Descriptions.Item>
            <Descriptions.Item label="物流公司">
              {order.shippingCarrier || '暂无'}
            </Descriptions.Item>
            <Descriptions.Item label="物流单号">
              {order.trackingNo || '暂无'}
            </Descriptions.Item>
          </Descriptions>
          <Title level={4} style={{ marginTop: 16 }}>商品</Title>
          {order.items && order.items.length > 0 ? (
            <List
              bordered
              dataSource={order.items || []}
              renderItem={(item) => (
                <List.Item>
                  {item.productName} × {item.quantity}（¥ {item.price}）
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
