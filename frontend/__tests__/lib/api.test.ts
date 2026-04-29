import { http, HttpResponse } from 'msw';
import { server } from '../mocks/server';
import { adminApi, ApiError, authApi, cartApi, productsApi, tokenStorage } from '@/lib/api';
import { sampleCart, sampleProduct } from '../test-utils';

describe('api client', () => {
  beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
  afterEach(() => server.resetHandlers());
  afterAll(() => server.close());

  it('fetches public product data without an Authorization header', async () => {
    server.use(
      http.get('http://localhost/api/products', ({ request }) => {
        // Product listing is public, so no bearer token should be sent.
        expect(request.headers.get('authorization')).toBeNull();
        return HttpResponse.json([sampleProduct]);
      }),
    );

    await expect(productsApi.getAll()).resolves.toEqual([sampleProduct]);
  });

  it('posts login credentials as JSON and stores no auth header for login', async () => {
    server.use(
      http.post('http://localhost/api/auth/login', async ({ request }) => {
        const body = (await request.json()) as { password: string; username: string };

        expect(request.headers.get('authorization')).toBeNull();
        expect(request.headers.get('content-type')).toContain('application/json');
        expect(body).toEqual({ password: 'root', username: 'root' });

        return HttpResponse.json({ message: 'ok', name: '관리자', token: 'jwt', username: 'root' });
      }),
    );

    await expect(authApi.login({ password: 'root', username: 'root' })).resolves.toEqual({
      message: 'ok',
      name: '관리자',
      token: 'jwt',
      username: 'root',
    });
  });

  it('adds bearer tokens to authenticated requests', async () => {
    tokenStorage.set('stored-jwt');
    server.use(
      http.get('http://localhost/api/cart', ({ request }) => {
        expect(request.headers.get('authorization')).toBe('Bearer stored-jwt');
        return HttpResponse.json(sampleCart);
      }),
    );

    await expect(cartApi.getCart()).resolves.toEqual(sampleCart);
  });

  it('submits FormData without forcing JSON content type', async () => {
    tokenStorage.set('admin-jwt');
    const formData = new FormData();
    formData.append('name', '테스트 카메라');
    formData.append('price', '129000');
    formData.append('description', '관리자 상품 등록');

    server.use(
      http.post('http://localhost/api/admin/products', async ({ request }) => {
        const body = await request.formData();

        expect(request.headers.get('authorization')).toBe('Bearer admin-jwt');
        expect(request.headers.get('content-type')).toContain('multipart/form-data');
        expect(body.get('name')).toBe('테스트 카메라');
        expect(body.get('price')).toBe('129000');

        return HttpResponse.json({ ...sampleProduct, description: '관리자 상품 등록', message: 'created' });
      }),
    );

    await expect(adminApi.createProduct(formData)).resolves.toMatchObject({ id: sampleProduct.id, message: 'created' });
  });

  it('normalizes backend JSON error messages into ApiError', async () => {
    server.use(
      http.get('http://localhost/api/products/404', () =>
        HttpResponse.json({ message: '상품을 찾을 수 없습니다.' }, { status: 404 }),
      ),
    );

    await expect(productsApi.getById(404)).rejects.toMatchObject({
      message: '상품을 찾을 수 없습니다.',
      name: 'ApiError',
      status: 404,
    });
  });

  it('normalizes network failures into an ApiError with status 0', async () => {
    server.use(http.get('http://localhost/api/products', () => HttpResponse.error()));

    await expect(productsApi.getAll()).rejects.toEqual(
      new ApiError('서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.', 0),
    );
  });
});
