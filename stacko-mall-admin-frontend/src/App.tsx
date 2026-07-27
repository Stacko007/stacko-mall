import { Button, Layout, Menu, Space, Spin, Tabs, Tag, Typography } from 'antd';
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
import { adminApi, UserSession } from './services/api';
import { session } from './store/session';
import './App.css';

const { Header, Content, Sider } = Layout;
const { Text } = Typography;

const navItems = [
  { key: '/admin', label: '仪表盘' },
  { key: '/admin/products', label: '商品管理', permission: 'mall:product:list' },
  { key: '/admin/orders', label: '订单管理', permission: 'mall:order:list' },
  { key: '/admin/stocks', label: '库存管理', permission: 'mall:stock:list' },
  { key: '/admin/after-sales', label: '售后管理', permission: 'mall:afterSales:read' },
  { key: '/admin/payments', label: '支付查询', permission: 'mall:payment:read' }
];

type TabItem = {
  key: string;
  label: string;
  closable?: boolean;
};

const permissionForPath = (path: string) => {
  if (matchPath('/admin/orders/:id', path)) return 'mall:order:read';
  return navItems.find((item) => item.key === path)?.permission;
};

export default function App() {
  const location = useLocation();
  const navigate = useNavigate();
  const pathname = location.pathname;
  const isLogin = pathname.startsWith('/login');
  const [portalSession, setPortalSession] = useState<UserSession | null>(
    () => session.getProfile<UserSession>()
  );
  const [checkingSession, setCheckingSession] = useState(!isLogin && Boolean(session.getToken()));

  useEffect(() => {
    if (isLogin || !session.getToken()) {
      setCheckingSession(false);
      return;
    }
    setCheckingSession(true);
    adminApi.currentSession()
      .then((response) => {
        const current = response.data.data;
        if (
          !response.data.success ||
          current?.portalCode !== 'stacko-mall-admin' ||
          current?.audience !== 'stacko-mall-admin'
        ) {
          throw new Error('Portal access denied');
        }
        session.setProfile(current);
        setPortalSession(current);
      })
      .catch(() => {
        session.clearAll();
        setPortalSession(null);
      })
      .finally(() => setCheckingSession(false));
  }, [isLogin]);

  const allowedNavItems = useMemo(
    () => navItems.filter((item) =>
      !item.permission || portalSession?.permissions?.includes(item.permission)
    ),
    [portalSession]
  );

  const allowed = (permission?: string) =>
    !permission || Boolean(portalSession?.permissions?.includes(permission));

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
    const hit = allowedNavItems.find((item) => item.key === path);
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

  const visibleTabs = useMemo(
    () => tabs.filter((tab) => allowed(permissionForPath(tab.key))),
    [tabs, portalSession]
  );

  const tabItems = useMemo(
    () =>
      visibleTabs.map((tab) => ({
        key: tab.key,
        label: tab.label,
        closable: tab.closable !== false
      })),
    [visibleTabs]
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
    visibleTabs.find((item) => item.key === pathname)?.key ||
    visibleTabs[0]?.key ||
    '/admin';

  if (isLogin) {
    return (
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  if (!session.getToken()) {
    return <Navigate to={`/login?redirect=${encodeURIComponent(pathname)}`} replace />;
  }

  if (checkingSession) {
    return <Spin fullscreen />;
  }

  if (!portalSession) {
    return <Navigate to="/login?denied=1" replace />;
  }

  return (
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
            items={allowedNavItems.map((item) => ({
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
              <Route path="/admin/products" element={allowed('mall:product:list') ? <Products /> : <Navigate to="/admin" replace />} />
              <Route path="/admin/orders" element={allowed('mall:order:list') ? <Orders /> : <Navigate to="/admin" replace />} />
              <Route path="/admin/orders/:id" element={allowed('mall:order:read') ? <OrderDetail /> : <Navigate to="/admin" replace />} />
              <Route path="/admin/stocks" element={allowed('mall:stock:list') ? <Stocks /> : <Navigate to="/admin" replace />} />
              <Route path="/admin/after-sales" element={allowed('mall:afterSales:read') ? <AfterSales /> : <Navigate to="/admin" replace />} />
              <Route path="/admin/payments" element={allowed('mall:payment:read') ? <Payments /> : <Navigate to="/admin" replace />} />
              <Route path="*" element={<Navigate to="/admin" replace />} />
            </Routes>
          </Content>
        </Layout>
      </Layout>
  );
}
