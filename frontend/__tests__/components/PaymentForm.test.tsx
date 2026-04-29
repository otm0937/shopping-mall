import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import PaymentForm from '@/components/payment/PaymentForm';
import { paymentsApi } from '@/lib/api';
import { useCart } from '@/hooks/useCart';
import { sampleCart, samplePayment } from '../test-utils';

jest.mock('@/hooks/useCart', () => ({
  useCart: jest.fn(),
}));

jest.mock('@/lib/api', () => ({
  paymentsApi: {
    processPayment: jest.fn(),
  },
}));

const mockUseCart = useCart as jest.Mock;
const mockProcessPayment = paymentsApi.processPayment as jest.Mock;

describe('PaymentForm', () => {
  it('renders payment method, cart summary, and total amount', () => {
    mockUseCart.mockReturnValue({
      items: sampleCart.items,
      refreshCart: jest.fn(),
      totalPrice: sampleCart.totalPrice,
    });

    render(<PaymentForm />);

    // The current app intentionally supports only the mock payment method.
    expect(screen.getByLabelText('MOCK 결제')).toBeChecked();
    expect(screen.getByText('테스트 카메라')).toBeInTheDocument();
    expect(screen.getByText('2개')).toBeInTheDocument();
    expect(screen.getAllByText('₩258,000')).toHaveLength(2);
    expect(screen.getByRole('button', { name: 'Complete Payment' })).toBeEnabled();
  });

  it('disables payment when the cart is empty', () => {
    mockUseCart.mockReturnValue({ items: [], refreshCart: jest.fn(), totalPrice: 0 });

    render(<PaymentForm />);

    expect(screen.getByRole('button', { name: 'Complete Payment' })).toBeDisabled();
  });

  it('processes payment, refreshes the cart, and links to payment detail', async () => {
    const user = userEvent.setup();
    const refreshCart = jest.fn().mockResolvedValue(sampleCart);

    mockUseCart.mockReturnValue({
      items: sampleCart.items,
      refreshCart,
      totalPrice: sampleCart.totalPrice,
    });
    mockProcessPayment.mockResolvedValue(samplePayment);

    render(<PaymentForm />);

    await user.click(screen.getByRole('button', { name: 'Complete Payment' }));

    await waitFor(() => expect(mockProcessPayment).toHaveBeenCalledTimes(1));
    expect(refreshCart).toHaveBeenCalledTimes(1);
    expect(screen.getByText('결제가 완료되었습니다.')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: '결제 상세 보기' })).toHaveAttribute('href', '/payments/77');
  });

  it('shows a payment error message when processing fails', async () => {
    const user = userEvent.setup();

    mockUseCart.mockReturnValue({
      items: sampleCart.items,
      refreshCart: jest.fn(),
      totalPrice: sampleCart.totalPrice,
    });
    mockProcessPayment.mockRejectedValue(new Error('결제 승인 실패'));

    render(<PaymentForm />);

    await user.click(screen.getByRole('button', { name: 'Complete Payment' }));

    expect(await screen.findByText('결제 승인 실패')).toBeInTheDocument();
  });
});
