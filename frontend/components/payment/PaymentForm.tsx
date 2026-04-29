'use client';

import Link from 'next/link';
import { useState } from 'react';
import { Button, Card } from '@vapor-ui/core';
import { paymentsApi } from '@/lib/api';
import { useCart } from '@/hooks/useCart';
import { formatPrice } from '@/components/products/ProductCard';
import type { PaymentResponse } from '@/types';

export default function PaymentForm() {
  const { items, totalPrice, refreshCart } = useCart();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [payment, setPayment] = useState<PaymentResponse | null>(null);

  const handlePayment = async () => {
    setIsSubmitting(true);
    setError('');
    setPayment(null);

    try {
      const response = await paymentsApi.processPayment();
      setPayment(response);
      await refreshCart();
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : '결제를 완료하지 못했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Card.Root className="border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
      <Card.Header className="px-6 pt-6">
        <h2 className="text-lg font-semibold text-zinc-950 dark:text-zinc-50">결제</h2>
        <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">현재는 MOCK 결제만 지원합니다.</p>
      </Card.Header>
      <Card.Body className="px-6 py-5">
        <div className="rounded-lg border border-zinc-200 px-4 py-3 dark:border-zinc-800">
          <label className="flex items-center gap-3 text-sm font-medium text-zinc-800 dark:text-zinc-200">
            <input type="radio" checked readOnly className="size-4 accent-blue-600" />
            MOCK 결제
          </label>
        </div>

        <div className="mt-5 space-y-3">
          {items.map((item) => (
            <div key={item.id} className="flex items-start justify-between gap-3 text-sm">
              <div className="min-w-0 text-zinc-600 dark:text-zinc-400">
                <p className="truncate font-medium text-zinc-900 dark:text-zinc-100">{item.productName}</p>
                <p className="mt-1">{item.quantity}개</p>
              </div>
              <span className="font-medium text-zinc-950 dark:text-zinc-50">{formatPrice(item.totalPrice)}</span>
            </div>
          ))}
        </div>

        <div className="mt-5 flex items-center justify-between border-t border-zinc-100 pt-4 text-base font-semibold text-zinc-950 dark:border-zinc-800 dark:text-zinc-50">
          <span>결제 금액</span>
          <span>{formatPrice(totalPrice)}</span>
        </div>

        {error && <p className="mt-4 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950/40 dark:text-red-300">{error}</p>}

        {payment && (
          <div className="mt-4 rounded-lg bg-green-50 px-4 py-3 text-sm text-green-700 dark:bg-green-950/40 dark:text-green-300">
            <p>{payment.message || '결제가 완료되었습니다.'}</p>
            <Link className="mt-2 inline-flex font-medium underline" href={`/payments/${payment.id}`}>
              결제 상세 보기
            </Link>
          </div>
        )}
      </Card.Body>
      <Card.Footer className="border-t border-zinc-100 px-6 py-4 dark:border-zinc-800">
        <Button type="button" className="w-full" size="lg" disabled={isSubmitting || items.length === 0} onClick={handlePayment}>
          {isSubmitting ? '결제 처리 중...' : 'Complete Payment'}
        </Button>
      </Card.Footer>
    </Card.Root>
  );
}
