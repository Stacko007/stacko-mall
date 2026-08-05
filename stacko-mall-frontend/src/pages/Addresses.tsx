import {
  Button,
  Card,
  Form,
  Input,
  List,
  Modal,
  Popconfirm,
  Space,
  Switch,
  Tag,
  Typography,
  message
} from 'antd';
import { useEffect, useState } from 'react';
import { api, ShippingAddress, ShippingAddressPayload } from '../services/api';
import { EmptyState, ErrorState } from '../components/State';
import { getErrorMessage } from '../utils/error';

const { Title, Text } = Typography;

export default function Addresses() {
  const [addresses, setAddresses] = useState<ShippingAddress[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<ShippingAddress | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [form] = Form.useForm<ShippingAddressPayload>();

  const load = async () => {
    setLoading(true);
    try {
      const resp = await api.listAddresses();
      if (resp.data.success) {
        setAddresses(resp.data.data || []);
        setError(null);
      } else {
        const msg = resp.data.message || '地址加载失败';
        setError(msg);
        message.error(msg);
      }
    } catch (error) {
      setError(getErrorMessage(error, '地址加载失败'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ defaultAddress: addresses.length === 0 });
    setModalOpen(true);
  };

  const openEdit = (address: ShippingAddress) => {
    setEditing(address);
    form.setFieldsValue({
      receiverName: address.receiverName,
      receiverPhone: address.receiverPhone,
      province: address.province,
      city: address.city,
      district: address.district || '',
      detailAddress: address.detailAddress,
      defaultAddress: address.defaultAddress
    });
    setModalOpen(true);
  };

  const submit = async (values: ShippingAddressPayload) => {
    try {
      const resp = editing
        ? await api.updateAddress(editing.id, values)
        : await api.createAddress(values);
      if (!resp.data.success) {
        message.error(resp.data.message || '保存地址失败');
        return;
      }
      message.success('地址已保存');
      setModalOpen(false);
      load();
    } catch (error) {
      // global handler will notify
    }
  };

  const setDefault = async (id: string) => {
    try {
      const resp = await api.setDefaultAddress(id);
      if (resp.data.success) {
        message.success('默认地址已更新');
        load();
      } else {
        message.error(resp.data.message || '设置默认地址失败');
      }
    } catch (error) {
      // global handler will notify
    }
  };

  const remove = async (id: string) => {
    try {
      const resp = await api.deleteAddress(id);
      if (resp.data.success) {
        message.success('地址已删除');
        load();
      } else {
        message.error(resp.data.message || '删除地址失败');
      }
    } catch (error) {
      // global handler will notify
    }
  };

  return (
    <div>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={2} style={{ marginBottom: 0 }}>收货地址</Title>
        <Button type="primary" onClick={openCreate}>新增地址</Button>
      </Space>
      {error ? (
        <ErrorState description={error} onRetry={load} />
      ) : addresses.length === 0 ? (
        <EmptyState title="暂无收货地址" description="新增地址后即可下单配送" />
      ) : (
        <List
          loading={loading}
          dataSource={addresses}
          renderItem={(address) => (
            <List.Item>
              <Card style={{ width: '100%' }}>
                <Space direction="vertical" style={{ width: '100%' }}>
                  <Space wrap>
                    <Text strong>{address.receiverName}</Text>
                    <Text>{address.receiverPhone}</Text>
                    {address.defaultAddress ? <Tag color="green">默认</Tag> : null}
                  </Space>
                  <Text type="secondary">
                    {[address.province, address.city, address.district, address.detailAddress]
                      .filter(Boolean)
                      .join(' ')}
                  </Text>
                  <Space>
                    {!address.defaultAddress ? (
                      <Button onClick={() => setDefault(address.id)}>设为默认</Button>
                    ) : null}
                    <Button onClick={() => openEdit(address)}>编辑</Button>
                    <Popconfirm
                      title="删除这个地址？"
                      okText="删除"
                      cancelText="取消"
                      onConfirm={() => remove(address.id)}
                    >
                      <Button danger>删除</Button>
                    </Popconfirm>
                  </Space>
                </Space>
              </Card>
            </List.Item>
          )}
        />
      )}

      <Modal
        open={modalOpen}
        title={editing ? '编辑地址' : '新增地址'}
        okText="保存"
        onOk={() => form.submit()}
        onCancel={() => setModalOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={submit}>
          <Form.Item name="receiverName" label="收货人" rules={[{ required: true, message: '请输入收货人' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="receiverPhone" label="手机号" rules={[{ required: true, message: '请输入手机号' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="province" label="省份" rules={[{ required: true, message: '请输入省份' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="city" label="城市" rules={[{ required: true, message: '请输入城市' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="district" label="区县">
            <Input />
          </Form.Item>
          <Form.Item name="detailAddress" label="详细地址" rules={[{ required: true, message: '请输入详细地址' }]}>
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="defaultAddress" label="默认地址" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
