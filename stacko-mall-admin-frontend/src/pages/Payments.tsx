import {
  Button,
  Card,
  Descriptions,
  Form,
  Input,
  Space,
  Tag,
  Typography,
  message
} from 'antd';
import { useState } from 'react';
import { adminApi, Payment, PaymentStatus } from '../services/api';
import { formatDateTime } from '../utils/format';

const statusColor: Record<PaymentStatus, string> = {
  CREATED: 'default',
  PAID: 'green',
  FAILED: 'volcano',
  REFUNDED: 'purple'
};

export default function Payments() {
  const [record, setRecord] = useState<Payment | null>(null);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const onSearch = async (values: { paymentId?: string }) => {
    if (!values.paymentId) {
      message.warning('请输入支付单ID');
      return;
    }
    setLoading(true);
    try {
      const resp = await adminApi.getPayment(values.paymentId.trim());
      if (!resp.data.success) {
        message.error(resp.data.message || '获取支付信息失败');
        return;
      }
      setRecord(resp.data.data);
    } catch (error) {
      // global handler
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card title="支付查询" className="card-shadow" loading={loading}>
      <Form
        form={form}
        layout="inline"
        onFinish={onSearch}
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="paymentId" label="支付单ID">
          <Input placeholder="请输入支付单ID" style={{ width: 320 }} />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">
              查询
            </Button>
            <Button
              onClick={() => {
                form.resetFields();
                setRecord(null);
              }}
            >
              清空
            </Button>
          </Space>
        </Form.Item>
      </Form>

      {record ? (
        <Descriptions bordered column={2} size="middle">
          <Descriptions.Item label="支付单ID">
            <Typography.Text code>{record.id}</Typography.Text>
          </Descriptions.Item>
          <Descriptions.Item label="订单ID">
            <Typography.Text code>{record.orderId}</Typography.Text>
          </Descriptions.Item>
          <Descriptions.Item label="支付状态">
            <Tag color={statusColor[record.status]}>{record.status}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="支付渠道">{record.channel}</Descriptions.Item>
          <Descriptions.Item label="支付金额">
            ¥{Number(record.amount).toFixed(2)}
          </Descriptions.Item>
          <Descriptions.Item label="交易单号">
            {record.tradeNo || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="回调原文" span={2}>
            <Typography.Paragraph ellipsis={{ rows: 3, tooltip: record.rawCallback }}>
              {record.rawCallback || '-'}
            </Typography.Paragraph>
          </Descriptions.Item>
          <Descriptions.Item label="创建时间">
            {formatDateTime(record.createdAt)}
          </Descriptions.Item>
          <Descriptions.Item label="更新时间">
            {formatDateTime(record.updatedAt)}
          </Descriptions.Item>
        </Descriptions>
      ) : (
        <Typography.Text type="secondary">请输入支付单ID进行查询</Typography.Text>
      )}
    </Card>
  );
}
