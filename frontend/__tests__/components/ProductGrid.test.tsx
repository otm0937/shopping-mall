import { render, screen } from '@testing-library/react';
import ProductGrid from '@/components/products/ProductGrid';
import { sampleProduct, secondProduct } from '../test-utils';

describe('ProductGrid', () => {
  it('renders every product card in the responsive grid', () => {
    render(<ProductGrid products={[sampleProduct, secondProduct]} />);

    // The grid delegates each item to ProductCard while preserving one link per product.
    expect(screen.getByRole('link', { name: /테스트 카메라/i })).toHaveAttribute('href', '/products/1');
    expect(screen.getByRole('link', { name: /무선 키보드/i })).toHaveAttribute('href', '/products/2');
  });

  it('shows the empty state when there are no products', () => {
    render(<ProductGrid products={[]} />);

    expect(screen.getByText('등록된 상품이 없습니다')).toBeInTheDocument();
    expect(screen.getByText('아직 판매할 상품이 준비되지 않았습니다.')).toBeInTheDocument();
  });
});
