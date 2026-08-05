import { Button, Card, InputNumber, Select, Space, Typography, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api, Product, ShippingAddress } from '../services/api';
import { session } from '../store/session';
import { createIdempotencyKey } from '../utils/idempotency';
import { ProductCategoryLink } from '../components/ProductCategoryLink';
import { useProductCategories } from '../hooks/useProductCategories';

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
  const [addresses, setAddresses] = useState<ShippingAddress[]>([]);
  const [addressId, setAddressId] = useState('');
  const [quantity, setQuantity] = useState(1);
  const { categoryMap } = useProductCategories();

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

  useEffect(() => {
    if (!session.getToken()) return;
    const loadAddresses = async () => {
      try {
        const resp = await api.listAddresses();
        if (resp.data.success) {
          const next = resp.data.data || [];
          setAddresses(next);
          const defaultAddress = next.find((address) => address.defaultAddress) || next[0];
          setAddressId(defaultAddress?.id || '');
        }
      } catch {
        // global handler will notify
      }
    };
    loadAddresses();
  }, []);

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
    if (!addressId) {
      message.warning('请先选择收货地址');
      navigate('/addresses');
      return;
    }
    try {
      const resp = await api.createOrder(
        {
          addressId,
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
          <Paragraph>
            类目：<ProductCategoryLink categoryId={product.categoryId} categoryMap={categoryMap} />
          </Paragraph>
          <Paragraph>单价：¥ {product.price}</Paragraph>
          <Space direction="vertical" style={{ width: '100%', marginBottom: 12 }}>
            <span>收货地址：</span>
            <Select
              value={addressId || undefined}
              placeholder="请选择收货地址"
              style={{ width: '100%' }}
              options={addresses.map((address) => ({
                value: address.id,
                label: `${address.receiverName} ${address.receiverPhone} ${[
                  address.province,
                  address.city,
                  address.district,
                  address.detailAddress
                ].filter(Boolean).join(' ')}${address.defaultAddress ? '（默认）' : ''}`
              }))}
              onChange={setAddressId}
            />
            {addresses.length === 0 ? (
              <Button onClick={() => navigate('/addresses')}>新增收货地址</Button>
            ) : null}
          </Space>
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
