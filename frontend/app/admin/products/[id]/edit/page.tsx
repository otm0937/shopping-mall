'use client';

import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Button, Card, Skeleton } from '@vapor-ui/core';
import AdminGuard from '@/components/auth/AdminGuard';
import ProductForm from '@/components/admin/ProductForm';
import { adminApi, ApiError } from '@/lib/api';
import { useProduct } from '@/hooks/useProducts';

export default function EditProductPage() {
  const params = useParams();
  const router = useRouter();
  const productId = Number(params.id);
  const { product, isLoading, error: loadError } = useProduct(productId);
  const [submitError, setSubmitError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (formData: FormData) => {
    setSubmitError('');
    setIsSubmitting(true);

    try {
      await adminApi.updateProduct(productId, formData);
      router.replace('/admin/products');
    } catch (caughtError) {
      setSubmitError(
        caughtError instanceof ApiError || caughtError instanceof Error
          ? caughtError.message
          : '상품을 수정하지 못했습니다.',
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AdminGuard>
      <main className="flex flex-1 flex-col bg-zinc-50 dark:bg-black">
        <div className="mx-auto w-full max-w-4xl px-6 py-10">
          <div className="mb-8">
            <p className="text-sm font-medium text-blue-600 dark:text-blue-400">관리자</p>
            <h1 className="mt-2 text-2xl font-semibold tracking-tight text-zinc-950 dark:text-zinc-50">
              상품 수정
            </h1>
            <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
              기존 상품 정보를 수정하고 필요한 경우 이미지를 교체하세요.
            </p>
          </div>

          {(loadError || submitError) && (
            <Card.Root className="mb-6 border border-red-200 bg-red-50 dark:border-red-900 dark:bg-red-950/40">
              <Card.Body className="px-4 py-3 text-sm text-red-700 dark:text-red-300">
                {submitError || loadError}
              </Card.Body>
            </Card.Root>
          )}

          {isLoading ? (
            <Card.Root className="border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
              <Card.Body className="grid gap-6 px-6 py-6 lg:grid-cols-[1fr_320px]">
                <div className="flex flex-col gap-5">
                  <Skeleton shape="rounded" size="lg" animation="shimmer" className="h-12 w-full" />
                  <Skeleton shape="rounded" size="lg" animation="shimmer" className="h-12 w-full" />
                  <Skeleton shape="rounded" size="xl" animation="shimmer" className="h-40 w-full" />
                </div>
                <Skeleton shape="square" size="xl" animation="shimmer" className="aspect-square w-full" />
              </Card.Body>
            </Card.Root>
          ) : product ? (
            <ProductForm
              initialProduct={product}
              submitLabel="상품 수정"
              isSubmitting={isSubmitting}
              onSubmit={handleSubmit}
              onCancel={() => router.push('/admin/products')}
            />
          ) : (
            <Card.Root className="border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
              <Card.Body className="px-6 py-6">
                <p className="text-sm text-zinc-600 dark:text-zinc-400">상품 정보를 찾을 수 없습니다.</p>
              </Card.Body>
              <Card.Footer className="border-t border-zinc-100 px-6 py-4 dark:border-zinc-800">
                <Button variant="outline" onClick={() => router.push('/admin/products')}>
                  목록으로 돌아가기
                </Button>
              </Card.Footer>
            </Card.Root>
          )}
        </div>
      </main>
    </AdminGuard>
  );
}
