import {
  Button,
  Card,
  Descriptions,
  Form,
  Input,
  Modal,
  Radio,
  Space,
  Tag,
  Typography,
  message
} from 'antd';
import { useState } from 'react';
import {
  adminApi,
  AfterSales as AfterSalesRecord,
  AfterSalesStatus
} from '../services/api';
import { formatDateTime } from '../utils/format';

const statusColor: Record<AfterSalesStatus, string> = {
  APPLIED: 'blue',
  APPROVED: 'green',
  REJECTED: 'volcano',
  REFUNDED: 'purple',
  CANCELLED: 'default'
};

type ReviewFormValues = {
  approved: boolean;
  remark?: string;
};

type RefundFormValues = {
  remark?: string;
};

export default function AfterSales() {
  const [record, setRecord] = useState<AfterSalesRecord | null>(null);
  const [loading, setLoading] = useState(false);
  const [searchForm] = Form.useForm();
  const [reviewOpen, setReviewOpen] = useState(false);
  const [refundOpen, setRefundOpen] = useState(false);
  const [reviewForm] = Form.useForm<ReviewFormValues>();
  const [refundForm] = Form.useForm<RefundFormValues>();

  const fetchRecord = async (id: string) => {
    setLoading(true);
    try {
      const resp = await adminApi.getAfterSales(id);
      if (!resp.data.success) {
        message.error(resp.data.message || '获取售后信息失败');
        return;
      }
      setRecord(resp.data.data);
    } catch (error) {
      // global handler
    } finally {
      setLoading(false);
    }
  };

  const onSearch = (values: { afterSalesId?: string }) => {
    if (!values.afterSalesId) {
      message.warning('请输入售后单ID');
      return;
    }
    fetchRecord(values.afterSalesId.trim());
  };

  const handleReview = async (values: ReviewFormValues) => {
    if (!record) return;
    const resp = await adminApi.reviewAfterSales(record.id, values);
    if (!resp.data.success) {
      message.error(resp.data.message || '审核失败');
      return;
    }
    message.success('售后已审核');
    setReviewOpen(false);
    fetchRecord(record.id);
  };

  const handleRefund = async (values: RefundFormValues) => {
    if (!record) return;
    const resp = await adminApi.refundAfterSales(record.id, values);
    if (!resp.data.success) {
      message.error(resp.data.message || '退款失败');
      return;
    }
    message.success('已触发退款');
    setRefundOpen(false);
    fetchRecord(record.id);
  };

  return (
    <Card title="售后管理" className="card-shadow" loading={loading}>
      <Form
        form={searchForm}
        layout="inline"
        onFinish={onSearch}
        style={{ marginBottom: 16 }}
      >
        <Form.Item name="afterSalesId" label="售后单ID">
          <Input placeholder="请输入售后单ID" style={{ width: 320 }} />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">
              查询
            </Button>
            <Button
              onClick={() => {
                searchForm.resetFields();
                setRecord(null);
              }}
            >
              清空
            </Button>
          </Space>
        </Form.Item>
      </Form>

      {record ? (
        <>
          <Descriptions bordered column={2} size="middle" style={{ marginBottom: 16 }}>
            <Descriptions.Item label="售后单ID">
              <Typography.Text code>{record.id}</Typography.Text>
            </Descriptions.Item>
            <Descriptions.Item label="订单ID">
              <Typography.Text code>{record.orderId}</Typography.Text>
            </Descriptions.Item>
            <Descriptions.Item label="支付单ID">
              {record.paymentId ? (
                <Typography.Text code>{record.paymentId}</Typography.Text>
              ) : (
                '-'
              )}
            </Descriptions.Item>
            <Descriptions.Item label="售后类型">
              {record.type === 'RETURN_REFUND' ? '退货退款' : '仅退款'}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusColor[record.status]}>{record.status}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="申请原因">
              {record.reason || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="备注">{record.remark || '-'}</Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {formatDateTime(record.createdAt)}
            </Descriptions.Item>
            <Descriptions.Item label="更新时间">
              {formatDateTime(record.updatedAt)}
            </Descriptions.Item>
          </Descriptions>
          <Space>
            <Button
              type="primary"
              disabled={record.status !== 'APPLIED'}
              onClick={() => {
                reviewForm.setFieldsValue({ approved: true, remark: '' });
                setReviewOpen(true);
              }}
            >
              审核
            </Button>
            <Button
              danger
              disabled={record.status !== 'APPROVED'}
              onClick={() => {
                refundForm.setFieldsValue({ remark: '' });
                setRefundOpen(true);
              }}
            >
              退款
            </Button>
          </Space>
        </>
      ) : (
        <Typography.Text type="secondary">请输入售后单ID进行查询</Typography.Text>
      )}

      <Modal
        open={reviewOpen}
        title="售后审核"
        okText="确认审核"
        onCancel={() => setReviewOpen(false)}
        onOk={() => reviewForm.submit()}
      >
        <Form form={reviewForm} layout="vertical" onFinish={handleReview}>
          <Form.Item
            name="approved"
            label="审核结果"
            rules={[{ required: true, message: '请选择审核结果' }]}
          >
            <Radio.Group>
              <Radio value={true}>通过</Radio>
              <Radio value={false}>拒绝</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} placeholder="可选，填写审核说明" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={refundOpen}
        title="售后退款"
        okText="确认退款"
        onCancel={() => setRefundOpen(false)}
        onOk={() => refundForm.submit()}
      >
        <Form form={refundForm} layout="vertical" onFinish={handleRefund}>
          <Form.Item name="remark" label="退款备注">
            <Input.TextArea rows={3} placeholder="可选，填写退款说明" />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
