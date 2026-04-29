'use client';

import { type ReactNode } from 'react';
import { Card } from '@vapor-ui/core';
import AuthGuard from './AuthGuard';
import { useAuth } from '@/hooks/useAuth';

interface AdminGuardProps {
  children: ReactNode;
}

function AdminAccessCheck({ children }: AdminGuardProps) {
  const { isAdmin } = useAuth();

  if (!isAdmin) {
    return (
      <div className="flex min-h-[320px] items-center justify-center px-6">
        <Card.Root className="w-full max-w-md border border-zinc-200 bg-white shadow-sm dark:border-zinc-800 dark:bg-zinc-950">
          <Card.Header className="px-6 pt-6">
            <h2 className="text-xl font-semibold text-zinc-950 dark:text-zinc-50">접근 권한이 없습니다</h2>
          </Card.Header>
          <Card.Body className="px-6 pb-6 pt-3">
            <p className="text-sm leading-6 text-zinc-600 dark:text-zinc-400">
              관리자 계정만 이 페이지에 접근할 수 있습니다.
            </p>
          </Card.Body>
        </Card.Root>
      </div>
    );
  }

  return <>{children}</>;
}

export default function AdminGuard({ children }: AdminGuardProps) {
  return (
    <AuthGuard>
      <AdminAccessCheck>{children}</AdminAccessCheck>
    </AuthGuard>
  );
}
