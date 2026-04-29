'use client';

import Link from 'next/link';
import Image from 'next/image';
import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Button, Card, Skeleton, Text } from '@vapor-ui/core';
import { useProduct } from '@/hooks/useProducts';
import { useAuth } from '@/hooks/useAuth';
import { useCart } from '@/hooks/useCart';
import { API_BASE_URL } from '@/lib/api';
import { FALLBACK_IMAGE, formatPrice } from '@/components/products/ProductCard';

function ProductDetailSkeleton() {
  return (
    <div className="mx-auto w-full max-w-6xl px-6 py-10">
      <Skeleton shape="rounded" size="sm" animation="shimmer" className="mb-8 h-4 w-24" />
      <div className="grid grid-cols-1 gap-10 md:grid-cols-2">
        <Skeleton shape="square" size="xl" animation="shimmer" className="aspect-square w-full" />
        <div className="flex flex-col gap-4">
          <Skeleton shape="rounded" size="lg" animation="shimmer" className="h-8 w-3/4" />
          <Skeleton shape="rounded" size="md" animation="shimmer" className="h-6 w-1/3" />
          <Skeleton shape="rounded" size="md" animation="shimmer" className="mt-4 h-4 w-full" />
          <Skeleton shape="rounded" size="md" animation="shimmer" className="h-4 w-5/6" />
          <Skeleton shape="rounded" size="md" animation="shimmer" className="h-4 w-2/3" />
        </div>
      </div>
    </div>
  );
}

interface AddToCartButtonProps {
  productId: number;
}

function AddToCartButton({ productId }: AddToCartButtonProps) {
  const { isAuthenticated, isLoading } = useAuth();
  const { addToCart } = useCart();
  const [isAdding, setIsAdding] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleQuantityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseInt(e.target.value, 10);
    if (isNaN(value) || value < 1) {
      setQuantity(1);
    } else if (value > 99) {
      setQuantity(99);
    } else {
      setQuantity(value);
    }
  };

  const handleAddToCart = async () => {
    setIsAdding(true);
    setMessage('');
    setError('');

    try {
      const cart = await addToCart(productId, quantity);
      setMessage(cart.message || `장바구니에 ${quantity}개의 상품을 담았습니다.`);
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : '장바구니에 담지 못했습니다.');
    } finally {
      setIsAdding(false);
    }
  };

  if (isLoading) {
    return <Skeleton shape="rounded" size="lg" animation="shimmer" className="h-12 w-full" />;
  }

  if (!isAuthenticated) {
    return (
      <Text typography="body2" foreground="hint-200" className="text-center">
        <Link href="/login" className="font-medium text-blue-600 hover:text-blue-700 dark:text-blue-400">
          로그인
        </Link>
        후 장바구니에 담을 수 있습니다.
      </Text>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-4">
        <label htmlFor="quantity" className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
          수량
        </label>
        <div className="flex items-center gap-2">
          <button
            type="button"
            className="flex h-10 w-10 items-center justify-center rounded-md border border-zinc-300 bg-white text-zinc-700 hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:bg-zinc-700"
            onClick={() => setQuantity((prev) => Math.max(1, prev - 1))}
            disabled={quantity <= 1 || isAdding}
          >
            −
          </button>
          <input
            id="quantity"
            type="number"
            min={1}
            max={99}
            value={quantity}
            onChange={handleQuantityChange}
            disabled={isAdding}
            className="h-10 w-16 appearance-none rounded-md border border-zinc-300 bg-white py-0 text-center text-sm font-medium text-zinc-900 [appearance:textfield] focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:cursor-not-allowed disabled:opacity-50 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100 [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
          />
          <button
            type="button"
            className="flex h-10 w-10 items-center justify-center rounded-md border border-zinc-300 bg-white text-zinc-700 hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:bg-zinc-700"
            onClick={() => setQuantity((prev) => Math.min(99, prev + 1))}
            disabled={quantity >= 99 || isAdding}
          >
            +
          </button>
        </div>
      </div>
      <Button
        type="button"
        size="lg"
        colorPalette="primary"
        variant="fill"
        className="w-full"
        disabled={isAdding}
        onClick={handleAddToCart}
      >
        {isAdding ? '담는 중...' : `장바구니 담기 (${quantity}개)`}
      </Button>
      {message && <p className="rounded-lg bg-green-50 px-4 py-3 text-sm text-green-700 dark:bg-green-950/40 dark:text-green-300">{message}</p>}
      {error && <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950/40 dark:text-red-300">{error}</p>}
    </div>
  );
}

export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const productId = Number(params.id);
  const { product, isLoading, error } = useProduct(productId);

  if (error) {
    return (
      <main className="flex flex-1 items-center justify-center bg-zinc-50 px-6 py-16 dark:bg-black">
        <Card.Root className="w-full max-w-md border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
          <Card.Header className="px-6 pt-6">
            <h2 className="text-xl font-semibold text-zinc-950 dark:text-zinc-50">상품을 찾을 수 없습니다</h2>
          </Card.Header>
          <Card.Body className="px-6 pb-6 pt-3">
            <p className="text-sm leading-6 text-zinc-600 dark:text-zinc-400">{error}</p>
          </Card.Body>
          <Card.Footer className="border-t border-zinc-100 px-6 py-4 dark:border-zinc-800">
            <Button variant="outline" onClick={() => router.push('/')}>
              상품 목록으로 돌아가기
            </Button>
          </Card.Footer>
        </Card.Root>
      </main>
    );
  }

  if (isLoading || !product) {
    return (
      <main className="flex flex-1 flex-col bg-zinc-50 dark:bg-black">
        <ProductDetailSkeleton />
      </main>
    );
  }

  const imageSrc = product.imageUrl
    ? product.imageUrl.startsWith('http')
      ? product.imageUrl
      : `${API_BASE_URL}${product.imageUrl}`
    : FALLBACK_IMAGE;

  return (
    <main className="flex flex-1 flex-col bg-zinc-50 dark:bg-black">
      <div className="mx-auto w-full max-w-6xl px-6 py-10">
        <Link
          href="/"
          className="mb-8 inline-flex items-center gap-1 text-sm font-medium text-zinc-600 hover:text-zinc-950 dark:text-zinc-400 dark:hover:text-zinc-50"
        >
          ← 전체 상품
        </Link>

        <div className="grid grid-cols-1 gap-10 md:grid-cols-2">
          <div className="relative aspect-square w-full overflow-hidden rounded-lg bg-zinc-100 dark:bg-zinc-900">
            <Image
              src={imageSrc}
              alt={product.name}
              fill
              sizes="(max-width: 768px) 100vw, 50vw"
              className="object-cover"
              priority
              unoptimized={imageSrc.startsWith('http')}
            />
          </div>

          <div className="flex flex-col justify-between">
            <div>
              <Text typography="heading3" foreground="primary-100" className="block">
                {product.name}
              </Text>
              <Text typography="heading2" foreground="primary-100" className="mt-3 block">
                {formatPrice(product.price)}
              </Text>
              {product.description && (
                <div className="mt-6">
                  <Text typography="subtitle2" foreground="secondary-100" className="mb-2 block">
                    상품 설명
                  </Text>
                  <Text typography="body2" foreground="secondary-200" className="whitespace-pre-line leading-7">
                    {product.description}
                  </Text>
                </div>
              )}
            </div>

            <div className="mt-8">
              <AddToCartButton productId={product.id} />
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
