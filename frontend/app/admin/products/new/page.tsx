'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import AdminGuard from '@/components/auth/AdminGuard';
import ProductForm from '@/components/admin/ProductForm';
import { adminApi, ApiError } from '@/lib/api';

export default function NewProductPage() {
  const router = useRouter();
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (formData: FormData) => {
    setError('');
    setIsSubmitting(true);

    try {
      await adminApi.createProduct(formData);
      router.replace('/admin/products');
    } catch (caughtError) {
      setError(
        caughtError instanceof ApiError || caughtError instanceof Error
          ? caughtError.message
          : '상품을 등록하지 못했습니다.',
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
              새 상품 등록
            </h1>
            <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
              상품 정보와 이미지를 입력해 새 상품을 등록하세요.
            </p>
          </div>

          {error && (
            <p className="mb-6 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950/40 dark:text-red-300">
              {error}
            </p>
          )}

          <ProductForm
            submitLabel="상품 등록"
            isSubmitting={isSubmitting}
            onSubmit={handleSubmit}
            onCancel={() => router.push('/admin/products')}
          />
        </div>
      </main>
    </AdminGuard>
  );
}
