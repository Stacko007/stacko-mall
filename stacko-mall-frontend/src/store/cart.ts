import type { Product } from '../services/api';

export type CartItem = {
  productId: string;
  name: string;
  categoryId?: string | null;
  price: number;
  quantity: number;
  selected: boolean;
};

const CART_KEY = 'stacko_cart';

function readCart(): CartItem[] {
  try {
    const raw = localStorage.getItem(CART_KEY);
    return raw ? (JSON.parse(raw) as CartItem[]) : [];
  } catch {
    return [];
  }
}

function writeCart(list: CartItem[]) {
  localStorage.setItem(CART_KEY, JSON.stringify(list));
}

export const cartStore = {
  list(): CartItem[] {
    return readCart();
  },
  add(product: Product, quantity = 1) {
    const current = readCart();
    const existing = current.find((item) => item.productId === product.id);
    if (existing) {
      existing.quantity += quantity;
    } else {
      current.push({
        productId: product.id,
        name: product.name,
        categoryId: product.categoryId,
        price: product.price,
        quantity,
        selected: true
      });
    }
    writeCart(current);
    return current;
  },
  update(productId: string, patch: Partial<CartItem>) {
    const current = readCart();
    const target = current.find((item) => item.productId === productId);
    if (target) {
      Object.assign(target, patch);
    }
    writeCart(current);
    return current;
  },
  remove(productId: string) {
    const next = readCart().filter((item) => item.productId !== productId);
    writeCart(next);
    return next;
  },
  clearSelected() {
    const next = readCart().filter((item) => !item.selected);
    writeCart(next);
    return next;
  },
  toggleAll(selected: boolean) {
    const next = readCart().map((item) => ({ ...item, selected }));
    writeCart(next);
    return next;
  }
};
