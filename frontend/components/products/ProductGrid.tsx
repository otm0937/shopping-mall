import type { ProductListResponse } from '@/types';
import ProductCard from './ProductCard';

interface ProductGridProps {
  products: ProductListResponse[];
}

export default function ProductGrid({ products }: ProductGridProps) {
  if (products.length === 0) {
    return (
      <div className="flex min-h-[320px] flex-col items-center justify-center gap-3 py-16">
        <p className="text-lg font-medium text-zinc-950 dark:text-zinc-50">등록된 상품이 없습니다</p>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">아직 판매할 상품이 준비되지 않았습니다.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
}