import {
  Button,
  Card,
  Form,
  Input,
  Modal,
  Space,
  Table,
  Tag,
  Tooltip,
  Typography,
  message
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminApi, Order, OrderStatus } from '../services/api';
import { formatDateTime } from '../utils/format';
import { orderStatusColors, orderStatusLabels } from '../utils/orderStatus';

type ShipFormValues = {
  carrier: string;
  trackingNo: string;
};

export default function Orders() {
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState<Order[]>([]);
  const [shipOpen, setShipOpen] = useState(false);
  const [shippingOrder, setShippingOrder] = useState<Order | null>(null);
  const [searchForm] = Form.useForm();
  const [shipForm] = Form.useForm<ShipFormValues>();
  const navigate = useNavigate();

  const fetchOrders = async (buyerId?: string) => {
    setLoading(true);
    try {
      const resp = await adminApi.listOrders(buyerId);
      if (!resp.data.success) {
        message.error(resp.data.message || '获取订单失败');
        return;
      }
      setOrders(resp.data.data || []);
    } catch (error) {
      // global handler
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const openShip = (record: Order) => {
    setShippingOrder(record);
    shipForm.setFieldsValue({
      carrier: record.shippingCarrier || '',
      trackingNo: record.trackingNo || ''
    });
    setShipOpen(true);
  };

  const closeOrder = async (record: Order) => {
    Modal.confirm({
      title: '确认关闭订单',
      content: `订单 ${record.id} 将被关闭，无法继续履约。`,
      okText: '确认关闭',
      cancelText: '取消',
      onOk: async () => {
        const resp = await adminApi.closeOrder(record.id);
        if (!resp.data.success) {
          message.error(resp.data.message || '关闭订单失败');
          return;
        }
        message.success('订单已关闭');
        fetchOrders(searchForm.getFieldValue('buyerId'));
      }
    });
  };

  const onShipSubmit = async (values: ShipFormValues) => {
    if (!shippingOrder) return;
    const resp = await adminApi.shipOrder(shippingOrder.id, values);
    if (!resp.data.success) {
      message.error(resp.data.message || '发货失败');
      return;
    }
    message.success('订单已发货');
    setShipOpen(false);
    fetchOrders(searchForm.getFieldValue('buyerId'));
  };

  const columns = useMemo(
    () => [
      {
        title: '订单ID',
        dataIndex: 'id',
        width: 220,
        render: (value: string) => <Typography.Text code>{value}</Typography.Text>
      },
      {
        title: '买家',
        dataIndex: 'buyerName',
        width: 180,
        render: (value: string | undefined, record: Order) => (
          <Space direction="vertical" size={0}>
            <Typography.Text>{value || '-'}</Typography.Text>
            <Typography.Text type="secondary">{record.buyerId}</Typography.Text>
          </Space>
        )
      },
      {
        title: '状态',
        dataIndex: 'status',
        width: 120,
        render: (value: OrderStatus) => (
          <Tag color={orderStatusColors[value] || 'default'}>
            {orderStatusLabels[value] || value}
          </Tag>
        )
      },
      {
        title: '金额',
        dataIndex: 'totalAmount',
        width: 120,
        render: (value: number) => `¥${Number(value).toFixed(2)}`
      },
      {
        title: '创建时间',
        dataIndex: 'createdAt',
        width: 180,
        render: (value: string) => formatDateTime(value)
      },
      {
        title: '操作',
        key: 'actions',
        width: 220,
        render: (_: unknown, record: Order) => (
          <Space>
            <Button type="link" onClick={() => navigate(`/admin/orders/${record.id}`)}>
              详情
            </Button>
            <Tooltip title={record.status !== 'PAID' ? '订单支付后才能发货' : undefined}>
              <span>
                <Button
                  type="link"
                  disabled={record.status !== 'PAID'}
                  onClick={() => openShip(record)}
                >
                  发货
                </Button>
              </span>
            </Tooltip>
            <Button
              type="link"
              danger
              disabled={!['CREATED', 'PAID'].includes(record.status)}
              onClick={() => closeOrder(record)}
            >
              关闭
            </Button>
          </Space>
        )
      }
    ],
    [navigate]
  );

  return (
    <Card title="订单管理" className="card-shadow">
      <Form
        form={searchForm}
        layout="inline"
        onFinish={(values) => fetchOrders(values.buyerId)}
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="buyerId" label="买家ID">
          <Input placeholder="可选，输入买家ID过滤" />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">
              查询
            </Button>
            <Button
              onClick={() => {
                searchForm.resetFields();
                fetchOrders();
              }}
            >
              重置
            </Button>
          </Space>
        </Form.Item>
      </Form>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={orders}
        loading={loading}
        scroll={{ x: 980 }}
      />

      <Modal
        open={shipOpen}
        title="订单发货"
        okText="确认发货"
        onCancel={() => setShipOpen(false)}
        onOk={() => shipForm.submit()}
        destroyOnClose
      >
        <Form form={shipForm} layout="vertical" onFinish={onShipSubmit}>
          <Form.Item
            name="carrier"
            label="物流公司"
            rules={[{ required: true, message: '请输入物流公司' }]}
          >
            <Input placeholder="例如：顺丰" maxLength={32} />
          </Form.Item>
          <Form.Item
            name="trackingNo"
            label="物流单号"
            rules={[{ required: true, message: '请输入物流单号' }]}
          >
            <Input placeholder="例如：SF123456" maxLength={64} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
