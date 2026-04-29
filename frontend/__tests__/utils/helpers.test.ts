import { FALLBACK_IMAGE, getProductImageUrl } from '@/components/products/ProductCard';

describe('product image helpers', () => {
  it('returns the shared fallback image for missing product images', () => {
    expect(getProductImageUrl(null)).toBe(FALLBACK_IMAGE);
  });

  it('keeps absolute image URLs unchanged', () => {
    expect(getProductImageUrl('https://cdn.example.com/product.png')).toBe('https://cdn.example.com/product.png');
  });

  it('prefixes backend-relative image URLs with the configured API base URL', () => {
    expect(getProductImageUrl('/uploads/product.png')).toBe('http://localhost/uploads/product.png');
  });
});
