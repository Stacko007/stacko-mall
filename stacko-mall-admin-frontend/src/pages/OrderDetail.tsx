import {
  Button,
  Card,
  Descriptions,
  Input,
  Modal,
  Space,
  Table,
  Tag,
  Typography,
  message
} from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { adminApi, Order, OrderStatus } from '../services/api';
import { formatDateTime } from '../utils/format';

const statusColor: Record<OrderStatus, string> = {
  CREATED: 'default',
  PAID: 'blue',
  SHIPPED: 'geekblue',
  COMPLETED: 'green',
  CANCELLED: 'orange',
  CLOSED: 'volcano'
};

type ShipFormValues = {
  carrier: string;
  trackingNo: string;
};

export default function OrderDetail() {
  const { id } = useParams();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(false);
  const [shipOpen, setShipOpen] = useState(false);
  const [shipForm, setShipForm] = useState<ShipFormValues>({
    carrier: '',
    trackingNo: ''
  });
  const navigate = useNavigate();

  const fetchOrder = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const resp = await adminApi.getOrder(id);
      if (!resp.data.success) {
        message.error(resp.data.message || '获取订单失败');
        return;
      }
      setOrder(resp.data.data);
    } catch (error) {
      // global handler
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrder();
  }, [id]);

  const openShip = () => {
    if (!order) return;
    setShipForm({
      carrier: order.shippingCarrier || '',
      trackingNo: order.trackingNo || ''
    });
    setShipOpen(true);
  };

  const handleShip = async () => {
    if (!order) return;
    if (!shipForm.carrier || !shipForm.trackingNo) {
      message.warning('请填写物流公司和单号');
      return;
    }
    const resp = await adminApi.shipOrder(order.id, shipForm);
    if (!resp.data.success) {
      message.error(resp.data.message || '发货失败');
      return;
    }
    message.success('订单已发货');
    setShipOpen(false);
    fetchOrder();
  };

  const handleClose = async () => {
    if (!order) return;
    Modal.confirm({
      title: '确认关闭订单',
      content: `订单 ${order.id} 将被关闭。`,
      okText: '确认关闭',
      cancelText: '取消',
      onOk: async () => {
        const resp = await adminApi.closeOrder(order.id);
        if (!resp.data.success) {
          message.error(resp.data.message || '关闭订单失败');
          return;
        }
        message.success('订单已关闭');
        fetchOrder();
      }
    });
  };

  const itemColumns = [
    {
      title: '商品ID',
      dataIndex: 'productId',
      render: (value: string) => <Typography.Text code>{value}</Typography.Text>
    },
    { title: '商品名称', dataIndex: 'productName' },
    {
      title: '单价',
      dataIndex: 'price',
      render: (value: number) => `¥${Number(value).toFixed(2)}`
    },
    { title: '数量', dataIndex: 'quantity' },
    {
      title: '金额',
      dataIndex: 'amount',
      render: (value: number) => `¥${Number(value).toFixed(2)}`
    }
  ];

  return (
    <Card
      title="订单详情"
      className="card-shadow"
      loading={loading}
      extra={
        <Space>
          <Button onClick={() => navigate(-1)}>返回</Button>
          <Button
            type="primary"
            disabled={!order || order.status !== 'PAID'}
            onClick={openShip}
          >
            发货
          </Button>
          <Button
            danger
            disabled={!order || !['CREATED', 'PAID'].includes(order.status)}
            onClick={handleClose}
          >
            关闭订单
          </Button>
        </Space>
      }
    >
      {order ? (
        <>
          <Descriptions column={2} bordered size="middle" style={{ marginBottom: 24 }}>
            <Descriptions.Item label="订单ID">
              <Typography.Text code>{order.id}</Typography.Text>
            </Descriptions.Item>
            <Descriptions.Item label="买家ID">{order.buyerId}</Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusColor[order.status]}>{order.status}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="订单金额">
              ¥{Number(order.totalAmount).toFixed(2)}
            </Descriptions.Item>
            <Descriptions.Item label="物流公司">
              {order.shippingCarrier || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="物流单号">
              {order.trackingNo || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {formatDateTime(order.createdAt)}
            </Descriptions.Item>
            <Descriptions.Item label="更新时间">
              {formatDateTime(order.updatedAt)}
            </Descriptions.Item>
            <Descriptions.Item label="发货时间">
              {formatDateTime(order.shippedAt)}
            </Descriptions.Item>
            <Descriptions.Item label="完成时间">
              {formatDateTime(order.completedAt)}
            </Descriptions.Item>
          </Descriptions>
          <Table
            rowKey="productId"
            columns={itemColumns}
            dataSource={order.items || []}
            pagination={false}
          />
        </>
      ) : (
        <Typography.Text type="secondary">暂无订单数据</Typography.Text>
      )}

      <Modal
        open={shipOpen}
        title="订单发货"
        okText="确认发货"
        onCancel={() => setShipOpen(false)}
        onOk={handleShip}
      >
        <Space direction="vertical" style={{ width: '100%' }}>
          <div>
            <Typography.Text>物流公司</Typography.Text>
            <Input
              value={shipForm.carrier}
              onChange={(e) =>
                setShipForm((prev) => ({ ...prev, carrier: e.target.value }))
              }
              placeholder="例如：顺丰"
              maxLength={32}
            />
          </div>
          <div>
            <Typography.Text>物流单号</Typography.Text>
            <Input
              value={shipForm.trackingNo}
              onChange={(e) =>
                setShipForm((prev) => ({ ...prev, trackingNo: e.target.value }))
              }
              placeholder="例如：SF123456"
              maxLength={64}
            />
          </div>
        </Space>
      </Modal>
    </Card>
  );
}
