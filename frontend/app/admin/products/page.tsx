'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button, Card, Skeleton } from '@vapor-ui/core';
import AdminGuard from '@/components/auth/AdminGuard';
import AdminProductGrid from '@/components/admin/AdminProductGrid';
import { adminApi, ApiError } from '@/lib/api';
import { useProducts } from '@/hooks/useProducts';
import type { ProductListResponse } from '@/types';

function AdminProductGridSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {Array.from({ length: 8 }).map((_, index) => (
        <div key={index} className="overflow-hidden rounded-lg border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-950">
          <Skeleton shape="square" size="xl" animation="shimmer" className="aspect-square w-full" />
          <div className="px-4 py-4">
            <Skeleton shape="rounded" size="md" animation="shimmer" className="mb-2 h-4 w-3/4" />
            <Skeleton shape="rounded" size="sm" animation="shimmer" className="h-5 w-1/3" />
          </div>
          <div className="flex gap-2 border-t border-zinc-100 px-4 py-3 dark:border-zinc-800">
            <Skeleton shape="rounded" size="md" animation="shimmer" className="h-8 flex-1" />
            <Skeleton shape="rounded" size="md" animation="shimmer" className="h-8 flex-1" />
          </div>
        </div>
      ))}
    </div>
  );
}

export default function AdminProductsPage() {
  const router = useRouter();
  const { products, isLoading, error } = useProducts();
  const [deletedProductIds, setDeletedProductIds] = useState<number[]>([]);
  const [deleteError, setDeleteError] = useState('');
  const [deletingProductId, setDeletingProductId] = useState<number | null>(null);

  const adminProducts = useMemo<ProductListResponse[]>(
    () => products.filter((product) => !deletedProductIds.includes(product.id)),
    [deletedProductIds, products],
  );

  const handleDelete = async (productId: number) => {
    setDeleteError('');
    setDeletingProductId(productId);

    try {
      await adminApi.deleteProduct(productId);
      setDeletedProductIds((currentIds) => [...currentIds, productId]);
    } catch (caughtError) {
      setDeleteError(
        caughtError instanceof ApiError || caughtError instanceof Error
          ? caughtError.message
          : '상품을 삭제하지 못했습니다.',
      );
    } finally {
      setDeletingProductId(null);
    }
  };

  return (
    <AdminGuard>
      <main className="flex flex-1 flex-col bg-zinc-50 dark:bg-black">
        <div className="mx-auto w-full max-w-6xl px-6 py-10">
          <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p className="text-sm font-medium text-blue-600 dark:text-blue-400">관리자</p>
              <h1 className="mt-2 text-2xl font-semibold tracking-tight text-zinc-950 dark:text-zinc-50">
                상품 관리
              </h1>
              <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
                쇼핑몰 상품을 등록, 수정, 삭제할 수 있습니다.
              </p>
            </div>
            <Button type="button" size="lg" onClick={() => router.push('/admin/products/new')}>
              새 상품 추가
            </Button>
          </div>

          {(error || deleteError) && (
            <Card.Root className="mb-6 border border-red-200 bg-red-50 dark:border-red-900 dark:bg-red-950/40">
              <Card.Body className="px-4 py-3 text-sm text-red-700 dark:text-red-300">
                {deleteError || error}
              </Card.Body>
            </Card.Root>
          )}

          {isLoading ? (
            <AdminProductGridSkeleton />
          ) : (
            <AdminProductGrid products={adminProducts} deletingProductId={deletingProductId} onDelete={handleDelete} />
          )}
        </div>
      </main>
    </AdminGuard>
  );
}
