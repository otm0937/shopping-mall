'use client';

import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { Button, Card, Text } from '@vapor-ui/core';
import type { ProductListResponse } from '@/types';
import { FALLBACK_IMAGE, formatPrice, getProductImageUrl } from '@/components/products/ProductCard';

interface AdminProductCardProps {
  product: ProductListResponse;
  isDeleting: boolean;
  onDelete: (productId: number) => Promise<void>;
}

export default function AdminProductCard({ product, isDeleting, onDelete }: AdminProductCardProps) {
  const router = useRouter();
  const imageSrc = product.imageUrl ? getProductImageUrl(product.imageUrl) : FALLBACK_IMAGE;

  const handleDelete = async () => {
    const confirmed = window.confirm(`'${product.name}' 상품을 삭제하시겠습니까?`);

    if (!confirmed) {
      return;
    }

    await onDelete(product.id);
  };

  return (
    <Card.Root className="h-full overflow-hidden border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
      <div className="relative aspect-square w-full overflow-hidden bg-zinc-100 dark:bg-zinc-900">
        <Image
          src={imageSrc}
          alt={product.name}
          fill
          sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw"
          className="object-cover"
          unoptimized={imageSrc.startsWith('http')}
        />
      </div>
      <Card.Body className="px-4 py-4">
        <Text typography="body1" foreground="primary-100" className="line-clamp-2">
          {product.name}
        </Text>
        <Text typography="subtitle1" foreground="primary-100" className="mt-1 block">
          {formatPrice(product.price)}
        </Text>
        <p className="mt-1 text-xs text-zinc-500 dark:text-zinc-400">상품 ID: {product.id}</p>
      </Card.Body>
      <Card.Footer className="flex gap-2 border-t border-zinc-100 px-4 py-3 dark:border-zinc-800">
        <Button type="button" variant="outline" size="sm" className="flex-1" onClick={() => router.push(`/admin/products/${product.id}/edit`)}>
          수정
        </Button>
        <Button type="button" colorPalette="danger" variant="outline" size="sm" className="flex-1" disabled={isDeleting} onClick={handleDelete}>
          {isDeleting ? '삭제 중...' : '삭제'}
        </Button>
      </Card.Footer>
    </Card.Root>
  );
}
