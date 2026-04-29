'use client';

import { Skeleton } from '@vapor-ui/core';
import { useProducts } from '@/hooks/useProducts';
import ProductGrid from '@/components/products/ProductGrid';

function ProductGridSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {Array.from({ length: 8 }).map((_, index) => (
        <div key={index} className="overflow-hidden rounded-lg border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-950">
          <Skeleton shape="square" size="xl" animation="shimmer" className="aspect-square w-full" />
          <div className="px-4 py-3">
            <Skeleton shape="rounded" size="md" animation="shimmer" className="mb-2 h-4 w-3/4" />
            <Skeleton shape="rounded" size="sm" animation="shimmer" className="h-5 w-1/3" />
          </div>
        </div>
      ))}
    </div>
  );
}

export default function HomePage() {
  const { products, isLoading, error } = useProducts();

  return (
    <main className="flex flex-1 flex-col bg-zinc-50 dark:bg-black">
      <div className="mx-auto w-full max-w-6xl px-6 py-10">
        <div className="mb-8">
          <h1 className="text-2xl font-semibold tracking-tight text-zinc-950 dark:text-zinc-50">
            전체 상품
          </h1>
          <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
            다양한 상품을 둘러보고 원하는 것을 찾아보세요.
          </p>
        </div>

        {error && (
          <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950/40 dark:text-red-300">
            {error}
          </div>
        )}

        {isLoading ? <ProductGridSkeleton /> : <ProductGrid products={products} />}
      </div>
    </main>
  );
}