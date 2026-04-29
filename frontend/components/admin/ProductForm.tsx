'use client';

import { ChangeEvent, FormEvent, useEffect, useMemo, useState } from 'react';
import { Button, Card, TextInput, Textarea } from '@vapor-ui/core';
import type { ProductDetailResponse } from '@/types';
import { FALLBACK_IMAGE, getProductImageUrl, formatPrice } from '@/components/products/ProductCard';

interface ProductFormProps {
  initialProduct?: ProductDetailResponse | null;
  submitLabel: string;
  isSubmitting: boolean;
  onSubmit: (formData: FormData) => Promise<void>;
  onCancel: () => void;
}

function buildProductFormData(name: string, price: string, description: string, image: File | null) {
  const formData = new FormData();

  formData.append('name', name.trim());
  formData.append('price', String(Number(price)));
  formData.append('description', description.trim());

  if (image) {
    formData.append('image', image);
  }

  return formData;
}

export default function ProductForm({
  initialProduct,
  submitLabel,
  isSubmitting,
  onSubmit,
  onCancel,
}: ProductFormProps) {
  const [name, setName] = useState(initialProduct?.name ?? '');
  const [price, setPrice] = useState(initialProduct ? String(initialProduct.price) : '');
  const [description, setDescription] = useState(initialProduct?.description ?? '');
  const [image, setImage] = useState<File | null>(null);
  const [error, setError] = useState('');

  const previewUrl = useMemo(() => (image ? URL.createObjectURL(image) : null), [image]);

  useEffect(() => {
    if (!previewUrl) {
      return undefined;
    }

    return () => {
      URL.revokeObjectURL(previewUrl);
    };
  }, [previewUrl]);

  const handleImageChange = (event: ChangeEvent<HTMLInputElement>) => {
    setImage(event.target.files?.[0] ?? null);
  };

  const validate = () => {
    if (!name.trim()) {
      return '상품명을 입력해 주세요.';
    }

    if (!price || Number(price) <= 0) {
      return '가격은 0보다 큰 숫자로 입력해 주세요.';
    }

    if (!description.trim()) {
      return '상품 설명을 입력해 주세요.';
    }

    return '';
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const validationError = validate();
    setError(validationError);

    if (validationError) {
      return;
    }

    await onSubmit(buildProductFormData(name, price, description, image));
  };

  const currentImageUrl = initialProduct?.imageUrl ? getProductImageUrl(initialProduct.imageUrl) : FALLBACK_IMAGE;
  const imageLabel = previewUrl ? '새 이미지 미리보기' : initialProduct?.imageUrl ? '현재 이미지' : '이미지 미리보기';

  return (
    <Card.Root className="border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
      <Card.Body className="px-6 py-6">
        <form className="grid gap-6 lg:grid-cols-[1fr_320px]" onSubmit={handleSubmit}>
          <div className="flex flex-col gap-5">
            <label className="flex flex-col gap-2 text-sm font-medium text-zinc-800 dark:text-zinc-200">
              상품명
              <TextInput
                value={name}
                onValueChange={setName}
                placeholder="상품명을 입력하세요"
                required
                size="lg"
              />
            </label>

            <label className="flex flex-col gap-2 text-sm font-medium text-zinc-800 dark:text-zinc-200">
              가격
              <TextInput
                value={price}
                onValueChange={setPrice}
                placeholder="가격을 입력하세요"
                inputMode="decimal"
                pattern="[0-9]*"
                required
                size="lg"
              />
              {Number(price) > 0 && (
                <span className="text-xs text-zinc-500 dark:text-zinc-400">표시 가격: {formatPrice(Number(price))}</span>
              )}
            </label>

            <label className="flex flex-col gap-2 text-sm font-medium text-zinc-800 dark:text-zinc-200">
              상품 설명
              <Textarea
                value={description}
                onValueChange={setDescription}
                placeholder="상품 설명을 입력하세요"
                required
                rows={7}
              />
            </label>

            <label className="flex flex-col gap-2 text-sm font-medium text-zinc-800 dark:text-zinc-200">
              상품 이미지
              <input
                type="file"
                accept="image/*"
                onChange={handleImageChange}
                className="rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-700 file:mr-3 file:rounded-md file:border-0 file:bg-blue-600 file:px-3 file:py-1.5 file:text-sm file:font-medium file:text-white hover:file:bg-blue-700 dark:border-zinc-800 dark:bg-zinc-950 dark:text-zinc-300"
              />
              <span className="text-xs text-zinc-500 dark:text-zinc-400">
                이미지를 선택하지 않으면 기존 이미지가 유지됩니다.
              </span>
            </label>

            {error && (
              <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950/40 dark:text-red-300">
                {error}
              </p>
            )}

            <div className="flex flex-col gap-3 sm:flex-row">
              <Button type="submit" size="lg" disabled={isSubmitting}>
                {isSubmitting ? '저장 중...' : submitLabel}
              </Button>
              <Button type="button" size="lg" variant="outline" disabled={isSubmitting} onClick={onCancel}>
                취소
              </Button>
            </div>
          </div>

          <div className="rounded-lg border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-800 dark:bg-zinc-900/50">
            <p className="mb-3 text-sm font-medium text-zinc-800 dark:text-zinc-200">{imageLabel}</p>
            <div className="aspect-square overflow-hidden rounded-lg bg-zinc-100 dark:bg-zinc-900">
              {/* eslint-disable-next-line @next/next/no-img-element */}
              <img
                src={previewUrl ?? currentImageUrl}
                alt="상품 이미지 미리보기"
                className="h-full w-full object-cover"
              />
            </div>
            {image && <p className="mt-3 text-xs text-zinc-500 dark:text-zinc-400">선택한 파일: {image.name}</p>}
          </div>
        </form>
      </Card.Body>
    </Card.Root>
  );
}
