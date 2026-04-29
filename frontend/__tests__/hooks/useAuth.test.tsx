import type { ReactNode } from 'react';
import { act, renderHook, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from '@/hooks/useAuth';
import { authApi, tokenStorage } from '@/lib/api';

jest.mock('@/lib/api', () => ({
  authApi: {
    login: jest.fn(),
    logout: jest.fn(),
    signup: jest.fn(),
  },
  tokenStorage: {
    get: jest.fn(),
    remove: jest.fn(),
    set: jest.fn(),
  },
}));

const mockAuthApi = authApi as jest.Mocked<typeof authApi>;
const mockTokenStorage = tokenStorage as jest.Mocked<typeof tokenStorage>;
const wrapper = ({ children }: { children: ReactNode }) => <AuthProvider>{children}</AuthProvider>;

describe('useAuth', () => {
  beforeEach(() => {
    mockTokenStorage.get.mockReturnValue(null);
  });

  it('starts unauthenticated when no token exists', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    // AuthProvider defers localStorage reads to a microtask to avoid hydration mismatches.
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.isAdmin).toBe(false);
  });

  it('hydrates the stored user when a token and user record are available', async () => {
    mockTokenStorage.get.mockReturnValue('stored-token');
    window.localStorage.setItem('user', JSON.stringify({ name: '홍길동', username: 'hong' }));

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.isAuthenticated).toBe(true));

    expect(result.current.user).toEqual({ name: '홍길동', role: 'USER', username: 'hong' });
  });

  it('logs in, stores token and user data, and derives admin state', async () => {
    mockAuthApi.login.mockResolvedValue({ message: 'ok', name: '관리자', token: 'jwt-token', username: 'root' });

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await act(async () => {
      await result.current.login({ password: 'root', username: 'root' });
    });

    expect(mockAuthApi.login).toHaveBeenCalledWith({ password: 'root', username: 'root' });
    expect(mockTokenStorage.set).toHaveBeenCalledWith('jwt-token');
    expect(JSON.parse(window.localStorage.getItem('user') ?? '{}')).toEqual({
      name: '관리자',
      role: 'ADMIN',
      username: 'root',
    });
    expect(result.current.isAdmin).toBe(true);
  });

  it('rejects login responses that do not include a token', async () => {
    mockAuthApi.login.mockResolvedValue({ message: 'missing token', username: 'hong' });

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await expect(result.current.login({ password: 'pw', username: 'hong' })).rejects.toThrow(
      '로그인 응답에 인증 토큰이 없습니다.',
    );
    expect(mockTokenStorage.set).not.toHaveBeenCalled();
  });

  it('logs out from the backend when a token exists and clears local state', async () => {
    mockTokenStorage.get.mockReturnValue('stored-token');
    window.localStorage.setItem('user', JSON.stringify({ name: '홍길동', username: 'hong' }));
    mockAuthApi.logout.mockResolvedValue(undefined);

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isAuthenticated).toBe(true));

    await act(async () => {
      await result.current.logout();
    });

    expect(mockAuthApi.logout).toHaveBeenCalledTimes(1);
    expect(mockTokenStorage.remove).toHaveBeenCalledTimes(1);
    expect(window.localStorage.getItem('user')).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
  });

  it('throws a helpful error when used outside AuthProvider', () => {
    const consoleError = jest.spyOn(console, 'error').mockImplementation(() => undefined);

    expect(() => renderHook(() => useAuth())).toThrow('useAuth must be used within an AuthProvider.');

    consoleError.mockRestore();
  });
});
