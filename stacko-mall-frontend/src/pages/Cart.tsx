import { Button, Card, Checkbox, Empty, InputNumber, List, Select, Space, Typography, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, ShippingAddress } from '../services/api';
import { cartStore, CartItem } from '../store/cart';
import { session } from '../store/session';
import { createIdempotencyKey } from '../utils/idempotency';
import { ProductCategoryLink } from '../components/ProductCategoryLink';
import { useProductCategoryLookup } from '../hooks/useProductCategoryLookup';

const { Title, Paragraph } = Typography;

export default function Cart() {
  const [items, setItems] = useState<CartItem[]>(cartStore.list());
  const [addresses, setAddresses] = useState<ShippingAddress[]>([]);
  const [addressId, setAddressId] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const { categoryMap, getProductCategoryId } = useProductCategoryLookup();

  const allSelected = useMemo(
    () => items.length > 0 && items.every((item) => item.selected),
    [items]
  );

  const total = useMemo(
    () =>
      items
        .filter((item) => item.selected)
        .reduce((sum, item) => sum + item.price * item.quantity, 0),
    [items]
  );

  const selectedItems = useMemo(() => items.filter((i) => i.selected), [items]);

  const refresh = (next: CartItem[]) => setItems([...next]);

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

  const handleSubmit = async () => {
    if (!session.getToken()) {
      message.warning('请先登录再下单');
      return;
    }
    if (selectedItems.length === 0) {
      message.warning('请选择商品');
      return;
    }
    if (!addressId) {
      message.warning('请先选择收货地址');
      navigate('/addresses');
      return;
    }
    setSubmitting(true);
    try {
      const resp = await api.createOrder(
        {
          addressId,
          items: selectedItems.map((item) => ({
            productId: item.productId,
            productName: item.name,
            price: item.price,
            quantity: item.quantity
          }))
        },
        createIdempotencyKey('ORDER_CREATE')
      );
      if (resp.data.success) {
        message.success('订单创建成功');
        refresh(cartStore.clearSelected());
        if (resp.data.data?.id) {
          navigate(`/payment/${resp.data.data.id}`);
        }
      } else {
        message.error(resp.data.message || '订单创建失败');
      }
    } catch (error) {
      // global handler will notify
    } finally {
      setSubmitting(false);
    }
  };

  if (items.length === 0) {
    return (
      <div>
        <Title level={2}>购物车</Title>
        <Empty description="购物车为空" />
      </div>
    );
  }

  return (
    <div>
      <Title level={2}>购物车</Title>
      <List
        grid={{ gutter: 16, column: 2 }}
        dataSource={items}
        renderItem={(item) => (
          <List.Item>
            <Card
              title={
                <Space>
                  <Checkbox
                    checked={item.selected}
                    onChange={(e) =>
                      refresh(cartStore.update(item.productId, { selected: e.target.checked }))
                    }
                  />
                  {item.name}
                </Space>
              }
              extra={
                <Button danger onClick={() => refresh(cartStore.remove(item.productId))}>
                  删除
                </Button>
              }
            >
              <Paragraph>单价：¥ {item.price}</Paragraph>
              <Paragraph>
                类目：<ProductCategoryLink categoryId={item.categoryId ?? getProductCategoryId(item.productId)} categoryMap={categoryMap} />
              </Paragraph>
              <Space>
                数量：
                <InputNumber
                  min={1}
                  value={item.quantity}
                  onChange={(value) =>
                    refresh(
                      cartStore.update(item.productId, {
                        quantity: value ? Number(value) : 1
                      })
                    )
                  }
                />
              </Space>
            </Card>
          </List.Item>
        )}
      />
      <Card style={{ marginTop: 16 }}>
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
          <Checkbox checked={allSelected} onChange={(e) => refresh(cartStore.toggleAll(e.target.checked))}>
            全选
          </Checkbox>
          <Paragraph style={{ marginBottom: 0 }}>合计：¥ {total}</Paragraph>
          <Button type="primary" loading={submitting} onClick={handleSubmit}>
            去结算
          </Button>
        </Space>
      </Card>
    </div>
  );
}
