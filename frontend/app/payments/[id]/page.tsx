'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { Badge, Card, Skeleton, Table } from '@vapor-ui/core';
import AuthGuard from '@/components/auth/AuthGuard';
import { formatPrice } from '@/components/products/ProductCard';
import { paymentsApi } from '@/lib/api';
import type { PaymentResponse } from '@/types';
import { formatDate, getStatusPalette } from '../page';

export default function PaymentDetailPage() {
  const params = useParams();
  const paymentId = Number(params.id);
  const [payment, setPayment] = useState<PaymentResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;

    async function fetchPayment() {
      setIsLoading(true);
      setError('');

      try {
        const data = await paymentsApi.getDetail(paymentId);
        if (!cancelled) {
          setPayment(data);
        }
      } catch (caughtError) {
        if (!cancelled) {
          setError(caughtError instanceof Error ? caughtError.message : '결제 상세를 불러오지 못했습니다.');
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    fetchPayment();

    return () => {
      cancelled = true;
    };
  }, [paymentId]);

  return (
    <AuthGuard>
      <main className="flex flex-1 flex-col bg-zinc-50 dark:bg-black">
        <div className="mx-auto w-full max-w-6xl px-6 py-10">
          <Link className="mb-8 inline-flex text-sm font-medium text-zinc-600 hover:text-zinc-950 dark:text-zinc-400 dark:hover:text-zinc-50" href="/payments">
            ← 결제 내역으로 돌아가기
          </Link>

          {error && <div className="mb-5 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950/40 dark:text-red-300">{error}</div>}

          {isLoading ? (
            <Skeleton shape="rounded" size="xl" animation="shimmer" className="h-96 w-full" />
          ) : payment ? (
            <div className="space-y-6">
              <Card.Root className="border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
                <Card.Header className="flex flex-col gap-4 px-6 pt-6 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <p className="text-sm font-medium text-blue-600 dark:text-blue-400">Payment #{payment.id}</p>
                    <h1 className="mt-2 text-2xl font-semibold text-zinc-950 dark:text-zinc-50">결제 상세</h1>
                    <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">{formatDate(payment.createdAt)}</p>
                  </div>
                  <Badge colorPalette={getStatusPalette(payment.status)} shape="pill" size="md">
                    {payment.status}
                  </Badge>
                </Card.Header>
                <Card.Body className="px-6 py-6">
                  <div className="flex items-center justify-between rounded-lg bg-zinc-50 px-4 py-4 dark:bg-zinc-900">
                    <span className="text-sm text-zinc-600 dark:text-zinc-400">총 결제 금액</span>
                    <span className="text-xl font-semibold text-zinc-950 dark:text-zinc-50">{formatPrice(payment.totalAmount)}</span>
                  </div>
                  {payment.message && <p className="mt-4 text-sm text-zinc-600 dark:text-zinc-400">{payment.message}</p>}
                </Card.Body>
              </Card.Root>

              <Card.Root className="overflow-hidden border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
                <Card.Header className="px-6 pt-6">
                  <h2 className="text-lg font-semibold text-zinc-950 dark:text-zinc-50">구매 상품</h2>
                </Card.Header>
                <Card.Body className="px-0 py-4">
                  <Table.Root className="w-full">
                    <Table.Header>
                      <Table.Row>
                        <Table.Heading className="px-6 py-3 text-left">상품명</Table.Heading>
                        <Table.Heading className="px-6 py-3 text-left">수량</Table.Heading>
                        <Table.Heading className="px-6 py-3 text-left">단가</Table.Heading>
                        <Table.Heading className="px-6 py-3 text-right">합계</Table.Heading>
                      </Table.Row>
                    </Table.Header>
                    <Table.Body>
                      {payment.items.map((item) => (
                        <Table.Row key={item.id} className="border-t border-zinc-100 dark:border-zinc-800">
                          <Table.Cell className="px-6 py-4 font-medium text-zinc-950 dark:text-zinc-50">{item.productName}</Table.Cell>
                          <Table.Cell className="px-6 py-4 text-zinc-600 dark:text-zinc-400">{item.quantity}개</Table.Cell>
                          <Table.Cell className="px-6 py-4 text-zinc-600 dark:text-zinc-400">{formatPrice(item.unitPrice)}</Table.Cell>
                          <Table.Cell className="px-6 py-4 text-right font-medium text-zinc-950 dark:text-zinc-50">{formatPrice(item.totalPrice)}</Table.Cell>
                        </Table.Row>
                      ))}
                    </Table.Body>
                  </Table.Root>
                </Card.Body>
              </Card.Root>
            </div>
          ) : null}
        </div>
      </main>
    </AuthGuard>
  );
}
