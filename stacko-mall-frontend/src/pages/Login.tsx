import { Button, Card, Form, Input, message, Typography } from 'antd';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { api, LoginRequest } from '../services/api';
import { session } from '../store/session';

const { Title } = Typography;

export default function Login() {
  const [form] = Form.useForm<LoginRequest>();
  const navigate = useNavigate();
  const location = useLocation();

  const onFinish = async (values: LoginRequest) => {
    try {
      const resp = await api.login({ ...values, portalCode: 'stacko-mall-web' });
      if (!resp.data.success) {
        message.error(resp.data.message || '登录失败');
        return;
      }
      const token = resp.data.data?.token;
      if (token) {
        session.setToken(token);
      }
      if (resp.data.data?.refreshToken) {
        session.setRefreshToken(resp.data.data.refreshToken);
      }
      if (resp.data.data?.userId) {
        session.setBuyerId(String(resp.data.data.userId));
      }
      if (resp.data.data?.tenantId) {
        session.setTenantId(resp.data.data.tenantId);
      }
      if (
        resp.data.data?.portalCode !== 'stacko-mall-web' ||
        resp.data.data?.audience !== 'stacko-mall-web'
      ) {
        session.clearAll();
        message.error('当前账号无权访问商城用户端');
        return;
      }
      const current = await api.currentSession();
      if (
        !current.data.success ||
        current.data.data?.portalCode !== 'stacko-mall-web' ||
        current.data.data?.audience !== 'stacko-mall-web'
      ) {
        session.clearAll();
        message.error('当前账号无权访问商城用户端');
        return;
      }
      session.setProfile(current.data.data);
      message.success('登录成功');
      const params = new URLSearchParams(location.search);
      const redirect = params.get('redirect');
      if (redirect) {
        navigate(decodeURIComponent(redirect));
      } else {
        navigate('/');
      }
    } catch (error) {
      // global handler will notify
    }
  };

  return (
    <div style={{ maxWidth: 420, margin: '40px auto' }}>
      <Title level={2}>登录</Title>
      <Card>
        <Form
          layout="vertical"
          form={form}
          onFinish={onFinish}
          initialValues={{ withRefresh: true }}
        >
          <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item name="tenantId" label="租户ID" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              登录
            </Button>
          </Form.Item>
          <Form.Item>
            <Link to="/register">没有账号？去注册</Link>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
