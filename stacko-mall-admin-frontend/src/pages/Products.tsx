import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Modal,
  Select,
  Table,
  Tag,
  Typography,
  message
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { adminApi, Product, ProductStatus } from '../services/api';
import { formatDateTime } from '../utils/format';

const { Text } = Typography;

const statusColors: Record<ProductStatus, string> = {
  DRAFT: 'default',
  ACTIVE: 'green',
  INACTIVE: 'volcano'
};

type ProductFormValues = {
  name: string;
  description?: string;
  price: number;
  status?: ProductStatus;
};

export default function Products() {
  const [loading, setLoading] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Product | null>(null);
  const [form] = Form.useForm<ProductFormValues>();

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const resp = await adminApi.listProducts();
      if (!resp.data.success) {
        message.error(resp.data.message || '获取商品失败');
        return;
      }
      setProducts(resp.data.data || []);
    } catch (error) {
      // global handler
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record: Product) => {
    setEditing(record);
    form.setFieldsValue({
      name: record.name,
      description: record.description,
      price: record.price,
      status: record.status as ProductStatus
    });
    setModalOpen(true);
  };

  const onSubmit = async (values: ProductFormValues) => {
    try {
      if (editing) {
        const payload = {
          name: values.name,
          description: values.description,
          price: values.price,
          status: values.status
        };
        const resp = await adminApi.updateProduct(editing.id, payload);
        if (!resp.data.success) {
          message.error(resp.data.message || '更新商品失败');
          return;
        }
        message.success('商品已更新');
      } else {
        const payload = {
          name: values.name,
          description: values.description,
          price: values.price
        };
        const resp = await adminApi.createProduct(payload);
        if (!resp.data.success) {
          message.error(resp.data.message || '创建商品失败');
          return;
        }
        message.success('商品已创建');
      }
      setModalOpen(false);
      fetchProducts();
    } catch (error) {
      // global handler
    }
  };

  const columns = useMemo(
    () => [
      {
        title: '商品ID',
        dataIndex: 'id',
        width: 220,
        render: (value: string) => <Text code>{value}</Text>
      },
      {
        title: '名称',
        dataIndex: 'name',
        width: 180
      },
      {
        title: '描述',
        dataIndex: 'description',
        render: (value: string) => (
          <Text type="secondary" ellipsis={{ tooltip: value }}>
            {value || '-'}
          </Text>
        )
      },
      {
        title: '价格',
        dataIndex: 'price',
        width: 120,
        render: (value: number) => `¥${Number(value).toFixed(2)}`
      },
      {
        title: '状态',
        dataIndex: 'status',
        width: 120,
        render: (value: ProductStatus) => (
          <Tag color={statusColors[value] || 'default'}>{value}</Tag>
        )
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
        width: 120,
        render: (_: unknown, record: Product) => (
          <Button type="link" onClick={() => openEdit(record)}>
            编辑
          </Button>
        )
      }
    ],
    []
  );

  return (
    <Card
      title="商品管理"
      extra={
        <Button type="primary" onClick={openCreate}>
          新建商品
        </Button>
      }
      className="card-shadow"
    >
      <Table
        rowKey="id"
        columns={columns}
        dataSource={products}
        loading={loading}
        scroll={{ x: 960 }}
      />

      <Modal
        open={modalOpen}
        title={editing ? '编辑商品' : '新建商品'}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        okText={editing ? '保存' : '创建'}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={onSubmit}>
          <Form.Item
            name="name"
            label="商品名称"
            rules={[{ required: true, message: '请输入商品名称' }]}
          >
            <Input placeholder="例如：Stacko 组合礼包" />
          </Form.Item>
          <Form.Item name="description" label="商品描述">
            <Input.TextArea rows={3} placeholder="描述商品卖点" />
          </Form.Item>
          <Form.Item
            name="price"
            label="商品价格"
            rules={[{ required: true, message: '请输入价格' }]}
          >
            <InputNumber min={0.01} precision={2} style={{ width: '100%' }} />
          </Form.Item>
          {editing ? (
            <Form.Item
              name="status"
              label="商品状态"
              rules={[{ required: true, message: '请选择状态' }]}
            >
              <Select
                options={['DRAFT', 'ACTIVE', 'INACTIVE'].map((status) => ({
                  label: status,
                  value: status
                }))}
              />
            </Form.Item>
          ) : null}
        </Form>
      </Modal>
    </Card>
  );
}
