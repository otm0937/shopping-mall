package gdfs.shopping.backend.service

import gdfs.shopping.backend.domain.cart.CartRepository
import gdfs.shopping.backend.domain.member.MemberRepository
import gdfs.shopping.backend.domain.payment.Payment
import gdfs.shopping.backend.domain.payment.PaymentItem
import gdfs.shopping.backend.domain.payment.PaymentRepository
import gdfs.shopping.backend.domain.payment.PaymentStatus
import gdfs.shopping.backend.dto.payment.PaymentHistoryResponse
import gdfs.shopping.backend.dto.payment.PaymentItemResponse
import gdfs.shopping.backend.dto.payment.PaymentRequest
import gdfs.shopping.backend.dto.payment.PaymentResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val cartRepository: CartRepository,
    private val memberRepository: MemberRepository,
    private val cartService: CartService
) {

    @Transactional
    fun processPayment(username: String, request: PaymentRequest): PaymentResponse {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { IllegalArgumentException("회원을 찾을 수 없습니다") }

        val cart = cartRepository.findByMemberId(member.id!!)
            .orElseThrow { IllegalArgumentException("장바구니를 찾을 수 없습니다") }

        if (cart.items.isEmpty()) {
            throw IllegalArgumentException("장바구니가 비어있습니다")
        }

        val totalAmount = cart.getTotalPrice()

        val payment = Payment(
            member = member,
            totalAmount = totalAmount,
            status = PaymentStatus.COMPLETED
        )

        cart.items.forEach { cartItem ->
            val paymentItem = PaymentItem(
                payment = payment,
                product = cartItem.product,
                quantity = cartItem.quantity,
                unitPrice = cartItem.price,
                totalPrice = cartItem.getTotalPrice()
            )
            payment.items.add(paymentItem)
        }

        val savedPayment = paymentRepository.save(payment)

        cartService.clearCart(username)

        return PaymentResponse(
            id = savedPayment.id!!,
            totalAmount = savedPayment.totalAmount,
            status = savedPayment.status,
            items = savedPayment.items.map { item ->
                PaymentItemResponse(
                    id = item.id!!,
                    productId = item.product.id!!,
                    productName = item.product.name,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice
                )
            },
            createdAt = savedPayment.createdAt,
            message = "결제가 완료되었습니다"
        )
    }

    @Transactional(readOnly = true)
    fun getPaymentHistory(username: String): List<PaymentHistoryResponse> {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { IllegalArgumentException("회원을 찾을 수 없습니다") }

        return paymentRepository.findByMemberIdOrderByCreatedAtDesc(member.id!!)
            .map { payment ->
                PaymentHistoryResponse(
                    id = payment.id!!,
                    totalAmount = payment.totalAmount,
                    totalQuantity = payment.items.sumOf { it.quantity },
                    status = payment.status,
                    createdAt = payment.createdAt
                )
            }
    }

    @Transactional(readOnly = true)
    fun getPaymentDetail(username: String, paymentId: Long): PaymentResponse {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { IllegalArgumentException("회원을 찾을 수 없습니다") }

        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { IllegalArgumentException("결제 내역을 찾을 수 없습니다: $paymentId") }

        if (payment.member.id != member.id) {
            throw IllegalArgumentException("해당 결제 내역에 접근할 권한이 없습니다")
        }

        return PaymentResponse(
            id = payment.id!!,
            totalAmount = payment.totalAmount,
            status = payment.status,
            items = payment.items.map { item ->
                PaymentItemResponse(
                    id = item.id!!,
                    productId = item.product.id!!,
                    productName = item.product.name,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice
                )
            },
            createdAt = payment.createdAt,
            message = "결제 내역 조회 성공"
        )
    }
}
