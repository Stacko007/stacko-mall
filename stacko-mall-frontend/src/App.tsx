import { Button, Layout, Menu, Space } from 'antd';
import { Link, Route, Routes, useLocation } from 'react-router-dom';
import Home from './pages/Home';
import Category from './pages/Category';
import Cart from './pages/Cart';
import Orders from './pages/Orders';
import Profile from './pages/Profile';
import Login from './pages/Login';
import Register from './pages/Register';
import Search from './pages/Search';
import ProductList from './pages/ProductList';
import ProductDetail from './pages/ProductDetail';
import ConfirmOrder from './pages/ConfirmOrder';
import Payment from './pages/Payment';
import PaymentResult from './pages/PaymentResult';
import OrderDetail from './pages/OrderDetail';
import { session } from './store/session';
import './App.css';

const { Header, Content, Footer } = Layout;

const navItems = [
  { key: '/', label: '首页' },
  { key: '/category', label: '分类' },
  { key: '/search', label: '搜索' },
  { key: '/products', label: '商品列表' },
  { key: '/cart', label: '购物车' },
  { key: '/orders', label: '订单' },
  { key: '/profile', label: '我的' }
];

export default function App() {
  const location = useLocation();
  const pathname = location.pathname;
  const selectedKey = (() => {
    if (pathname === '/') return '/';
    if (pathname.startsWith('/category')) return '/category';
    if (pathname.startsWith('/search')) return '/search';
    if (pathname.startsWith('/products')) return '/products';
    if (pathname.startsWith('/cart')) return '/cart';
    if (pathname.startsWith('/orders')) return '/orders';
    if (pathname.startsWith('/profile')) return '/profile';
    return pathname;
  })();

  return (
    <Layout className="app-layout">
      <Header className="app-header">
        <div className="logo">Stacko Mall</div>
        <Menu
          theme="dark"
          mode="horizontal"
          selectedKeys={[selectedKey]}
          items={navItems.map((item) => ({
            key: item.key,
            label: <Link to={item.key}>{item.label}</Link>
          }))}
          style={{ flex: 1 }}
        />
        {session.getToken() ? (
          <Button
            type="primary"
            ghost
            onClick={() => {
              session.clearAll();
              window.location.href = '/login';
            }}
          >
            退出
          </Button>
        ) : (
          <Space>
            <Button type="primary">
              <Link to="/login">登录</Link>
            </Button>
            <Button>
              <Link to="/register">注册</Link>
            </Button>
          </Space>
        )}
      </Header>
      <Content className="app-content">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/category" element={<Category />} />
          <Route path="/search" element={<Search />} />
          <Route path="/products" element={<ProductList />} />
          <Route path="/products/:id" element={<ProductDetail />} />
          <Route path="/confirm" element={<ConfirmOrder />} />
          <Route path="/payment/:id" element={<Payment />} />
          <Route path="/payment-result/:id" element={<PaymentResult />} />
          <Route path="/cart" element={<Cart />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/orders/:id" element={<OrderDetail />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
        </Routes>
      </Content>
      <Footer className="app-footer">Stacko Mall ©2026</Footer>
    </Layout>
  );
}
