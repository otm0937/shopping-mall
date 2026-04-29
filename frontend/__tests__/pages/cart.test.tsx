import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useRouter } from 'next/navigation';
import CartPage from '@/app/cart/page';
import { useAuth } from '@/hooks/useAuth';
import { useCart } from '@/hooks/useCart';
import { sampleCart } from '../test-utils';

jest.mock('@/hooks/useAuth', () => ({
  useAuth: jest.fn(),
}));

jest.mock('@/hooks/useCart', () => ({
  useCart: jest.fn(),
}));

jest.mock('@/components/payment/PaymentForm', () => ({
  __esModule: true,
  default: () => <section>Payment form mock</section>,
}));

const mockUseAuth = useAuth as jest.Mock;
const mockUseCart = useCart as jest.Mock;
const mockUseRouter = useRouter as jest.Mock;

describe('CartPage', () => {
  beforeEach(() => {
    mockUseRouter.mockReturnValue({ replace: jest.fn() });
  });

  it('redirects unauthenticated users through AuthGuard', async () => {
    const replace = jest.fn();

    mockUseRouter.mockReturnValue({ replace });
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isLoading: false });
    mockUseCart.mockReturnValue({ error: null, isLoading: false, items: [], removeFromCart: jest.fn(), totalPrice: 0 });

    render(<CartPage />);

    await waitFor(() => expect(replace).toHaveBeenCalledWith('/login'));
    expect(screen.queryByRole('heading', { name: '장바구니' })).not.toBeInTheDocument();
  });

  it('renders the empty cart state for authenticated users', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, isLoading: false });
    mockUseCart.mockReturnValue({ error: null, isLoading: false, items: [], removeFromCart: jest.fn(), totalPrice: 0 });

    render(<CartPage />);

    expect(screen.getByRole('heading', { name: '장바구니' })).toBeInTheDocument();
    expect(screen.getByText('장바구니가 비어 있습니다')).toBeInTheDocument();
    expect(screen.getByText('마음에 드는 상품을 장바구니에 담아보세요.')).toBeInTheDocument();
  });

  it('renders cart items, order summary, and removes an item', async () => {
    const user = userEvent.setup();
    const removeFromCart = jest.fn().mockResolvedValue({ ...sampleCart, items: [], totalPrice: 0 });

    mockUseAuth.mockReturnValue({ isAuthenticated: true, isLoading: false });
    mockUseCart.mockReturnValue({
      error: null,
      isLoading: false,
      items: sampleCart.items,
      removeFromCart,
      totalPrice: sampleCart.totalPrice,
    });

    render(<CartPage />);

    expect(screen.getByText('상품 1종')).toBeInTheDocument();
    expect(screen.getByText('총 결제 금액')).toBeInTheDocument();
    expect(screen.getByText('Payment form mock')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '삭제' }));

    expect(removeFromCart).toHaveBeenCalledWith(10);
  });

  it('shows remove errors from failed cart item deletion', async () => {
    const user = userEvent.setup();

    mockUseAuth.mockReturnValue({ isAuthenticated: true, isLoading: false });
    mockUseCart.mockReturnValue({
      error: null,
      isLoading: false,
      items: sampleCart.items,
      removeFromCart: jest.fn().mockRejectedValue(new Error('상품을 삭제하지 못했습니다.')),
      totalPrice: sampleCart.totalPrice,
    });

    render(<CartPage />);

    await user.click(screen.getByRole('button', { name: '삭제' }));

    expect(await screen.findByText('상품을 삭제하지 못했습니다.')).toBeInTheDocument();
  });
});
