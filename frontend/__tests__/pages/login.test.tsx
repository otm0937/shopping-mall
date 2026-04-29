import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useRouter } from 'next/navigation';
import LoginPage from '@/app/login/page';
import { useAuth } from '@/hooks/useAuth';

jest.mock('@/hooks/useAuth', () => ({
  useAuth: jest.fn(),
}));

const mockUseAuth = useAuth as jest.Mock;
const mockUseRouter = useRouter as jest.Mock;

describe('LoginPage', () => {
  it('submits credentials and redirects to home on success', async () => {
    const user = userEvent.setup();
    const login = jest.fn().mockResolvedValue({ message: 'ok', token: 'jwt', username: 'root' });
    const replace = jest.fn();

    mockUseAuth.mockReturnValue({ login });
    mockUseRouter.mockReturnValue({ replace });

    render(<LoginPage />);

    expect(screen.getByRole('button', { name: '로그인' })).toBeDisabled();

    await user.type(screen.getByLabelText('아이디'), 'root');
    await user.type(screen.getByLabelText('비밀번호'), 'root');
    await user.click(screen.getByRole('button', { name: '로그인' }));

    await waitFor(() => expect(login).toHaveBeenCalledWith({ password: 'root', username: 'root' }));
    expect(replace).toHaveBeenCalledWith('/');
  });

  it('shows login errors and keeps the user on the form', async () => {
    const user = userEvent.setup();
    const login = jest.fn().mockRejectedValue(new Error('아이디 또는 비밀번호가 올바르지 않습니다.'));
    const replace = jest.fn();

    mockUseAuth.mockReturnValue({ login });
    mockUseRouter.mockReturnValue({ replace });

    render(<LoginPage />);

    await user.type(screen.getByLabelText('아이디'), 'wrong');
    await user.type(screen.getByLabelText('비밀번호'), 'secret');
    await user.click(screen.getByRole('button', { name: '로그인' }));

    expect(await screen.findByText('아이디 또는 비밀번호가 올바르지 않습니다.')).toBeInTheDocument();
    expect(replace).not.toHaveBeenCalled();
  });
});
