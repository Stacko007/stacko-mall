import { Link } from 'react-router-dom';
import type { ProductCategory } from '../services/api';
import { categoryQueryPath } from '../utils/category';

type Props = {
  categoryId?: string | null;
  categoryMap: Record<string, ProductCategory>;
};

export function ProductCategoryLink({ categoryId, categoryMap }: Props) {
  if (!categoryId) {
    return <span>未分类</span>;
  }

  return (
    <Link to={categoryQueryPath(categoryId)}>
      {categoryMap[categoryId]?.name || '未知类目'}
    </Link>
  );
}
