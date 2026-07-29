import { Button, Card, InputNumber, Space, Typography, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api, Product } from '../services/api';
import { session } from '../store/session';
import { createIdempotencyKey } from '../utils/idempotency';

const { Title, Paragraph } = Typography;

function useQuery() {
  const { search } = useLocation();
  return useMemo(() => new URLSearchParams(search), [search]);
}

export default function ConfirmOrder() {
  const query = useQuery();
  const navigate = useNavigate();
  const productId = query.get('productId');
  const [product, setProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    if (!productId) return;
    const load = async () => {
      try {
        const resp = await api.getProduct(productId);
        if (resp.data.success) {
          setProduct(resp.data.data);
        } else {
          message.error(resp.data.message || '商品加载失败');
        }
      } catch (error) {
        message.error('商品加载失败');
      }
    };
    load();
  }, [productId]);

  const total = (product?.price || 0) * quantity;

  const handleSubmit = async () => {
    if (!session.getToken()) {
      message.warning('请先登录');
      navigate('/login');
      return;
    }
    if (!product) {
      message.error('商品信息缺失');
      return;
    }
    try {
      const resp = await api.createOrder(
        {
          items: [
            {
              productId: product.id,
              productName: product.name,
              price: product.price,
              quantity
            }
          ]
        },
        createIdempotencyKey('ORDER_CREATE')
      );
      if (resp.data.success) {
        message.success('订单创建成功');
        navigate(`/payment/${resp.data.data.id}`);
      } else {
        message.error(resp.data.message || '订单创建失败');
      }
    } catch (error) {
      // global handler will notify
    }
  };

  if (!productId) {
    return <div>缺少商品参数</div>;
  }

  return (
    <div>
      <Title level={2}>确认订单</Title>
      {product ? (
        <Card>
          <Paragraph>商品：{product.name}</Paragraph>
          <Paragraph>单价：¥ {product.price}</Paragraph>
          <Space>
            数量：
            <InputNumber min={1} value={quantity} onChange={(v) => setQuantity(v || 1)} />
          </Space>
          <Paragraph style={{ marginTop: 12 }}>合计：¥ {total}</Paragraph>
          <Button type="primary" onClick={handleSubmit}>
            提交订单
          </Button>
        </Card>
      ) : (
        <div>加载中...</div>
      )}
    </div>
  );
}
