import { useEffect, useMemo, useState } from 'react';
import { api, ProductCategory } from '../services/api';
import { flattenCategories } from '../utils/category';

export function useProductCategories() {
  const [categories, setCategories] = useState<ProductCategory[]>([]);

  useEffect(() => {
    const load = async () => {
      try {
        const resp = await api.getCategories();
        if (resp.data.success) {
          setCategories(resp.data.data || []);
        }
      } catch {
        // 类目展示失败不阻塞商品主流程。
      }
    };
    load();
  }, []);

  const categoryMap = useMemo(() => flattenCategories(categories), [categories]);

  return { categories, categoryMap };
}
