import { Button, Result } from 'antd';
import type { ReactNode } from 'react';

export function EmptyState({
  title = '暂无数据',
  description,
  action
}: {
  title?: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <Result
      status="info"
      title={title}
      subTitle={description}
      extra={action}
    />
  );
}

export function ErrorState({
  title = '加载失败',
  description,
  onRetry
}: {
  title?: string;
  description?: string;
  onRetry?: () => void;
}) {
  return (
    <Result
      status="error"
      title={title}
      subTitle={description}
      extra={
        onRetry ? (
          <Button type="primary" onClick={onRetry}>
            重试
          </Button>
        ) : undefined
      }
    />
  );
}
