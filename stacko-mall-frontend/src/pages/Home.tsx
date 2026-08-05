import { Button, Card, Col, Row, Skeleton, Space, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, Product } from '../services/api';
import { cartStore } from '../store/cart';
import { EmptyState, ErrorState } from '../components/State';
import { getErrorMessage } from '../utils/error';
import { ProductCategoryLink } from '../components/ProductCategoryLink';
import { useProductCategories } from '../hooks/useProductCategories';

const { Title, Paragraph } = Typography;

export default function Home() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { categoryMap } = useProductCategories();

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const resp = await api.getProducts();
      if (resp.data.success) {
        setProducts(resp.data.data || []);
      } else {
        const msg = resp.data.message || '商品加载失败';
        setError(msg);
        message.error(msg);
      }
    } catch (error) {
      const msg = getErrorMessage(error, '商品加载失败');
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <div>
      <Title level={2}>首页</Title>
      <Paragraph>这里展示 Banner、分类入口、推荐商品等内容。</Paragraph>
      {loading ? (
        <Row gutter={[16, 16]}>
          {[1, 2, 3, 4].map((i) => (
            <Col key={i} xs={24} sm={12} md={8} lg={6}>
              <Card>
                <Skeleton active />
              </Card>
            </Col>
          ))}
        </Row>
      ) : error ? (
        <ErrorState description={error} onRetry={load} />
      ) : products.length === 0 ? (
        <EmptyState title="暂无推荐商品" />
      ) : (
        <Row gutter={[16, 16]}>
          {products.map((product) => (
            <Col key={product.id} xs={24} sm={12} md={8} lg={6}>
              <Card
                title={product.name}
                hoverable
                extra={<Link to={`/products/${product.id}`}>详情</Link>}
              >
                <div>价格：¥ {product.price}</div>
                <div style={{ marginTop: 8 }}>
                  类目：<ProductCategoryLink categoryId={product.categoryId} categoryMap={categoryMap} />
                </div>
                <div style={{ marginTop: 8, color: '#6b7280' }}>
                  {product.description || '暂无描述'}
                </div>
                <Space style={{ marginTop: 12 }}>
                  <Button
                    type="primary"
                    onClick={() => {
                      cartStore.add(product, 1);
                      message.success('已加入购物车');
                    }}
                  >
                    加入购物车
                  </Button>
                </Space>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </div>
  );
}
