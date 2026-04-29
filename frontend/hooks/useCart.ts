'use client';

import {
  createContext,
  createElement,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { ApiError, cartApi } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import type { CartItemResponse, CartResponse } from '@/types';

interface CartContextValue {
  items: CartItemResponse[];
  totalPrice: number;
  itemCount: number;
  isLoading: boolean;
  error: string | null;
  addToCart: (productId: number, quantity?: number) => Promise<CartResponse>;
  removeFromCart: (cartItemId: number) => Promise<CartResponse>;
  refreshCart: () => Promise<CartResponse | null>;
  clearCart: () => void;
}

interface CartProviderProps {
  children: ReactNode;
}

const CartContext = createContext<CartContextValue | undefined>(undefined);

function getErrorMessage(error: unknown, fallback: string): string {
  return error instanceof ApiError || error instanceof Error ? error.message : fallback;
}

export function CartProvider({ children }: CartProviderProps) {
  const { isAuthenticated, isLoading: isAuthLoading } = useAuth();
  const [items, setItems] = useState<CartItemResponse[]>([]);
  const [totalPrice, setTotalPrice] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const applyCart = useCallback((cart: CartResponse) => {
    setItems(cart.items);
    setTotalPrice(cart.totalPrice);
  }, []);

  const clearCart = useCallback(() => {
    setItems([]);
    setTotalPrice(0);
    setError(null);
  }, []);

  const refreshCart = useCallback(async () => {
    if (!isAuthenticated) {
      clearCart();
      return null;
    }

    setIsLoading(true);
    setError(null);

    try {
      const cart = await cartApi.getCart();
      applyCart(cart);
      return cart;
    } catch (caughtError) {
      setError(getErrorMessage(caughtError, '장바구니를 불러오지 못했습니다.'));
      throw caughtError;
    } finally {
      setIsLoading(false);
    }
  }, [applyCart, clearCart, isAuthenticated]);

  const addToCart = useCallback(
    async (productId: number, quantity = 1) => {
      setIsLoading(true);
      setError(null);

      try {
        const cart = await cartApi.addToCart(productId, quantity);
        applyCart(cart);
        return cart;
      } catch (caughtError) {
        setError(getErrorMessage(caughtError, '상품을 장바구니에 담지 못했습니다.'));
        throw caughtError;
      } finally {
        setIsLoading(false);
      }
    },
    [applyCart],
  );

  const removeFromCart = useCallback(
    async (cartItemId: number) => {
      const previousItems = items;
      const previousTotalPrice = totalPrice;
      const nextItems = items.filter((item) => item.id !== cartItemId);

      setItems(nextItems);
      setTotalPrice(nextItems.reduce((sum, item) => sum + item.totalPrice, 0));
      setIsLoading(true);
      setError(null);

      try {
        const cart = await cartApi.removeFromCart(cartItemId);
        applyCart(cart);
        return cart;
      } catch (caughtError) {
        setItems(previousItems);
        setTotalPrice(previousTotalPrice);
        setError(getErrorMessage(caughtError, '장바구니 상품을 삭제하지 못했습니다.'));
        throw caughtError;
      } finally {
        setIsLoading(false);
      }
    },
    [applyCart, items, totalPrice],
  );

  useEffect(() => {
    if (isAuthLoading) {
      return;
    }

    if (!isAuthenticated) {
      queueMicrotask(clearCart);
      return;
    }

    queueMicrotask(() => {
      refreshCart().catch(() => undefined);
    });
  }, [clearCart, isAuthenticated, isAuthLoading, refreshCart]);

  const value = useMemo<CartContextValue>(
    () => ({
      items,
      totalPrice,
      itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
      isLoading,
      error,
      addToCart,
      removeFromCart,
      refreshCart,
      clearCart,
    }),
    [addToCart, clearCart, error, isLoading, items, refreshCart, removeFromCart, totalPrice],
  );

  return createElement(CartContext.Provider, { value }, children);
}

export function useCart() {
  const context = useContext(CartContext);

  if (!context) {
    throw new Error('useCart must be used within a CartProvider.');
  }

  return context;
}
