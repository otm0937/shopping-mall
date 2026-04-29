import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { usePathname, useRouter } from 'next/navigation';
import Header from '@/components/layout/Header';
import { useAuth } from '@/hooks/useAuth';
import { useCart } from '@/hooks/useCart';

jest.mock('@/hooks/useAuth', () => ({
  useAuth: jest.fn(),
}));

jest.mock('@/hooks/useCart', () => ({
  useCart: jest.fn(),
}));

const mockUseAuth = useAuth as jest.Mock;
const mockUseCart = useCart as jest.Mock;
const mockUsePathname = usePathname as jest.Mock;
const mockUseRouter = useRouter as jest.Mock;

describe('Header', () => {
  beforeEach(() => {
    mockUsePathname.mockReturnValue('/');
    mockUseRouter.mockReturnValue({ replace: jest.fn() });
    mockUseCart.mockReturnValue({ clearCart: jest.fn(), itemCount: 0 });
  });

  it('renders public navigation for unauthenticated users', () => {
    mockUseAuth.mockReturnValue({
      isAdmin: false,
      isAuthenticated: false,
      isLoading: false,
      logout: jest.fn(),
      user: null,
    });

    render(<Header />);

    // Visitors only see entry points into authentication, not cart or payment links.
    expect(screen.getByRole('link', { name: 'Shopping Mall' })).toHaveAttribute('href', '/');
    expect(screen.getByRole('link', { name: 'Login' })).toHaveAttribute('href', '/login');
    expect(screen.getByRole('link', { name: 'Signup' })).toHaveAttribute('href', '/signup');
    expect(screen.queryByText('Payments')).not.toBeInTheDocument();
  });

  it('renders authenticated navigation, cart count, and logs out cleanly', async () => {
    const user = userEvent.setup();
    const logout = jest.fn().mockResolvedValue(undefined);
    const clearCart = jest.fn();
    const replace = jest.fn();

    mockUseRouter.mockReturnValue({ replace });
    mockUseAuth.mockReturnValue({
      isAdmin: false,
      isAuthenticated: true,
      isLoading: false,
      logout,
      user: { name: '홍길동', role: 'USER', username: 'hong' },
    });
    mockUseCart.mockReturnValue({ clearCart, itemCount: 3 });

    render(<Header />);

    expect(screen.getByText('홍길동')).toBeInTheDocument();
    expect(screen.getAllByText('Payments')).toHaveLength(2);
    expect(screen.getAllByText('3')).toHaveLength(2);

    await user.click(screen.getByRole('button', { name: 'Logout' }));

    await waitFor(() => expect(logout).toHaveBeenCalledTimes(1));
    expect(clearCart).toHaveBeenCalledTimes(1);
    expect(replace).toHaveBeenCalledWith('/login');
  });

  it('shows admin links only for admin users and loading text while auth is pending', () => {
    mockUseAuth.mockReturnValue({
      isAdmin: true,
      isAuthenticated: true,
      isLoading: false,
      logout: jest.fn(),
      user: { name: '관리자', role: 'ADMIN', username: 'root' },
    });

    const { rerender } = render(<Header />);

    expect(screen.getAllByText('Admin')).toHaveLength(2);

    mockUseAuth.mockReturnValue({
      isAdmin: false,
      isAuthenticated: false,
      isLoading: true,
      logout: jest.fn(),
      user: null,
    });
    rerender(<Header />);

    expect(screen.getByText('확인 중...')).toBeInTheDocument();
  });
});
