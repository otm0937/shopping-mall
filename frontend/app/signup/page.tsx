'use client';

import { FormEvent, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Button, Card, TextInput } from '@vapor-ui/core';
import { ApiError } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';

export default function SignupPage() {
  const router = useRouter();
  const { signup } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validate = () => {
    if (username.trim().length < 4) {
      return '아이디는 4자 이상 입력해 주세요.';
    }

    if (password.length < 6) {
      return '비밀번호는 6자 이상 입력해 주세요.';
    }

    if (!name.trim()) {
      return '이름을 입력해 주세요.';
    }

    if (!email.trim()) {
      return '이메일을 입력해 주세요.';
    }

    return '';
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');

    const validationError = validate();

    if (validationError) {
      setError(validationError);
      return;
    }

    setIsSubmitting(true);

    try {
      await signup({ username: username.trim(), password, name: name.trim(), email: email.trim() });
      router.replace('/login');
    } catch (caughtError) {
      setError(
        caughtError instanceof ApiError || caughtError instanceof Error
          ? caughtError.message
          : '회원가입 중 오류가 발생했습니다.',
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
          <h1 className="mt-2 text-2xl font-semibold text-zinc-950 dark:text-zinc-50">회원가입</h1>
          <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
            새 계정을 만들고 상품을 둘러보세요.
          </p>
        </Card.Header>
        <Card.Body className="px-6 py-6">
          <form className="flex flex-col gap-5" onSubmit={handleSubmit}>
            <label className="flex flex-col gap-2 text-sm font-medium text-zinc-800 dark:text-zinc-200">
              아이디
              <TextInput
                value={username}
                onValueChange={setUsername}
                placeholder="4자 이상"
                autoComplete="username"
                required
                minLength={4}
                invalid={Boolean(error) && username.trim().length < 4}
                size="lg"
              />
            </label>
            <label className="flex flex-col gap-2 text-sm font-medium text-zinc-800 dark:text-zinc-200">
              비밀번호
              <TextInput
                type="password"
                value={password}
                onValueChange={setPassword}
                placeholder="6자 이상"
                autoComplete="new-password"
                required
                minLength={6}
                invalid={Boolean(error) && password.length > 0 && password.length < 6}
                size="lg"
              />
            </label>
            <label className="flex flex-col gap-2 text-sm font-medium text-zinc-800 dark:text-zinc-200">
              이름
              <TextInput
                value={name}
                onValueChange={setName}
                placeholder="홍길동"
                autoComplete="name"
                required
                size="lg"
              />
            </label>
            <label className="flex flex-col gap-2 text-sm font-medium text-zinc-800 dark:text-zinc-200">
              이메일
              <TextInput
                type="email"
                value={email}
                onValueChange={setEmail}
                placeholder="user@example.com"
                autoComplete="email"
                required
                size="lg"
              />
            </label>

            {error && (
              <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950/40 dark:text-red-300">
                {error}
              </p>
            )}

            <Button type="submit" size="lg" disabled={isSubmitting}>
              {isSubmitting ? '가입 중...' : '회원가입'}
            </Button>
          </form>
        </Card.Body>
        <Card.Footer className="border-t border-zinc-100 px-6 py-4 text-sm text-zinc-600 dark:border-zinc-800 dark:text-zinc-400">
          이미 계정이 있나요?{' '}
          <Link className="font-medium text-blue-600 hover:text-blue-700 dark:text-blue-400" href="/login">
            로그인
          </Link>
        </Card.Footer>
      </Card.Root>
    </main>
  );
}
