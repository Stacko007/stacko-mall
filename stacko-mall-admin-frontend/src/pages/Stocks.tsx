import {
  Button,
  Card,
  Form,
  InputNumber,
  Modal,
  Space,
  Table,
  Typography,
  message
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { adminApi, Stock } from '../services/api';
import { formatDateTime } from '../utils/format';

const { Text } = Typography;

type StockFormValues = {
  quantity?: number;
  delta?: number;
};

export default function Stocks() {
  const [loading, setLoading] = useState(false);
  const [stocks, setStocks] = useState<Stock[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [actionType, setActionType] = useState<'set' | 'adjust'>('set');
  const [selected, setSelected] = useState<Stock | null>(null);
  const [form] = Form.useForm<StockFormValues>();

  const fetchStocks = async () => {
    setLoading(true);
    try {
      const resp = await adminApi.listStocks();
      if (!resp.data.success) {
        message.error(resp.data.message || '获取库存失败');
        return;
      }
      setStocks(resp.data.data || []);
    } catch (error) {
      // global handler
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStocks();
  }, []);

  const openSet = (record: Stock) => {
    setSelected(record);
    setActionType('set');
    form.setFieldsValue({ quantity: record.quantity });
    setModalOpen(true);
  };

  const openAdjust = (record: Stock) => {
    setSelected(record);
    setActionType('adjust');
    form.setFieldsValue({ delta: 0 });
    setModalOpen(true);
  };

  const onSubmit = async (values: StockFormValues) => {
    if (!selected) return;
    if (actionType === 'set') {
      const resp = await adminApi.setStock(selected.productId, {
        quantity: values.quantity ?? 0
      });
      if (!resp.data.success) {
        message.error(resp.data.message || '设置库存失败');
        return;
      }
      message.success('库存已更新');
    } else {
      const resp = await adminApi.adjustStock(selected.productId, {
        delta: values.delta ?? 0
      });
      if (!resp.data.success) {
        message.error(resp.data.message || '调整库存失败');
        return;
      }
      message.success('库存已调整');
    }
    setModalOpen(false);
    fetchStocks();
  };

  const columns = useMemo(
    () => [
      {
        title: '商品ID',
        dataIndex: 'productId',
        width: 220,
        render: (value: string) => <Text code>{value}</Text>
      },
      {
        title: '库存数量',
        dataIndex: 'quantity',
        width: 120
      },
      {
        title: '更新时间',
        dataIndex: 'updatedAt',
        width: 180,
        render: (value: string) => formatDateTime(value)
      },
      {
        title: '操作',
        key: 'actions',
        width: 180,
        render: (_: unknown, record: Stock) => (
          <Space>
            <Button type="link" onClick={() => openSet(record)}>
              设置库存
            </Button>
            <Button type="link" onClick={() => openAdjust(record)}>
              调整库存
            </Button>
          </Space>
        )
      }
    ],
    []
  );

  return (
    <Card title="库存管理" className="card-shadow">
      <Table
        rowKey="productId"
        columns={columns}
        dataSource={stocks}
        loading={loading}
        scroll={{ x: 760 }}
      />

      <Modal
        open={modalOpen}
        title={actionType === 'set' ? '设置库存' : '调整库存'}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        okText={actionType === 'set' ? '确认设置' : '确认调整'}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={onSubmit}>
          {actionType === 'set' ? (
            <Form.Item
              name="quantity"
              label="库存数量"
              rules={[{ required: true, message: '请输入库存数量' }]}
            >
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
          ) : (
            <Form.Item
              name="delta"
              label="库存增减"
              rules={[{ required: true, message: '请输入调整数量' }]}
            >
              <InputNumber style={{ width: '100%' }} />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </Card>
  );
}
