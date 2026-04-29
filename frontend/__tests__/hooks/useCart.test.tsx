import type { ReactNode } from 'react';
import { act, renderHook, waitFor } from '@testing-library/react';
import { CartProvider, useCart } from '@/hooks/useCart';
import { useAuth } from '@/hooks/useAuth';
import { cartApi } from '@/lib/api';
import { sampleCart, sampleCartItem } from '../test-utils';

jest.mock('@/hooks/useAuth', () => ({
  useAuth: jest.fn(),
}));

jest.mock('@/lib/api', () => ({
  ApiError: class ApiError extends Error {
    status: number;

    constructor(message: string, status: number) {
      super(message);
      this.status = status;
    }
  },
  cartApi: {
    addToCart: jest.fn(),
    getCart: jest.fn(),
    removeFromCart: jest.fn(),
  },
}));

const mockUseAuth = useAuth as jest.Mock;
const mockCartApi = cartApi as jest.Mocked<typeof cartApi>;
const wrapper = ({ children }: { children: ReactNode }) => <CartProvider>{children}</CartProvider>;

describe('useCart', () => {
  it('does not fetch and returns null refresh data when unauthenticated', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isLoading: false });

    const { result } = renderHook(() => useCart(), { wrapper });

    await act(async () => {
      await expect(result.current.refreshCart()).resolves.toBeNull();
    });

    expect(mockCartApi.getCart).not.toHaveBeenCalled();
    expect(result.current.items).toEqual([]);
    expect(result.current.totalPrice).toBe(0);
  });

  it('loads the current cart when authentication is ready', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, isLoading: false });
    mockCartApi.getCart.mockResolvedValue(sampleCart);

    const { result } = renderHook(() => useCart(), { wrapper });

    // The provider automatically refreshes the cart once the user is authenticated.
    await waitFor(() => expect(result.current.items).toEqual(sampleCart.items));

    expect(result.current.itemCount).toBe(2);
    expect(result.current.totalPrice).toBe(258000);
  });

  it('adds an item and applies the returned cart', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isLoading: true });
    mockCartApi.addToCart.mockResolvedValue(sampleCart);

    const { result } = renderHook(() => useCart(), { wrapper });

    await act(async () => {
      await result.current.addToCart(1, 2);
    });

    expect(mockCartApi.addToCart).toHaveBeenCalledWith(1, 2);
    expect(result.current.items).toEqual(sampleCart.items);
    expect(result.current.error).toBeNull();
  });

  it('removes an item optimistically and keeps the API result', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, isLoading: false });
    mockCartApi.getCart.mockResolvedValue(sampleCart);
    mockCartApi.removeFromCart.mockResolvedValue({ ...sampleCart, items: [], totalPrice: 0 });

    const { result } = renderHook(() => useCart(), { wrapper });
    await waitFor(() => expect(result.current.items).toHaveLength(1));

    await act(async () => {
      await result.current.removeFromCart(sampleCartItem.id);
    });

    expect(mockCartApi.removeFromCart).toHaveBeenCalledWith(10);
    expect(result.current.items).toEqual([]);
    expect(result.current.totalPrice).toBe(0);
  });

  it('rolls back optimistic removal and exposes the failure message', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, isLoading: false });
    mockCartApi.getCart.mockResolvedValue(sampleCart);
    mockCartApi.removeFromCart.mockRejectedValue(new Error('삭제 실패'));

    const { result } = renderHook(() => useCart(), { wrapper });
    await waitFor(() => expect(result.current.items).toHaveLength(1));

    await act(async () => {
      await expect(result.current.removeFromCart(sampleCartItem.id)).rejects.toThrow('삭제 실패');
    });

    expect(result.current.items).toEqual(sampleCart.items);
    expect(result.current.totalPrice).toBe(258000);
    expect(result.current.error).toBe('삭제 실패');
  });

  it('throws a helpful error when used outside CartProvider', () => {
    const consoleError = jest.spyOn(console, 'error').mockImplementation(() => undefined);

    expect(() => renderHook(() => useCart())).toThrow('useCart must be used within a CartProvider.');

    consoleError.mockRestore();
  });
});
