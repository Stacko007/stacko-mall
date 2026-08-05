import { Button, Input, List, Space, Typography, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, Product } from '../services/api';
import { ProductCategoryLink } from '../components/ProductCategoryLink';
import { useProductCategories } from '../hooks/useProductCategories';

const { Title } = Typography;
const HISTORY_KEY = 'stacko_search_history';

function readHistory(): string[] {
  try {
    const raw = localStorage.getItem(HISTORY_KEY);
    return raw ? (JSON.parse(raw) as string[]) : [];
  } catch {
    return [];
  }
}

function writeHistory(list: string[]) {
  localStorage.setItem(HISTORY_KEY, JSON.stringify(list.slice(0, 10)));
}

export default function Search() {
  const [keyword, setKeyword] = useState('');
  const [products, setProducts] = useState<Product[]>([]);
  const [history, setHistory] = useState<string[]>([]);
  const { categoryMap } = useProductCategories();

  useEffect(() => {
    setHistory(readHistory());
    const load = async () => {
      try {
        const resp = await api.getProducts();
        if (resp.data.success) {
          setProducts(resp.data.data || []);
        } else {
          message.error(resp.data.message || '商品加载失败');
        }
      } catch (error) {
        message.error('商品加载失败');
      }
    };
    load();
  }, []);

  const result = useMemo(() => {
    if (!keyword.trim()) return [];
    return products.filter((item) =>
      item.name.toLowerCase().includes(keyword.trim().toLowerCase())
    );
  }, [keyword, products]);

  const handleSearch = () => {
    if (!keyword.trim()) return;
    const next = [keyword.trim(), ...history.filter((h) => h !== keyword.trim())];
    writeHistory(next);
    setHistory(next);
  };

  return (
    <div>
      <Title level={2}>搜索</Title>
      <Space wrap style={{ width: '100%' }}>
        <Input
          placeholder="输入关键词"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onPressEnter={handleSearch}
          style={{ minWidth: 220, flex: 1 }}
        />
        <Button type="primary" onClick={handleSearch}>
          搜索
        </Button>
      </Space>

      <Title level={4} style={{ marginTop: 16 }}>历史记录</Title>
      <Space wrap>
        {history.map((item) => (
          <Button key={item} onClick={() => setKeyword(item)}>
            {item}
          </Button>
        ))}
      </Space>

      <Title level={4} style={{ marginTop: 16 }}>搜索结果</Title>
      <List
        bordered
        dataSource={result}
        renderItem={(item) => (
          <List.Item>
            <Space wrap>
              <span>{item.name}</span>
              <span>¥ {item.price}</span>
              <span>
                类目：<ProductCategoryLink categoryId={item.categoryId} categoryMap={categoryMap} />
              </span>
              <Link to={`/products/${item.id}`}>详情</Link>
            </Space>
          </List.Item>
        )}
      />
    </div>
  );
}
