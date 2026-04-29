import '@testing-library/jest-dom';
import type { ReactNode } from 'react';

process.env.NEXT_PUBLIC_API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost';

// Keep browser state isolated across component, hook, and page tests.
afterEach(() => {
  window.localStorage.clear();
  jest.clearAllMocks();
});

// Next.js components are mocked to semantic HTML so RTL can assert links and images directly.
jest.mock('next/link', () => ({
  __esModule: true,
  default: ({ children, href, ...props }: { children: ReactNode; href: string }) => (
    <a href={href} {...props}>
      {children}
    </a>
  ),
}));

jest.mock('next/image', () => ({
  __esModule: true,
  default: ({ fill, priority, unoptimized, ...props }: Record<string, unknown>) => (
    <img {...props} alt={typeof props.alt === 'string' ? props.alt : ''} />
  ),
}));

jest.mock('next/navigation', () => ({
  usePathname: jest.fn(() => '/'),
  useRouter: jest.fn(() => ({
    back: jest.fn(),
    forward: jest.fn(),
    prefetch: jest.fn(),
    push: jest.fn(),
    refresh: jest.fn(),
    replace: jest.fn(),
  })),
}));

// Vapor UI is not the subject of these tests. The mock preserves accessibility semantics and
// onValueChange behavior used by forms without pulling in implementation details from the UI kit.
jest.mock('@vapor-ui/core', () => {
  const React = require('react');
  const omittedProps = new Set(['animation', 'colorPalette', 'foreground', 'shape', 'size', 'typography', 'variant']);

  function cleanProps(props: Record<string, unknown>) {
    return Object.fromEntries(Object.entries(props).filter(([key]) => !omittedProps.has(key)));
  }

  const createElement = (tagName: string) =>
    React.forwardRef(({ children, ...props }: Record<string, unknown>, ref: unknown) =>
      React.createElement(tagName, { ...cleanProps(props), ref }, children),
    );

  const Button = React.forwardRef(({ children, ...props }: Record<string, unknown>, ref: unknown) =>
    React.createElement('button', { ...cleanProps(props), ref }, children),
  );

  const TextInput = React.forwardRef(
    ({ onValueChange, onChange, value = '', ...props }: Record<string, unknown>, ref: unknown) =>
      React.createElement('input', {
        ...cleanProps(props),
        ref,
        value,
        onChange: (event: React.ChangeEvent<HTMLInputElement>) => {
          if (typeof onValueChange === 'function') {
            onValueChange(event.target.value);
          }

          if (typeof onChange === 'function') {
            onChange(event);
          }
        },
      }),
  );

  const Textarea = React.forwardRef(
    ({ onValueChange, onChange, value = '', ...props }: Record<string, unknown>, ref: unknown) =>
      React.createElement('textarea', {
        ...cleanProps(props),
        ref,
        value,
        onChange: (event: React.ChangeEvent<HTMLTextAreaElement>) => {
          if (typeof onValueChange === 'function') {
            onValueChange(event.target.value);
          }

          if (typeof onChange === 'function') {
            onChange(event);
          }
        },
      }),
  );

  return {
    __esModule: true,
    Badge: createElement('span'),
    Button,
    Card: {
      Body: createElement('div'),
      Footer: createElement('div'),
      Header: createElement('div'),
      Root: createElement('div'),
    },
    Skeleton: ({ children, ...props }: { children?: ReactNode } & Record<string, unknown>) => (
      <div data-testid="skeleton" {...cleanProps(props)}>
        {children}
      </div>
    ),
    Text: createElement('span'),
    Textarea,
    TextInput,
    ThemeProvider: ({ children }: { children: ReactNode }) => <>{children}</>,
  };
});
