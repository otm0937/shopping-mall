'use client';

import { useEffect, useState } from 'react';
import { productsApi, ApiError } from '@/lib/api';
import type { ProductDetailResponse, ProductListResponse } from '@/types';

interface UseProductsResult {
  products: ProductListResponse[];
  isLoading: boolean;
  error: string | null;
}

interface UseProductResult {
  product: ProductDetailResponse | null;
  isLoading: boolean;
  error: string | null;
}

export function useProducts(): UseProductsResult {
  const [products, setProducts] = useState<ProductListResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function fetchProducts() {
      setIsLoading(true);
      setError(null);

      try {
        const data = await productsApi.getAll();
        if (!cancelled) {
          setProducts(data);
        }
      } catch (caughtError) {
        if (!cancelled) {
          setError(
            caughtError instanceof ApiError || caughtError instanceof Error
              ? caughtError.message
              : '상품을 불러오지 못했습니다.',
          );
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    fetchProducts();

    return () => {
      cancelled = true;
    };
  }, []);

  return { products, isLoading, error };
}

export function useProduct(id: number): UseProductResult {
  const [product, setProduct] = useState<ProductDetailResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function fetchProduct() {
      setIsLoading(true);
      setError(null);

      try {
        const data = await productsApi.getById(id);
        if (!cancelled) {
          setProduct(data);
        }
      } catch (caughtError) {
        if (!cancelled) {
          setError(
            caughtError instanceof ApiError || caughtError instanceof Error
              ? caughtError.message
              : '상품을 불러오지 못했습니다.',
          );
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    fetchProduct();

    return () => {
      cancelled = true;
    };
  }, [id]);

  return { product, isLoading, error };
}