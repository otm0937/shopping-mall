import type { CartItemResponse, CartResponse, PaymentResponse, ProductDetailResponse, ProductListResponse } from '@/types';

export const sampleProduct: ProductListResponse = {
  id: 1,
  imageUrl: '/uploads/camera.jpg',
  name: '테스트 카메라',
  price: 129000,
};

export const secondProduct: ProductListResponse = {
  id: 2,
  imageUrl: null,
  name: '무선 키보드',
  price: 89000,
};

export const sampleProductDetail: ProductDetailResponse = {
  ...sampleProduct,
  description: '선명한 사진을 위한 테스트 상품입니다.',
};

export const sampleCartItem: CartItemResponse = {
  id: 10,
  price: 129000,
  productId: 1,
  productName: '테스트 카메라',
  quantity: 2,
  totalPrice: 258000,
};

export const sampleCart: CartResponse = {
  id: 5,
  items: [sampleCartItem],
  totalPrice: 258000,
};

export const samplePayment: PaymentResponse = {
  createdAt: '2026-04-29T12:00:00',
  id: 77,
  items: [
    {
      id: 1,
      productId: sampleCartItem.productId,
      productName: sampleCartItem.productName,
      quantity: sampleCartItem.quantity,
      totalPrice: sampleCartItem.totalPrice,
      unitPrice: sampleCartItem.price,
    },
  ],
  message: '결제가 완료되었습니다.',
  status: 'COMPLETED',
  totalAmount: sampleCart.totalPrice,
};
