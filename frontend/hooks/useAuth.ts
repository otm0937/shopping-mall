'use client';

import {
  createContext,
  createElement,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { authApi, tokenStorage } from '@/lib/api';
import type { AuthResponse, LoginRequest, SignupRequest, User } from '@/types';

const USER_STORAGE_KEY = 'user';

interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (request: LoginRequest) => Promise<AuthResponse>;
  signup: (request: SignupRequest) => Promise<AuthResponse>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function createUser(username: string, name?: string): User {
  return {
    username,
    name: name || username,
    role: username === 'root' ? 'ADMIN' : 'USER',
  };
}

function readStoredUser(): User | null {
  const rawUser = window.localStorage.getItem(USER_STORAGE_KEY);

  if (!rawUser) {
    return null;
  }

  try {
    const parsed = JSON.parse(rawUser) as Partial<User>;

    if (!parsed.username) {
      return null;
    }

    return createUser(parsed.username, parsed.name);
  } catch {
    window.localStorage.removeItem(USER_STORAGE_KEY);
    return null;
  }
}

function persistUser(user: User) {
  window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
}

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    queueMicrotask(() => {
      const token = tokenStorage.get();
      const storedUser = readStoredUser();

      if (token && storedUser) {
        setUser(storedUser);
      } else if (!token) {
        window.localStorage.removeItem(USER_STORAGE_KEY);
      }

      setIsLoading(false);
    });
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await authApi.login(request);

    if (!response.token) {
      throw new Error('로그인 응답에 인증 토큰이 없습니다.');
    }

    const nextUser = createUser(response.username || request.username, response.name);

    tokenStorage.set(response.token);
    persistUser(nextUser);
    setUser(nextUser);

    return response;
  }, []);

  const signup = useCallback(async (request: SignupRequest) => {
    return authApi.signup(request);
  }, []);

  const logout = useCallback(async () => {
    try {
      if (tokenStorage.get()) {
        await authApi.logout();
      }
    } finally {
      tokenStorage.remove();
      window.localStorage.removeItem(USER_STORAGE_KEY);
      setUser(null);
    }
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isLoading,
      isAuthenticated: Boolean(user),
      isAdmin: user?.username === 'root',
      login,
      signup,
      logout,
    }),
    [isLoading, login, logout, signup, user],
  );

  return createElement(AuthContext.Provider, { value }, children);
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider.');
  }

  return context;
}
