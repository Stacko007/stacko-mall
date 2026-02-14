import { Button, Card, Form, Input, Typography, message } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';
import { adminApi, LoginRequest } from '../services/api';
import { session } from '../store/session';

const { Title, Paragraph } = Typography;

export default function Login() {
  const [form] = Form.useForm<LoginRequest>();
  const navigate = useNavigate();
  const location = useLocation();

  const onFinish = async (values: LoginRequest) => {
    try {
      const resp = await adminApi.login(values);
      if (!resp.data.success) {
        message.error(resp.data.message || '登录失败');
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
      }
      message.success('登录成功');
      const params = new URLSearchParams(location.search);
      const redirect = params.get('redirect');
      if (redirect) {
        navigate(decodeURIComponent(redirect));
      } else {
        navigate('/admin');
      }
    } catch (error) {
      // global handler
    }
  };

  return (
    <div className="login-wrap">
      <Card className="login-card">
        <Title level={2} className="login-title">
          Stacko Mall 管理登录
        </Title>
        <Paragraph type="secondary">
          使用管理账号登录后进入后台控制台。
        </Paragraph>
        <Form
          layout="vertical"
          form={form}
          onFinish={onFinish}
          initialValues={{ withRefresh: true, tenantId: 'stacko-mall' }}
        >
          <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
            <Input placeholder="请输入管理员账号" />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true }]}>
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
          <Form.Item name="tenantId" label="租户ID" rules={[{ required: true }]}>
            <Input placeholder="例如：stacko-mall" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
