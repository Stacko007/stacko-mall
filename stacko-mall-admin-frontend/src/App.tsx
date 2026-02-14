import { Button, Layout, Menu, Space, Tabs, Tag, Typography } from 'antd';
import {
  Link,
  Navigate,
  Route,
  Routes,
  matchPath,
  useLocation,
  useNavigate
} from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Orders from './pages/Orders';
import OrderDetail from './pages/OrderDetail';
import Stocks from './pages/Stocks';
import AfterSales from './pages/AfterSales';
import Payments from './pages/Payments';
import Login from './pages/Login';
import { session } from './store/session';
import './App.css';

const { Header, Content, Sider } = Layout;
const { Text } = Typography;

const navItems = [
  { key: '/admin', label: '仪表盘' },
  { key: '/admin/products', label: '商品管理' },
  { key: '/admin/orders', label: '订单管理' },
  { key: '/admin/stocks', label: '库存管理' },
  { key: '/admin/after-sales', label: '售后管理' },
  { key: '/admin/payments', label: '支付查询' }
];

type TabItem = {
  key: string;
  label: string;
  closable?: boolean;
};

function RequireAuth({ children }: { children: React.ReactNode }) {
  const token = session.getToken();
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

export default function App() {
  const location = useLocation();
  const navigate = useNavigate();
  const pathname = location.pathname;
  const isLogin = pathname.startsWith('/login');

  const selectedKey = (() => {
    if (pathname === '/admin') return '/admin';
    if (pathname.startsWith('/admin/products')) return '/admin/products';
    if (pathname.startsWith('/admin/orders')) return '/admin/orders';
    if (pathname.startsWith('/admin/stocks')) return '/admin/stocks';
    if (pathname.startsWith('/admin/after-sales')) return '/admin/after-sales';
    if (pathname.startsWith('/admin/payments')) return '/admin/payments';
    return '/admin';
  })();

  const getTabLabel = (path: string) => {
    if (matchPath('/admin/orders/:id', path)) return '订单详情';
    const hit = navItems.find((item) => item.key === path);
    return hit?.label || '页面';
  };

  const [tabs, setTabs] = useState<TabItem[]>(() => {
    const stored = sessionStorage.getItem('admin_tabs');
    if (stored) {
      try {
        const parsed = JSON.parse(stored) as TabItem[];
        if (Array.isArray(parsed) && parsed.length) {
          const normalized = parsed.map((item) =>
            item.key === '/admin' ? { ...item, closable: false } : item
          );
          if (!isLogin && !normalized.some((item) => item.key === pathname)) {
            return [
              ...normalized,
              {
                key: pathname,
                label: getTabLabel(pathname),
                closable: pathname !== '/admin'
              }
            ];
          }
          return normalized;
        }
      } catch {
        // ignore
      }
    }
    return [{ key: '/admin', label: '仪表盘', closable: false }];
  });

  useEffect(() => {
    sessionStorage.setItem('admin_tabs', JSON.stringify(tabs));
  }, [tabs]);

  useEffect(() => {
    if (isLogin) return;
    setTabs((prev) => {
      if (prev.some((item) => item.key === pathname)) return prev;
      const label = getTabLabel(pathname);
      const closable = pathname !== '/admin';
      return [...prev, { key: pathname, label, closable }];
    });
  }, [isLogin, pathname]);

  const tabItems = useMemo(
    () =>
      tabs.map((tab) => ({
        key: tab.key,
        label: tab.label,
        closable: tab.closable !== false
      })),
    [tabs]
  );

  const onTabChange = (key: string) => {
    navigate(key);
  };

  const onTabEdit = (targetKey: string | unknown, action: string) => {
    if (action !== 'remove' || typeof targetKey !== 'string') return;
    setTabs((prev) => {
      const idx = prev.findIndex((item) => item.key === targetKey);
      const nextTabs = prev.filter((item) => item.key !== targetKey);
      if (pathname === targetKey && nextTabs.length) {
        const fallback = nextTabs[Math.max(0, idx - 1)] || nextTabs[0];
        navigate(fallback.key);
      }
      return nextTabs.length
        ? nextTabs
        : [{ key: '/admin', label: '仪表盘', closable: false }];
    });
  };

  const activeTabKey =
    tabs.find((item) => item.key === pathname)?.key ||
    tabs[0]?.key ||
    '/admin';

  if (isLogin) {
    return (
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <RequireAuth>
      <Layout className="app-layout">
        <Sider
          breakpoint="lg"
          collapsedWidth={0}
          className="app-sider"
          width={220}
        >
          <div className="app-brand">Stacko Mall</div>
          <Menu
            theme="dark"
            mode="inline"
            selectedKeys={[selectedKey]}
            items={navItems.map((item) => ({
              key: item.key,
              label: <Link to={item.key}>{item.label}</Link>
            }))}
          />
        </Sider>
        <Layout>
          <Header className="app-header">
            <div className="header-title">
              <Text strong>管理控制台</Text>
              <Tag color="blue" className="header-tag">
                {session.getTenantId() || 'stacko-mall'}
              </Tag>
            </div>
            <Space>
              <Button
                type="primary"
                onClick={() => {
                  session.clearAll();
                  window.location.href = '/login';
                }}
              >
                退出登录
              </Button>
            </Space>
          </Header>
          <Content className="app-content">
            <Tabs
              type="editable-card"
              hideAdd
              activeKey={activeTabKey}
              items={tabItems}
              onChange={onTabChange}
              onEdit={onTabEdit}
              className="admin-tabs"
            />
            <Routes>
              <Route path="/" element={<Navigate to="/admin" replace />} />
              <Route path="/admin" element={<Dashboard />} />
              <Route path="/admin/products" element={<Products />} />
              <Route path="/admin/orders" element={<Orders />} />
              <Route path="/admin/orders/:id" element={<OrderDetail />} />
              <Route path="/admin/stocks" element={<Stocks />} />
              <Route path="/admin/after-sales" element={<AfterSales />} />
              <Route path="/admin/payments" element={<Payments />} />
              <Route path="*" element={<Navigate to="/admin" replace />} />
            </Routes>
          </Content>
        </Layout>
      </Layout>
    </RequireAuth>
  );
}
