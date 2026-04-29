import { render, screen } from '@testing-library/react';
import ProductCard from '@/components/products/ProductCard';
import { sampleProduct, secondProduct } from '../test-utils';

describe('ProductCard', () => {
  it('renders product name, formatted price, image alt text, and detail link', () => {
    render(<ProductCard product={sampleProduct} />);

    // The card is the primary storefront entry point, so it should expose a real link.
    expect(screen.getByRole('link', { name: /테스트 카메라/i })).toHaveAttribute('href', '/products/1');
    expect(screen.getByRole('img', { name: '테스트 카메라' })).toHaveAttribute(
      'src',
      'http://localhost/uploads/camera.jpg',
    );
    expect(screen.getByText('₩129,000')).toBeInTheDocument();
  });

  it('uses the product placeholder image when imageUrl is missing', () => {
    render(<ProductCard product={secondProduct} />);

    // A missing backend image should never leave the UI with a broken image URL.
    expect(screen.getByRole('img', { name: '무선 키보드' })).toHaveAttribute('src', '/product-placeholder.svg');
    expect(screen.getByText('₩89,000')).toBeInTheDocument();
  });
});
