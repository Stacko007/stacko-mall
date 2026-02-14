import { Button, Card, Form, Input, message, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { api, RegisterRequest } from '../services/api';
import { session } from '../store/session';

const { Title } = Typography;

export default function Register() {
  const [form] = Form.useForm<RegisterRequest>();
  const navigate = useNavigate();

  const onFinish = async (values: RegisterRequest) => {
    try {
      const resp = await api.register(values);
      if (!resp.data.success) {
        message.error(resp.data.message || '注册失败');
        return;
      }
      const token = resp.data.data?.token;
      if (token) {
        session.setToken(token);
      }
      if (resp.data.data?.userId) {
        session.setBuyerId(String(resp.data.data.userId));
      }
      if (resp.data.data?.tenantId) {
        session.setTenantId(resp.data.data.tenantId);
      } else if (values.tenantId) {
        session.setTenantId(values.tenantId);
      }
      message.success('注册成功');
      navigate('/');
    } catch (error) {
      // global handler will notify
    }
  };

  return (
    <div style={{ maxWidth: 460, margin: '40px auto' }}>
      <Title level={2}>注册</Title>
      <Card>
        <Form
          layout="vertical"
          form={form}
          onFinish={onFinish}
          initialValues={{ tenantId: 'stacko-mall' }}
        >
          <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item
            name="password"
            label="密码"
            rules={[{ required: true, min: 6, message: '密码至少6位' }]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item name="tenantId" label="租户ID" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="手机号">
            <Input />
          </Form.Item>
          <Form.Item
            name="email"
            label="邮箱"
            rules={[{ type: 'email', message: '邮箱格式不正确' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              注册并登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
