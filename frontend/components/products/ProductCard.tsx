import Link from 'next/link';
import Image from 'next/image';
import { Card, Text } from '@vapor-ui/core';
import type { ProductListResponse } from '@/types';
import { API_BASE_URL } from '@/lib/api';

const FALLBACK_IMAGE = '/product-placeholder.svg';

function getProductImageUrl(imageUrl: string | null): string {
  if (!imageUrl) {
    return FALLBACK_IMAGE;
  }

  if (imageUrl.startsWith('http')) {
    return imageUrl;
  }

  return `${API_BASE_URL}${imageUrl}`;
}

function formatPrice(price: number): string {
  return `₩${price.toLocaleString('ko-KR')}`;
}

interface ProductCardProps {
  product: ProductListResponse;
}

export default function ProductCard({ product }: ProductCardProps) {
  const imageSrc = getProductImageUrl(product.imageUrl);

  return (
    <Link href={`/products/${product.id}`} className="group block">
      <Card.Root className="h-full overflow-hidden border border-zinc-200 bg-white transition-shadow hover:shadow-md dark:border-zinc-800 dark:bg-zinc-950">
        <div className="relative aspect-square w-full overflow-hidden bg-zinc-100 dark:bg-zinc-900">
          <Image
            src={imageSrc}
            alt={product.name}
            fill
            sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw"
            className="object-cover transition-transform group-hover:scale-105"
            unoptimized={imageSrc.startsWith('http')}
          />
        </div>
        <Card.Body className="px-4 py-3">
          <Text typography="body1" foreground="primary-100" className="line-clamp-2">
            {product.name}
          </Text>
          <Text typography="subtitle1" foreground="primary-100" className="mt-1 block">
            {formatPrice(product.price)}
          </Text>
        </Card.Body>
      </Card.Root>
    </Link>
  );
}

export { FALLBACK_IMAGE, getProductImageUrl, formatPrice };