export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  password: string;
  name: string;
  email: string;
}

export interface AuthResponse {
  message: string;
  username?: string;
  name?: string;
  token?: string;
}

export interface User {
  username: string;
  name: string;
  role: 'ADMIN' | 'USER';
}

export interface ProductListResponse {
  id: number;
  name: string;
  price: number;
  imageUrl: string | null;
}

export interface ProductDetailResponse {
  id: number;
  name: string;
  price: number;
  description: string;
  imageUrl: string | null;
}

export interface ProductResponse extends ProductDetailResponse {
  message: string;
}

export interface CartItemResponse {
  id: number;
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  totalPrice: number;
}

export interface CartResponse {
  id: number;
  items: CartItemResponse[];
  totalPrice: number;
  message?: string | null;
}

export interface AddToCartRequest {
  productId: number;
  quantity: number;
}

export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';

export interface PaymentItemResponse {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface PaymentResponse {
  id: number;
  totalAmount: number;
  status: PaymentStatus;
  items: PaymentItemResponse[];
  createdAt: string;
  message: string;
}

export interface PaymentHistoryResponse {
  id: number;
  totalAmount: number;
  totalQuantity: number;
  status: PaymentStatus;
  createdAt: string;
}

export interface PaymentRequest {
  paymentMethod: string;
}
