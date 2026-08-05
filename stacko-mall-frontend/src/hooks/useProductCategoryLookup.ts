import { useEffect, useState } from 'react';
import { api } from '../services/api';
import { useProductCategories } from './useProductCategories';

export function useProductCategoryLookup() {
  const { categoryMap } = useProductCategories();
  const [productCategoryMap, setProductCategoryMap] = useState<Record<string, string | null | undefined>>({});

  useEffect(() => {
    const load = async () => {
      try {
        const resp = await api.getProducts();
        if (resp.data.success) {
          setProductCategoryMap(
            (resp.data.data || []).reduce<Record<string, string | null | undefined>>((result, product) => {
              result[product.id] = product.categoryId;
              return result;
            }, {})
          );
        }
      } catch {
        // 订单商品类目展示失败不影响订单主流程。
      }
    };
    load();
  }, []);

  return {
    categoryMap,
    getProductCategoryId: (productId: string) => productCategoryMap[productId]
  };
}
