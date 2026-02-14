import { Button, Card, Result, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api, Order } from '../services/api';
import { ErrorState } from '../components/State';
import { getErrorMessage } from '../utils/error';

const { Paragraph } = Typography;

export default function PaymentResult() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    let timer: number | undefined;
    let attempts = 0;
    const load = async (silent = false) => {
      try {
        const resp = await api.getOrder(id);
        if (resp.data.success) {
          setOrder(resp.data.data);
          if (!silent) {
            setRefreshing(false);
          }
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

    timer = window.setInterval(() => {
      attempts += 1;
      if (attempts > 5) {
        window.clearInterval(timer);
        return;
      }
      load(true);
    }, 2000);

    return () => {
      if (timer) window.clearInterval(timer);
    };
  }, [id]);

  if (!id) {
    return <div>缺少订单参数</div>;
  }

  const isPaid = order?.status === 'PAID' || order?.status === 'COMPLETED';

  if (error) {
    return <ErrorState description={error} />;
  }

  return (
    <Result
      status={isPaid ? 'success' : 'warning'}
      title={isPaid ? '支付成功' : '支付未完成'}
      subTitle={order ? `订单号：${order.id}` : '订单加载中...'}
      extra={[
        <Button type="primary" key="orders" onClick={() => navigate('/orders')}>
          查看订单
        </Button>,
        <Button key="home" onClick={() => navigate('/')}>返回首页</Button>,
        <Button
          key="refresh"
          loading={refreshing}
          onClick={async () => {
            if (!id) return;
            setRefreshing(true);
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
              setRefreshing(false);
            }
          }}
        >
          刷新状态
        </Button>
      ]}
    >
      {loading ? (
        <div>加载中...</div>
      ) : order && (
        <Card>
          <Paragraph>金额：¥ {order.totalAmount}</Paragraph>
          <Paragraph>状态：{order.status}</Paragraph>
        </Card>
      )}
    </Result>
  );
}
