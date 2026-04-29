import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ProductForm from '@/components/admin/ProductForm';
import { sampleProductDetail } from '../test-utils';

describe('ProductForm', () => {
  beforeEach(() => {
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: jest.fn(() => 'blob:test-preview'),
    });
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      value: jest.fn(),
    });
  });

  it('validates required fields before calling onSubmit', () => {
    const onSubmit = jest.fn();

    render(<ProductForm submitLabel="저장" isSubmitting={false} onSubmit={onSubmit} onCancel={jest.fn()} />);

    // fireEvent.submit bypasses browser constraint validation so the component validation branch is tested.
    fireEvent.submit(screen.getByRole('button', { name: '저장' }).closest('form') as HTMLFormElement);

    expect(screen.getByText('상품명을 입력해 주세요.')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('submits trimmed product data and the selected image file', async () => {
    const user = userEvent.setup();
    const onSubmit = jest.fn().mockResolvedValue(undefined);
    const image = new File(['image-bytes'], 'camera.png', { type: 'image/png' });

    render(<ProductForm submitLabel="상품 저장" isSubmitting={false} onSubmit={onSubmit} onCancel={jest.fn()} />);

    await user.type(screen.getByLabelText('상품명'), '  테스트 카메라  ');
    await user.type(screen.getByLabelText('가격'), '129000');
    await user.type(screen.getByLabelText('상품 설명'), '  좋은 테스트 상품입니다.  ');
    await user.upload(screen.getByLabelText(/상품 이미지/), image);
    await user.click(screen.getByRole('button', { name: '상품 저장' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledTimes(1));

    const formData = onSubmit.mock.calls[0][0] as FormData;
    expect(formData.get('name')).toBe('테스트 카메라');
    expect(formData.get('price')).toBe('129000');
    expect(formData.get('description')).toBe('좋은 테스트 상품입니다.');
    expect(formData.get('image')).toBe(image);
    expect(screen.getByText('선택한 파일: camera.png')).toBeInTheDocument();
    expect(screen.getByRole('img', { name: '상품 이미지 미리보기' })).toHaveAttribute('src', 'blob:test-preview');
  });

  it('renders existing product values and supports cancel', async () => {
    const user = userEvent.setup();
    const onCancel = jest.fn();

    render(
      <ProductForm
        initialProduct={sampleProductDetail}
        submitLabel="수정"
        isSubmitting={false}
        onSubmit={jest.fn()}
        onCancel={onCancel}
      />,
    );

    expect(screen.getByDisplayValue('테스트 카메라')).toBeInTheDocument();
    expect(screen.getByDisplayValue('129000')).toBeInTheDocument();
    expect(screen.getByText('표시 가격: ₩129,000')).toBeInTheDocument();
    expect(screen.getByRole('img', { name: '상품 이미지 미리보기' })).toHaveAttribute(
      'src',
      'http://localhost/uploads/camera.jpg',
    );

    await user.click(screen.getByRole('button', { name: '취소' }));

    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it('disables actions while submitting', () => {
    render(<ProductForm submitLabel="저장" isSubmitting onSubmit={jest.fn()} onCancel={jest.fn()} />);

    expect(screen.getByRole('button', { name: '저장 중...' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '취소' })).toBeDisabled();
  });
});
