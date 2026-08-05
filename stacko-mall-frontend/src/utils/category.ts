import type { ProductCategory } from '../services/api';

export function flattenCategories(categories: ProductCategory[]) {
  const result: Record<string, ProductCategory> = {};

  const visit = (items: ProductCategory[]) => {
    items.forEach((item) => {
      result[item.id] = item;
      if (item.children && item.children.length > 0) {
        visit(item.children);
      }
    });
  };

  visit(categories);
  return result;
}

export function categoryQueryPath(categoryId: string) {
  return `/category?categoryId=${encodeURIComponent(categoryId)}`;
}
