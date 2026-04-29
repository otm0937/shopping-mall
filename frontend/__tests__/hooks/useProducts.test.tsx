import { renderHook, waitFor } from '@testing-library/react';
import { productsApi } from '@/lib/api';
import { useProduct, useProducts } from '@/hooks/useProducts';
import { sampleProduct, sampleProductDetail, secondProduct } from '../test-utils';

jest.mock('@/lib/api', () => ({
  ApiError: class ApiError extends Error {
    status: number;

    constructor(message: string, status: number) {
      super(message);
      this.status = status;
    }
  },
  productsApi: {
    getAll: jest.fn(),
    getById: jest.fn(),
  },
}));

const mockProductsApi = productsApi as jest.Mocked<typeof productsApi>;

describe('useProducts', () => {
  it('fetches and exposes product lists', async () => {
    mockProductsApi.getAll.mockResolvedValue([sampleProduct, secondProduct]);

    const { result } = renderHook(() => useProducts());

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.products).toEqual([sampleProduct, secondProduct]);
    expect(result.current.error).toBeNull();
  });

  it('stores a readable error when product list loading fails', async () => {
    mockProductsApi.getAll.mockRejectedValue(new Error('상품 API 실패'));

    const { result } = renderHook(() => useProducts());

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.products).toEqual([]);
    expect(result.current.error).toBe('상품 API 실패');
  });
});

describe('useProduct', () => {
  it('fetches product detail for the supplied id', async () => {
    mockProductsApi.getById.mockResolvedValue(sampleProductDetail);

    const { result } = renderHook(() => useProduct(1));

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(mockProductsApi.getById).toHaveBeenCalledWith(1);
    expect(result.current.product).toEqual(sampleProductDetail);
    expect(result.current.error).toBeNull();
  });

  it('updates error state when detail loading fails', async () => {
    mockProductsApi.getById.mockRejectedValue(new Error('상품을 찾을 수 없습니다'));

    const { result } = renderHook(() => useProduct(404));

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.product).toBeNull();
    expect(result.current.error).toBe('상품을 찾을 수 없습니다');
  });
});
