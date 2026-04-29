import { TOKEN_STORAGE_KEY, tokenStorage } from '@/lib/api';

describe('tokenStorage', () => {
  it('stores and retrieves the JWT token from localStorage', () => {
    tokenStorage.set('jwt-token');

    expect(window.localStorage.getItem(TOKEN_STORAGE_KEY)).toBe('jwt-token');
    expect(tokenStorage.get()).toBe('jwt-token');
  });

  it('removes the JWT token from localStorage', () => {
    window.localStorage.setItem(TOKEN_STORAGE_KEY, 'jwt-token');

    tokenStorage.remove();

    expect(tokenStorage.get()).toBeNull();
  });
});
