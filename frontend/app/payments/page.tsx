'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Badge, Card, Skeleton, Table } from '@vapor-ui/core';
import AuthGuard from '@/components/auth/AuthGuard';
import { formatPrice } from '@/components/products/ProductCard';
import { paymentsApi } from '@/lib/api';
import type { PaymentHistoryResponse, PaymentStatus } from '@/types';

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function getStatusPalette(status: PaymentStatus): 'warning' | 'success' | 'danger' | 'hint' {
  switch (status) {
    case 'PENDING':
      return 'warning';
    case 'COMPLETED':
      return 'success';
    case 'FAILED':
      return 'danger';
    case 'CANCELLED':
      return 'hint';
  }
}

export default function PaymentHistoryPage() {
  const [payments, setPayments] = useState<PaymentHistoryResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;

    async function fetchPayments() {
      setIsLoading(true);
      setError('');

      try {
        const data = await paymentsApi.getHistory();
        if (!cancelled) {
          setPayments(data);
        }
      } catch (caughtError) {
        if (!cancelled) {
          setError(caughtError instanceof Error ? caughtError.message : '결제 내역을 불러오지 못했습니다.');
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    fetchPayments();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <AuthGuard>
      <main className="flex flex-1 flex-col bg-zinc-50 dark:bg-black">
        <div className="mx-auto w-full max-w-6xl px-6 py-10">
          <div className="mb-8">
            <h1 className="text-2xl font-semibold tracking-tight text-zinc-950 dark:text-zinc-50">결제 내역</h1>
            <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">완료한 결제와 상태를 확인하세요.</p>
          </div>

          {error && <div className="mb-5 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950/40 dark:text-red-300">{error}</div>}

          {isLoading ? (
            <Skeleton shape="rounded" size="xl" animation="shimmer" className="h-64 w-full" />
          ) : payments.length === 0 ? (
            <Card.Root className="border border-zinc-200 bg-white text-center shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
              <Card.Body className="px-6 py-14">
                <h2 className="text-lg font-semibold text-zinc-950 dark:text-zinc-50">결제 내역이 없습니다</h2>
                <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">장바구니에 상품을 담고 첫 결제를 진행해 보세요.</p>
              </Card.Body>
            </Card.Root>
          ) : (
            <Card.Root className="overflow-hidden border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
              <Table.Root className="w-full">
                <Table.Header>
                  <Table.Row>
                    <Table.Heading className="px-5 py-3 text-left">결제일</Table.Heading>
                    <Table.Heading className="px-5 py-3 text-left">수량</Table.Heading>
                    <Table.Heading className="px-5 py-3 text-left">금액</Table.Heading>
                    <Table.Heading className="px-5 py-3 text-left">상태</Table.Heading>
                    <Table.Heading className="px-5 py-3 text-right">상세</Table.Heading>
                  </Table.Row>
                </Table.Header>
                <Table.Body>
                  {payments.map((payment) => (
                    <Table.Row key={payment.id} className="border-t border-zinc-100 dark:border-zinc-800">
                      <Table.Cell className="px-5 py-4 text-sm text-zinc-600 dark:text-zinc-400">{formatDate(payment.createdAt)}</Table.Cell>
                      <Table.Cell className="px-5 py-4 text-sm text-zinc-600 dark:text-zinc-400">{payment.totalQuantity}개</Table.Cell>
                      <Table.Cell className="px-5 py-4 font-medium text-zinc-950 dark:text-zinc-50">{formatPrice(payment.totalAmount)}</Table.Cell>
                      <Table.Cell className="px-5 py-4">
                        <Badge colorPalette={getStatusPalette(payment.status)} shape="pill" size="sm">
                          {payment.status}
                        </Badge>
                      </Table.Cell>
                      <Table.Cell className="px-5 py-4 text-right">
                        <Link className="text-sm font-medium text-blue-600 hover:text-blue-700 dark:text-blue-400" href={`/payments/${payment.id}`}>
                          보기
                        </Link>
                      </Table.Cell>
                    </Table.Row>
                  ))}
                </Table.Body>
              </Table.Root>
            </Card.Root>
          )}
        </div>
      </main>
    </AuthGuard>
  );
}

export { formatDate, getStatusPalette };
