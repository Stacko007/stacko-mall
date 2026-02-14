import { Button, Card, Checkbox, Empty, InputNumber, List, Space, Typography, message } from 'antd';
import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { cartStore, CartItem } from '../store/cart';
import { session } from '../store/session';
import { createIdempotencyKey } from '../utils/idempotency';

const { Title, Paragraph } = Typography;

export default function Cart() {
  const [items, setItems] = useState<CartItem[]>(cartStore.list());
  const navigate = useNavigate();

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

  const handleSubmit = async () => {
    const buyerId = session.getBuyerId();
    if (!buyerId) {
      message.warning('请先登录再下单');
      return;
    }
    if (selectedItems.length === 0) {
      message.warning('请选择商品');
      return;
    }
    try {
      const resp = await api.createOrder(
        {
          buyerId,
          items: selectedItems.map((item) => ({
            productId: item.productId,
            productName: item.name,
            price: item.price,
            quantity: item.quantity
          }))
        },
        createIdempotencyKey(`ORDER_CREATE:${buyerId}`)
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
        <Space>
          <Checkbox checked={allSelected} onChange={(e) => refresh(cartStore.toggleAll(e.target.checked))}>
            全选
          </Checkbox>
          <Paragraph style={{ marginBottom: 0 }}>合计：¥ {total}</Paragraph>
          <Button type="primary" onClick={handleSubmit}>
            去结算
          </Button>
        </Space>
      </Card>
    </div>
  );
}
