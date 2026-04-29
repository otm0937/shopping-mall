'use client';

import type { ProductListResponse } from '@/types';
import AdminProductCard from './AdminProductCard';

interface AdminProductGridProps {
  products: ProductListResponse[];
  deletingProductId: number | null;
  onDelete: (productId: number) => Promise<void>;
}

export default function AdminProductGrid({ products, deletingProductId, onDelete }: AdminProductGridProps) {
  if (products.length === 0) {
    return (
      <div className="flex min-h-[320px] flex-col items-center justify-center gap-3 rounded-lg border border-dashed border-zinc-300 bg-white py-16 text-center dark:border-zinc-700 dark:bg-zinc-950">
        <p className="text-lg font-medium text-zinc-950 dark:text-zinc-50">등록된 상품이 없습니다</p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">새 상품을 추가해 쇼핑몰 판매를 시작하세요.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {products.map((product) => (
        <AdminProductCard
          key={product.id}
          product={product}
          isDeleting={deletingProductId === product.id}
          onDelete={onDelete}
        />
      ))}
    </div>
  );
}
