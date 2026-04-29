'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { Badge, Button } from '@vapor-ui/core';
import { useAuth } from '@/hooks/useAuth';
import { useCart } from '@/hooks/useCart';

function getNavLinkClass(pathname: string, href: string) {
  const isActive = href === '/' ? pathname === href : pathname === href || pathname.startsWith(`${href}/`);

  return isActive
    ? 'text-zinc-950 dark:text-zinc-50'
    : 'text-zinc-600 hover:text-zinc-950 dark:text-zinc-400 dark:hover:text-zinc-50';
}

export default function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const { user, isAuthenticated, isAdmin, isLoading, logout } = useAuth();
  const { itemCount, clearCart } = useCart();

  const handleLogout = async () => {
    await logout();
    clearCart();
    router.replace('/login');
  };

  const cartLabel = (
    <span className="inline-flex items-center gap-1.5">
      Cart
      {itemCount > 0 && (
        <Badge colorPalette="primary" shape="pill" size="sm">
          {itemCount}
        </Badge>
      )}
    </span>
  );

  return (
    <header className="sticky top-0 z-20 border-b border-zinc-200 bg-white/90 backdrop-blur dark:border-zinc-800 dark:bg-black/90">
      <div className="mx-auto flex min-h-16 w-full max-w-6xl flex-col gap-3 px-6 py-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center justify-between gap-6">
          <Link className="text-lg font-semibold tracking-tight text-zinc-950 dark:text-zinc-50" href="/">
            Shopping Mall
          </Link>
          <nav className="hidden items-center gap-4 text-sm font-medium sm:flex">
            {isAuthenticated && (
              <>
                <Link className={getNavLinkClass(pathname, '/cart')} href="/cart">
                  {cartLabel}
                </Link>
                <Link className={getNavLinkClass(pathname, '/payments')} href="/payments">
                  Payments
                </Link>
              </>
            )}
            {isAdmin && (
              <Link className={getNavLinkClass(pathname, '/admin/products')} href="/admin/products">
                Admin
              </Link>
            )}
          </nav>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {isLoading ? (
            <span className="text-sm text-zinc-500">확인 중...</span>
          ) : isAuthenticated ? (
            <>
              <span className="text-sm text-zinc-600 dark:text-zinc-400">
                <strong className="font-semibold text-zinc-950 dark:text-zinc-50">{user?.name}</strong>님
              </span>
              <Button type="button" variant="outline" size="sm" onClick={handleLogout}>
                Logout
              </Button>
            </>
          ) : (
            <>
              <Link
                className="rounded-md px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100 hover:text-zinc-950 dark:text-zinc-300 dark:hover:bg-zinc-900 dark:hover:text-zinc-50"
                href="/login"
              >
                Login
              </Link>
              <Link
                className="rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white hover:bg-blue-700"
                href="/signup"
              >
                Signup
              </Link>
            </>
          )}
        </div>

        <nav className="flex items-center gap-4 text-sm font-medium sm:hidden">
          {isAuthenticated && (
            <>
              <Link className={getNavLinkClass(pathname, '/cart')} href="/cart">
                {cartLabel}
              </Link>
              <Link className={getNavLinkClass(pathname, '/payments')} href="/payments">
                Payments
              </Link>
            </>
          )}
          {isAdmin && (
            <Link className={getNavLinkClass(pathname, '/admin/products')} href="/admin/products">
              Admin
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
}
