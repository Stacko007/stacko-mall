import { Button, Card, Descriptions, Skeleton, Space, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, Product, Stock } from '../services/api';
import { cartStore } from '../store/cart';
import { ErrorState } from '../components/State';
import { getErrorMessage } from '../utils/error';
import { productStatusLabels } from '../utils/status';
import { ProductCategoryLink } from '../components/ProductCategoryLink';
import { useProductCategories } from '../hooks/useProductCategories';

const { Title, Paragraph } = Typography;

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState<Product | null>(null);
  const [stock, setStock] = useState<Stock | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { categoryMap } = useProductCategories();

  useEffect(() => {
    if (!id) return;
    const load = async () => {
      try {
        const resp = await api.getProduct(id);
        if (resp.data.success) {
          setProduct(resp.data.data);
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
    const loadStock = async () => {
      try {
        const resp = await api.getStock(id);
        if (resp.data.success) {
          setStock(resp.data.data);
        }
      } catch {
        // 库存接口失败不阻塞页面
      }
    };
    load();
    loadStock();
  }, [id]);

  if (loading) return <Skeleton active />;
  if (error) return <ErrorState description={error} />;
  if (!product) return <div>商品不存在</div>;
  const available = product.status === 'ACTIVE';

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Link to="/products">返回列表</Link>
      </Space>
      <Title level={2}>{product.name}</Title>
      <Paragraph>{product.description || '暂无描述'}</Paragraph>
      <Card>
        <Descriptions bordered column={1}>
          <Descriptions.Item label="价格">¥ {product.price}</Descriptions.Item>
          <Descriptions.Item label="类目">
            <ProductCategoryLink categoryId={product.categoryId} categoryMap={categoryMap} />
          </Descriptions.Item>
          <Descriptions.Item label="库存">
            {stock ? stock.quantity : '暂无'}
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            {product.status ? productStatusLabels[product.status] || product.status : '-'}
          </Descriptions.Item>
        </Descriptions>
        <Space style={{ marginTop: 16 }}>
          <Button
            type="primary"
            disabled={!available}
            onClick={() => navigate(`/confirm?productId=${product.id}`)}
          >
            立即购买
          </Button>
          <Button
            disabled={!available}
            onClick={() => {
              cartStore.add(product, 1);
              message.success('已加入购物车');
              navigate('/cart');
            }}
          >
            加入购物车
          </Button>
        </Space>
      </Card>
    </div>
  );
}
