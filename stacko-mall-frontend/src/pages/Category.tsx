import { List, Skeleton, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, Product } from '../services/api';
import { EmptyState, ErrorState } from '../components/State';
import { getErrorMessage } from '../utils/error';

const { Title } = Typography;

export default function Category() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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
      <Title level={2}>分类</Title>
      {loading ? (
        <Skeleton active />
      ) : error ? (
        <ErrorState description={error} onRetry={load} />
      ) : products.length === 0 ? (
        <EmptyState title="暂无商品" />
      ) : (
        <List
          bordered
          dataSource={products}
          renderItem={(item) => (
            <List.Item>
              {item.name} - ¥ {item.price} <Link to={`/products/${item.id}`}>详情</Link>
            </List.Item>
          )}
        />
      )}
    </div>
  );
}
