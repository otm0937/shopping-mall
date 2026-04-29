'use client';

import { FormEvent, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Button, Card, TextInput } from '@vapor-ui/core';
import { ApiError } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      await login({ username, password });
      router.replace('/');
    } catch (caughtError) {
      setError(
        caughtError instanceof ApiError || caughtError instanceof Error
          ? caughtError.message
          : '로그인 중 오류가 발생했습니다.',
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="flex flex-1 items-center justify-center bg-zinc-50 px-6 py-16 dark:bg-black">
      <Card.Root className="w-full max-w-md border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
        <Card.Header className="px-6 pt-6">
          <p className="text-sm font-medium text-blue-600 dark:text-blue-400">Shopping Mall</p>
          <h1 className="mt-2 text-2xl font-semibold text-zinc-950 dark:text-zinc-50">로그인</h1>
          <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
            계정으로 로그인하고 쇼핑을 계속하세요.
          </p>
        </Card.Header>
        <Card.Body className="px-6 py-6">
          <form className="flex flex-col gap-5" onSubmit={handleSubmit}>
            <label className="flex flex-col gap-2 text-sm font-medium text-zinc-800 dark:text-zinc-200">
              아이디
              <TextInput
                value={username}
                onValueChange={setUsername}
                placeholder="root"
                autoComplete="username"
                required
                size="lg"
              />
            </label>
            <label className="flex flex-col gap-2 text-sm font-medium text-zinc-800 dark:text-zinc-200">
              비밀번호
              <TextInput
                type="password"
                value={password}
                onValueChange={setPassword}
                placeholder="비밀번호를 입력하세요"
                autoComplete="current-password"
                required
                size="lg"
              />
            </label>

            {error && (
              <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950/40 dark:text-red-300">
                {error}
              </p>
            )}

            <Button type="submit" size="lg" disabled={isSubmitting || !username || !password}>
              {isSubmitting ? '로그인 중...' : '로그인'}
            </Button>
          </form>
        </Card.Body>
        <Card.Footer className="border-t border-zinc-100 px-6 py-4 text-sm text-zinc-600 dark:border-zinc-800 dark:text-zinc-400">
          계정이 없나요?{' '}
          <Link className="font-medium text-blue-600 hover:text-blue-700 dark:text-blue-400" href="/signup">
            회원가입
          </Link>
        </Card.Footer>
      </Card.Root>
    </main>
  );
}
