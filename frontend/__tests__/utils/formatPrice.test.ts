import { formatPrice } from '@/components/products/ProductCard';

describe('formatPrice', () => {
  it('formats zero and positive prices with Korean currency separators', () => {
    expect(formatPrice(0)).toBe('₩0');
    expect(formatPrice(129000)).toBe('₩129,000');
    expect(formatPrice(123456789)).toBe('₩123,456,789');
  });
});
