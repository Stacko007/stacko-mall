import { Card, Col, Row, Typography } from 'antd';
import { Link } from 'react-router-dom';

const { Title, Paragraph } = Typography;

const quickLinks = [
  {
    title: '商品管理',
    desc: '创建与维护商品信息，调整上架状态。',
    to: '/admin/products'
  },
  {
    title: '订单管理',
    desc: '查询订单、发货、关闭订单。',
    to: '/admin/orders'
  },
  {
    title: '库存管理',
    desc: '设置或调整商品库存。',
    to: '/admin/stocks'
  },
  {
    title: '售后管理',
    desc: '审核售后申请并执行退款。',
    to: '/admin/after-sales'
  },
  {
    title: '支付查询',
    desc: '按支付单号查询支付信息。',
    to: '/admin/payments'
  }
];

export default function Dashboard() {
  return (
    <div>
      <Title level={2}>Stacko Mall 管理控制台</Title>
      <Paragraph type="secondary">
        通过左侧导航进入具体模块，快速完成商品、订单、库存与售后管理。
      </Paragraph>
      <Row gutter={[16, 16]}>
        {quickLinks.map((item) => (
          <Col xs={24} md={12} lg={8} key={item.title}>
            <Card title={item.title} bordered={false} className="card-shadow">
              <Paragraph>{item.desc}</Paragraph>
              <Link to={item.to}>进入模块</Link>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}
