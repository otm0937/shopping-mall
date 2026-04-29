'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Button, Card, Skeleton } from '@vapor-ui/core';
import AuthGuard from '@/components/auth/AuthGuard';
import CartItem from '@/components/cart/CartItem';
import PaymentForm from '@/components/payment/PaymentForm';
import { formatPrice } from '@/components/products/ProductCard';
import { useCart } from '@/hooks/useCart';

export default function CartPage() {
  const { items, totalPrice, isLoading, error, removeFromCart } = useCart();
  const [removingItemId, setRemovingItemId] = useState<number | null>(null);
  const [removeError, setRemoveError] = useState('');

  const handleRemove = async (cartItemId: number) => {
    setRemovingItemId(cartItemId);
    setRemoveError('');

    try {
      await removeFromCart(cartItemId);
    } catch (caughtError) {
      setRemoveError(caughtError instanceof Error ? caughtError.message : '상품을 삭제하지 못했습니다.');
    } finally {
      setRemovingItemId(null);
    }
  };

  return (
    <AuthGuard>
      <main className="flex flex-1 flex-col bg-zinc-50 dark:bg-black">
        <div className="mx-auto w-full max-w-6xl px-6 py-10">
          <div className="mb-8 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <h1 className="text-2xl font-semibold tracking-tight text-zinc-950 dark:text-zinc-50">장바구니</h1>
              <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">담아둔 상품을 확인하고 결제를 진행하세요.</p>
            </div>
            <Link className="text-sm font-medium text-blue-600 hover:text-blue-700 dark:text-blue-400" href="/products">
              상품 계속 보기
            </Link>
          </div>

          {(error || removeError) && (
            <div className="mb-5 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950/40 dark:text-red-300">
              {removeError || error}
            </div>
          )}

          {isLoading && items.length === 0 ? (
            <div className="space-y-4">
              {Array.from({ length: 3 }).map((_, index) => (
                <Skeleton key={index} shape="rounded" size="xl" animation="shimmer" className="h-28 w-full" />
              ))}
            </div>
          ) : items.length === 0 ? (
            <Card.Root className="border border-zinc-200 bg-white text-center shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
              <Card.Body className="px-6 py-14">
                <h2 className="text-lg font-semibold text-zinc-950 dark:text-zinc-50">장바구니가 비어 있습니다</h2>
                <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">마음에 드는 상품을 장바구니에 담아보세요.</p>
                <Button className="mt-6" onClick={() => window.location.assign('/products')}>
                  상품 보러 가기
                </Button>
              </Card.Body>
            </Card.Root>
          ) : (
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_360px]">
              <div className="space-y-4">
                {items.map((item) => (
                  <CartItem key={item.id} item={item} onRemove={handleRemove} isRemoving={removingItemId === item.id} />
                ))}
              </div>

              <div className="space-y-4">
                <Card.Root className="border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
                  <Card.Header className="px-6 pt-6">
                    <h2 className="text-lg font-semibold text-zinc-950 dark:text-zinc-50">주문 요약</h2>
                  </Card.Header>
                  <Card.Body className="px-6 py-5">
                    <div className="flex items-center justify-between text-sm text-zinc-600 dark:text-zinc-400">
                      <span>상품 {items.length}종</span>
                      <span>{formatPrice(totalPrice)}</span>
                    </div>
                    <div className="mt-4 flex items-center justify-between border-t border-zinc-100 pt-4 text-base font-semibold text-zinc-950 dark:border-zinc-800 dark:text-zinc-50">
                      <span>총 결제 금액</span>
                      <span>{formatPrice(totalPrice)}</span>
                    </div>
                  </Card.Body>
                </Card.Root>

                <PaymentForm />
              </div>
            </div>
          )}
        </div>
      </main>
    </AuthGuard>
  );
}
