import { render, screen } from '@testing-library/react';
import HomePage from '@/app/page';
import { useProducts } from '@/hooks/useProducts';
import { sampleProduct } from '../test-utils';

jest.mock('@/hooks/useProducts', () => ({
  useProducts: jest.fn(),
}));

const mockUseProducts = useProducts as jest.Mock;

describe('HomePage', () => {
  it('renders the product listing when data is loaded', () => {
    mockUseProducts.mockReturnValue({ error: null, isLoading: false, products: [sampleProduct] });

    render(<HomePage />);

    // Home owns the title and loading/error branching while ProductGrid owns individual cards.
    expect(screen.getByRole('heading', { name: '전체 상품' })).toBeInTheDocument();
    expect(screen.getByText('다양한 상품을 둘러보고 원하는 것을 찾아보세요.')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /테스트 카메라/i })).toHaveAttribute('href', '/products/1');
  });

  it('renders skeleton placeholders while loading', () => {
    mockUseProducts.mockReturnValue({ error: null, isLoading: true, products: [] });

    render(<HomePage />);

    expect(screen.getAllByTestId('skeleton')).toHaveLength(24);
  });

  it('renders fetch errors and still shows the empty product state after loading', () => {
    mockUseProducts.mockReturnValue({ error: '상품 API 실패', isLoading: false, products: [] });

    render(<HomePage />);

    expect(screen.getByText('상품 API 실패')).toBeInTheDocument();
    expect(screen.getByText('등록된 상품이 없습니다')).toBeInTheDocument();
  });
});
