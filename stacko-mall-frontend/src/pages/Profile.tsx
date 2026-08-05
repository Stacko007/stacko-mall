import { Button, Descriptions, Space, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, MemberProfile } from '../services/api';
import { session } from '../store/session';

const { Title } = Typography;

export default function Profile() {
  const [member, setMember] = useState<MemberProfile | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        const resp = await api.currentMember();
        if (resp.data.success) {
          setMember(resp.data.data);
        } else {
          message.error(resp.data.message || '获取会员信息失败');
        }
      } catch {
        // global handler will notify
      }
    };
    load();
  }, []);

  return (
    <div>
      <Title level={2}>个人中心</Title>
      <Descriptions bordered column={1}>
        <Descriptions.Item label="用户中心账号ID">
          {member?.accountId || session.getBuyerId() || '未登录'}
        </Descriptions.Item>
        <Descriptions.Item label="用户中心成员ID">
          {member?.membershipId || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="商城买家ID">
          {member?.id || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="租户ID">{session.getTenantId() || '未设置'}</Descriptions.Item>
        <Descriptions.Item label="Token">{session.getToken() ? '已获取' : '未获取'}</Descriptions.Item>
      </Descriptions>
      <Typography.Paragraph type="secondary" style={{ marginTop: 12 }}>
        后台订单管理按“商城买家ID”过滤。
      </Typography.Paragraph>
      <Space style={{ marginTop: 16 }}>
        <Button type="primary">
          <Link to="/addresses">收货地址</Link>
        </Button>
      </Space>
    </div>
  );
}
