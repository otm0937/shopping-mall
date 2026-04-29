import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CartItem from '@/components/cart/CartItem';
import { sampleCartItem } from '../test-utils';

describe('CartItem', () => {
  it('renders cart item details and calls onRemove with the cart item id', async () => {
    const user = userEvent.setup();
    const onRemove = jest.fn();

    render(<CartItem item={sampleCartItem} onRemove={onRemove} />);

    // Pricing is displayed in both unit and line-total form so shoppers can verify quantity math.
    expect(screen.getByRole('link', { name: '테스트 카메라' })).toHaveAttribute('href', '/products/1');
    expect(screen.getByText('단가 ₩129,000')).toBeInTheDocument();
    expect(screen.getByText('수량 2개')).toBeInTheDocument();
    expect(screen.getByText('₩258,000')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '삭제' }));

    expect(onRemove).toHaveBeenCalledWith(10);
  });

  it('disables the remove button while deletion is in progress', () => {
    render(<CartItem item={sampleCartItem} onRemove={jest.fn()} isRemoving />);

    expect(screen.getByRole('button', { name: '삭제 중...' })).toBeDisabled();
  });
});
