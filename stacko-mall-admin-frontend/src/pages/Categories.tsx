import {
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message
} from 'antd';
import { useEffect, useState } from 'react';
import { adminApi, ProductCategory, ProductCategoryStatus } from '../services/api';
import { formatDateTime } from '../utils/format';

const { Text } = Typography;

const statusLabels: Record<ProductCategoryStatus, string> = {
  ENABLED: '启用',
  DISABLED: '停用'
};

const statusColors: Record<ProductCategoryStatus, string> = {
  ENABLED: 'green',
  DISABLED: 'default'
};

type CategoryFormValues = {
  parentId?: string;
  name: string;
  sort?: number;
  status: ProductCategoryStatus;
};

const flattenCategories = (items: ProductCategory[], excludeId?: string): ProductCategory[] =>
  items.flatMap((item) => {
    const children = flattenCategories(item.children || [], excludeId);
    return item.id === excludeId ? children : [item, ...children];
  });

export default function Categories() {
  const [categories, setCategories] = useState<ProductCategory[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<ProductCategory | null>(null);
  const [form] = Form.useForm<CategoryFormValues>();

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const resp = await adminApi.listCategories();
      if (!resp.data.success) {
        message.error(resp.data.message || '获取类目失败');
        return;
      }
      setCategories(resp.data.data || []);
    } catch {
      // global handler
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ status: 'ENABLED', sort: 0 });
    setModalOpen(true);
  };

  const openEdit = (category: ProductCategory) => {
    setEditing(category);
    form.setFieldsValue({
      parentId: category.parentId || undefined,
      name: category.name,
      sort: category.sort,
      status: category.status
    });
    setModalOpen(true);
  };

  const submit = async (values: CategoryFormValues) => {
    const payload = {
      parentId: values.parentId || null,
      name: values.name,
      sort: values.sort,
      status: values.status
    };
    try {
      const resp = editing
        ? await adminApi.updateCategory(editing.id, payload)
        : await adminApi.createCategory(payload);
      if (!resp.data.success) {
        message.error(resp.data.message || '保存类目失败');
        return;
      }
      message.success('类目已保存');
      setModalOpen(false);
      fetchCategories();
    } catch {
      // global handler
    }
  };

  const remove = async (id: string) => {
    try {
      const resp = await adminApi.deleteCategory(id);
      if (!resp.data.success) {
        message.error(resp.data.message || '删除类目失败');
        return;
      }
      message.success('类目已删除');
      fetchCategories();
    } catch {
      // global handler
    }
  };

  const columns = [
    {
      title: '类目名称',
      dataIndex: 'name',
      render: (value: string, record: ProductCategory) => (
        <Space>
          <Text strong={record.level === 1}>{value}</Text>
          <Text type="secondary">L{record.level}</Text>
        </Space>
      )
    },
    {
      title: '类目ID',
      dataIndex: 'id',
      width: 260,
      render: (value: string) => <Text code>{value}</Text>
    },
    {
      title: '排序',
      dataIndex: 'sort',
      width: 100
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (value: ProductCategoryStatus) => (
        <Tag color={statusColors[value]}>{statusLabels[value] || value}</Tag>
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
      width: 180,
      render: (_: unknown, record: ProductCategory) => (
        <Space>
          <Button type="link" onClick={() => openEdit(record)}>编辑</Button>
          <Popconfirm
            title="删除这个类目？"
            okText="删除"
            cancelText="取消"
            onConfirm={() => remove(record.id)}
          >
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  const parentOptions = flattenCategories(categories, editing?.id).map((category) => ({
    label: `${'  '.repeat(Math.max(0, category.level - 1))}${category.name}`,
    value: category.id
  }));

  return (
    <Card
      title="类目管理"
      extra={<Button type="primary" onClick={openCreate}>新建类目</Button>}
      className="card-shadow"
    >
      <Table
        rowKey="id"
        columns={columns}
        dataSource={categories}
        loading={loading}
        pagination={false}
        scroll={{ x: 960 }}
      />

      <Modal
        open={modalOpen}
        title={editing ? '编辑类目' : '新建类目'}
        okText={editing ? '保存' : '创建'}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={submit}>
          <Form.Item name="parentId" label="上级类目">
            <Select
              allowClear
              placeholder="不选则为一级类目"
              options={parentOptions}
            />
          </Form.Item>
          <Form.Item name="name" label="类目名称" rules={[{ required: true, message: '请输入类目名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="sort" label="排序">
            <InputNumber precision={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
            <Select
              options={[
                { label: '启用', value: 'ENABLED' },
                { label: '停用', value: 'DISABLED' }
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
