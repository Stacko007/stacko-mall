import { Card, Col, List, Row, Skeleton, Space, Tree, Typography, message } from 'antd';
import type { Key } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api, Product, ProductCategory } from '../services/api';
import { EmptyState, ErrorState } from '../components/State';
import { getErrorMessage } from '../utils/error';
import { flattenCategories } from '../utils/category';
import { ProductCategoryLink } from '../components/ProductCategoryLink';

const { Title } = Typography;

export default function Category() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [categories, setCategories] = useState<ProductCategory[]>([]);
  const [categoryId, setCategoryId] = useState<string | undefined>(
    searchParams.get('categoryId') || undefined
  );
  const [expandedKeys, setExpandedKeys] = useState<Key[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [categoryLoading, setCategoryLoading] = useState(true);
  const [productLoading, setProductLoading] = useState(true);
  const [categoryError, setCategoryError] = useState<string | null>(null);
  const [productError, setProductError] = useState<string | null>(null);
  const categoryMap = useMemo(() => flattenCategories(categories), [categories]);

  const selectCategory = (nextCategoryId?: string) => {
    setCategoryId(nextCategoryId);
    if (nextCategoryId) {
      setSearchParams({ categoryId: nextCategoryId });
    } else {
      setSearchParams({});
    }
  };

  const loadProducts = async (nextCategoryId?: string) => {
    setProductLoading(true);
    setProductError(null);
    try {
      const resp = await api.getProducts(nextCategoryId);
      if (resp.data.success) {
        setProducts(resp.data.data || []);
      } else {
        const msg = resp.data.message || '商品加载失败';
        setProductError(msg);
        message.error(msg);
      }
    } catch (error) {
      const msg = getErrorMessage(error, '商品加载失败');
      setProductError(msg);
    } finally {
      setProductLoading(false);
    }
  };

  const loadCategories = async () => {
    setCategoryLoading(true);
    setCategoryError(null);
    try {
      const resp = await api.getCategories();
      if (resp.data.success) {
        setCategories(resp.data.data || []);
      } else {
        const msg = resp.data.message || '类目加载失败';
        setCategoryError(msg);
        message.error(msg);
      }
    } catch (error) {
      const msg = getErrorMessage(error, '类目加载失败');
      setCategoryError(msg);
    } finally {
      setCategoryLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    const nextCategoryId = searchParams.get('categoryId') || undefined;
    setCategoryId(nextCategoryId);
    loadProducts(nextCategoryId);
  }, [searchParams]);

  useEffect(() => {
    if (!categoryId) return;
    const category = categoryMap[categoryId];
    if (!category?.path) return;
    const parentKeys = category.path
      .split('/')
      .filter((key) => key && key !== categoryId);
    setExpandedKeys((current) => Array.from(new Set([...current, ...parentKeys])));
  }, [categoryId, categoryMap]);

  const toTreeData = (items: ProductCategory[]): Array<{ key: string; title: string; children?: Array<{ key: string; title: string }> }> =>
    items.map((item) => ({
      key: item.id,
      title: item.name,
      children: item.children && item.children.length > 0 ? toTreeData(item.children) : undefined
    }));

  return (
    <div>
      <Title level={2}>分类</Title>
      <Row gutter={16}>
        <Col xs={24} md={6}>
          <Card title="商品类目">
            {categoryLoading ? (
              <Skeleton active paragraph={{ rows: 4 }} />
            ) : categoryError ? (
              <ErrorState description={categoryError} onRetry={loadCategories} />
            ) : categories.length === 0 ? (
              <EmptyState title="暂无类目" />
            ) : (
              <Tree
                treeData={toTreeData(categories)}
                selectedKeys={categoryId ? [categoryId] : []}
                expandedKeys={expandedKeys}
                onExpand={(keys) => setExpandedKeys(keys)}
                onSelect={(keys) => {
                  const selected = String(keys[0] || '');
                  selectCategory(selected || undefined);
                }}
              />
            )}
          </Card>
        </Col>
        <Col xs={24} md={18}>
          <Card
            title="商品"
            extra={categoryId ? <a onClick={() => selectCategory()}>全部商品</a> : null}
          >
            {productLoading ? (
              <Skeleton active paragraph={{ rows: 4 }} />
            ) : productError ? (
              <ErrorState description={productError} onRetry={() => loadProducts(categoryId)} />
            ) : products.length === 0 ? (
              <EmptyState title="暂无商品" />
            ) : (
              <List
                dataSource={products}
                renderItem={(item) => (
                  <List.Item>
                    <Space>
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
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
}
