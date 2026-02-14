import { Descriptions, Typography } from 'antd';
import { session } from '../store/session';

const { Title } = Typography;

export default function Profile() {
  return (
    <div>
      <Title level={2}>个人中心</Title>
      <Descriptions bordered column={1}>
        <Descriptions.Item label="用户ID">{session.getBuyerId() || '未登录'}</Descriptions.Item>
        <Descriptions.Item label="租户ID">{session.getTenantId() || '未设置'}</Descriptions.Item>
        <Descriptions.Item label="Token">{session.getToken() ? '已获取' : '未获取'}</Descriptions.Item>
      </Descriptions>
    </div>
  );
}
