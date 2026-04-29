'use client';

import Link from 'next/link';
import { Button, Card } from '@vapor-ui/core';
import { formatPrice } from '@/components/products/ProductCard';
import type { CartItemResponse } from '@/types';

interface CartItemProps {
  item: CartItemResponse;
  onRemove: (cartItemId: number) => void;
  isRemoving?: boolean;
}

export default function CartItem({ item, onRemove, isRemoving = false }: CartItemProps) {
  return (
    <Card.Root className="border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
      <Card.Body className="flex flex-col gap-4 px-5 py-5 sm:flex-row sm:items-center sm:justify-between">
        <div className="min-w-0 flex-1">
          <Link
            href={`/products/${item.productId}`}
            className="text-base font-semibold text-zinc-950 hover:text-blue-600 dark:text-zinc-50 dark:hover:text-blue-400"
          >
            {item.productName}
          </Link>
          <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-sm text-zinc-600 dark:text-zinc-400">
            <span>단가 {formatPrice(item.price)}</span>
            <span>수량 {item.quantity}개</span>
          </div>
        </div>

        <div className="flex items-center justify-between gap-4 sm:flex-col sm:items-end">
          <span className="text-lg font-semibold text-zinc-950 dark:text-zinc-50">
            {formatPrice(item.totalPrice)}
          </span>
          <Button type="button" size="sm" variant="outline" disabled={isRemoving} onClick={() => onRemove(item.id)}>
            {isRemoving ? '삭제 중...' : '삭제'}
          </Button>
        </div>
      </Card.Body>
    </Card.Root>
  );
}
