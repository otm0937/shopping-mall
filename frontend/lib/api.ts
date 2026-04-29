/**
 * API Client Module
 * 
 * 쇼핑몰 백엔드 API와 통신하는 클라이언트 모듈입니다.
 * JWT 기반 인증을 지원하며, 모든 API 요청에 자동으로 토큰을 첨부합니다.
 * 
 * Features:
 * - 자동 JWT 토큰 첨부
 * - 에러 처리 및 표준화
 * - FormData 지원 (이미지 업로드)
 * - TypeScript 타입 지원
 * 
 * @module lib/api
 */

import type {
  AddToCartRequest,
  AuthResponse,
  CartResponse,
  LoginRequest,
  PaymentHistoryResponse,
  PaymentRequest,
  PaymentResponse,
  ProductDetailResponse,
  ProductListResponse,
  ProductResponse,
  SignupRequest,
} from '@/types';

/** API 기본 URL (환경 변수에서 설정) */
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? '';

/** JWT 토큰 저장 키 */
const TOKEN_STORAGE_KEY = 'token';

/** API 요청 옵션 타입 */
type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: unknown;
  /** 인증 필요 여부 (기본값: true) */
  auth?: boolean;
};

/**
 * API 에러 클래스
 * 
 * HTTP 상태 코드와 에러 메시지를 포함하는 커스텀 에러 클래스입니다.
 */
export class ApiError extends Error {
  /** HTTP 상태 코드 */
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/**
 * 토큰 저장소
 * 
 * JWT 토큰을 localStorage에 안전하게 저장하고 관리합니다.
 * SSR 환경에서도 안전하게 동작하도록 window 객체 체크를 포함합니다.
 */
export const tokenStorage = {
  /**
   * 저장된 토큰 조회
   * @returns 저장된 토큰 또는 null
   */
  get() {
    if (typeof window === 'undefined') {
      return null;
    }

    return window.localStorage.getItem(TOKEN_STORAGE_KEY);
  },
  
  /**
   * 토큰 저장
   * @param token JWT 토큰 문자열
   */
  set(token: string) {
    window.localStorage.setItem(TOKEN_STORAGE_KEY, token);
  },
  
  /**
   * 토큰 삭제
   */
  remove() {
    window.localStorage.removeItem(TOKEN_STORAGE_KEY);
  },
};

/**
 * 기본 API Fetch 함수
 * 
 * 모든 API 요청의 기반이 되는 함수입니다.
 * 자동으로 인증 헤더를 추가하고, JSON 직렬화 및 에러 처리를 수행합니다.
 * 
 * @param path API 경로 (예: '/api/auth/login')
 * @param options 요청 옵션
 * @returns API 응답 데이터
 * @throws ApiError API 요청 실패 시
 */
async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { body, auth = true, headers, ...init } = options;
  const requestHeaders = new Headers(headers);
  const isFormData = body instanceof FormData;

  // JSON Content-Type 설정 (FormData가 아닌 경우)
  if (body !== undefined && !isFormData) {
    requestHeaders.set('Content-Type', 'application/json');
  }

  // 인증 헤더 추가
  if (auth) {
    const token = tokenStorage.get();

    if (token) {
      requestHeaders.set('Authorization', `Bearer ${token}`);
    }
  }

  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: requestHeaders,
      body: body === undefined ? undefined : isFormData ? body : JSON.stringify(body),
    });
  } catch {
    throw new ApiError('서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.', 0);
  }

  const contentType = response.headers.get('content-type');
  const hasJsonBody = contentType?.includes('application/json');
  const data = hasJsonBody ? await response.json() : null;

  if (!response.ok) {
    const message =
      typeof data?.message === 'string'
        ? data.message
        : '요청을 처리하지 못했습니다. 다시 시도해 주세요.';

    throw new ApiError(message, response.status);
  }

  return data as T;
}

/**
 * 인증 API
 * 
 * 사용자 인증 관련 API 호출을 담당합니다.
 * - 로그인
 * - 회원가입
 * - 로그아웃
 */
export const authApi = {
  /**
   * 사용자 로그인
   * @param request 로그인 요청 데이터 (username, password)
   * @returns 인증 응답 (토큰, 사용자 정보)
   */
  login(request: LoginRequest) {
    return apiFetch<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: request,
      auth: false,
    });
  },
  
  /**
   * 사용자 회원가입
   * @param request 회원가입 요청 데이터
   * @returns 인증 응답
   */
  signup(request: SignupRequest) {
    return apiFetch<AuthResponse>('/api/auth/signup', {
      method: 'POST',
      body: request,
      auth: false,
    });
  },
  
  /**
   * 사용자 로그아웃
   * @returns void
   */
  logout() {
    return apiFetch<void>('/api/auth/logout', {
      method: 'POST',
    });
  },
};

/**
 * 상품 API
 * 
 * 상품 조회 관련 API 호출을 담당합니다.
 * - 상품 목록 조회
 * - 상품 상세 조회
 */
export const productsApi = {
  /**
   * 전체 상품 목록 조회
   * @returns 상품 목록
   */
  getAll() {
    return apiFetch<ProductListResponse[]>('/api/products', {
      method: 'GET',
      auth: false,
    });
  },
  
  /**
   * 특정 상품 상세 조회
   * @param id 상품 ID
   * @returns 상품 상세 정보
   */
  getById(id: number) {
    return apiFetch<ProductDetailResponse>(`/api/products/${id}`, {
      method: 'GET',
      auth: false,
    });
  },
};

/**
 * 관리자 API
 * 
 * 관리자 전용 상품 관리 API 호출을 담당합니다.
 * - 상품 등록 (이미지 업로드 지원)
 * - 상품 수정
 * - 상품 삭제
 */
export const adminApi = {
  /**
   * 상품 등록
   * @param formData 상품 데이터 (이미지 포함)
   * @returns 등록된 상품 정보
   */
  createProduct(formData: FormData) {
    return apiFetch<ProductResponse>('/api/admin/products', {
      method: 'POST',
      body: formData,
    });
  },
  
  /**
   * 상품 수정
   * @param id 상품 ID
   * @param formData 수정할 상품 데이터
   * @returns 수정된 상품 정보
   */
  updateProduct(id: number, formData: FormData) {
    return apiFetch<ProductResponse>(`/api/admin/products/${id}`, {
      method: 'PUT',
      body: formData,
    });
  },
  
  /**
   * 상품 삭제
   * @param id 상품 ID
   */
  deleteProduct(id: number) {
    return apiFetch<void>(`/api/admin/products/${id}`, {
      method: 'DELETE',
    });
  },
};

/**
 * 장바구니 API
 * 
 * 장바구니 관련 API 호출을 담당합니다.
 * - 장바구니 조회
 * - 상품 추가
 * - 상품 제거
 */
export const cartApi = {
  /**
   * 현재 사용자의 장바구니 조회
   * @returns 장바구니 정보
   */
  getCart() {
    return apiFetch<CartResponse>('/api/cart', {
      method: 'GET',
    });
  },
  
  /**
   * 장바구니에 상품 추가
   * @param productId 상품 ID
   * @param quantity 수량
   * @returns 업데이트된 장바구니 정보
   */
  addToCart(productId: number, quantity: number) {
    const request: AddToCartRequest = { productId, quantity };

    return apiFetch<CartResponse>('/api/cart', {
      method: 'POST',
      body: request,
    });
  },
  
  /**
   * 장바구니에서 상품 제거
   * @param cartItemId 장바구니 아이템 ID
   * @returns 업데이트된 장바구니 정보
   */
  removeFromCart(cartItemId: number) {
    return apiFetch<CartResponse>(`/api/cart/items/${cartItemId}`, {
      method: 'DELETE',
    });
  },
};

/**
 * 결제 API
 * 
 * 결제 관련 API 호출을 담당합니다.
 * - 결제 처리 (Mock)
 * - 결제 내역 조회
 * - 결제 상세 조회
 */
export const paymentsApi = {
  /**
   * 결제 처리
   * @returns 결제 결과
   */
  processPayment() {
    const request: PaymentRequest = { paymentMethod: 'MOCK' };

    return apiFetch<PaymentResponse>('/api/payments', {
      method: 'POST',
      body: request,
    });
  },
  
  /**
   * 결제 내역 목록 조회
   * @returns 결제 내역 목록
   */
  getHistory() {
    return apiFetch<PaymentHistoryResponse[]>('/api/payments', {
      method: 'GET',
    });
  },
  
  /**
   * 특정 결제 상세 조회
   * @param paymentId 결제 ID
   * @returns 결제 상세 정보
   */
  getDetail(paymentId: number) {
    return apiFetch<PaymentResponse>(`/api/payments/${paymentId}`, {
      method: 'GET',
    });
  },
};

export { API_BASE_URL, TOKEN_STORAGE_KEY };
