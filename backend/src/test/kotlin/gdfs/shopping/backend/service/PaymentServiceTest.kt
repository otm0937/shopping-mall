package gdfs.shopping.backend.service

import gdfs.shopping.backend.domain.cart.Cart
import gdfs.shopping.backend.domain.cart.CartItem
import gdfs.shopping.backend.domain.cart.CartRepository
import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.member.MemberRepository
import gdfs.shopping.backend.domain.payment.Payment
import gdfs.shopping.backend.domain.payment.PaymentRepository
import gdfs.shopping.backend.domain.payment.PaymentStatus
import gdfs.shopping.backend.domain.product.Product
import gdfs.shopping.backend.dto.payment.PaymentRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class PaymentServiceTest {

    @Mock
    private lateinit var paymentRepository: PaymentRepository

    @Mock
    private lateinit var cartRepository: CartRepository

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var cartService: CartService

    private lateinit var paymentService: PaymentService

    @BeforeEach
    fun setUp() {
        paymentService = PaymentService(paymentRepository, cartRepository, memberRepository, cartService)
    }

    @Test
    fun `processPayment creates completed payment from cart items and clears cart`() {
        // Verifies checkout converts cart items into payment item snapshots and clears the cart after saving.
        val member = member()
        val cart = cartWithItem(member)
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member))
        Mockito.`when`(cartRepository.findByMemberId(1L)).thenReturn(Optional.of(cart))
        Mockito.doAnswer { invocation ->
            val payment = invocation.getArgument<Payment>(0)
            val saved = payment.copy(id = 500L, items = mutableListOf())
            payment.items.forEachIndexed { index, item ->
                saved.items.add(item.copy(id = index + 1L, payment = saved))
            }
            saved
        }.`when`(paymentRepository).save(anyValue())

        val response = paymentService.processPayment("buyer", PaymentRequest(paymentMethod = "MOCK"))

        assertEquals(500L, response.id)
        assertEquals(30_000, response.totalAmount)
        assertEquals(PaymentStatus.COMPLETED, response.status)
        assertEquals("결제가 완료되었습니다", response.message)
        assertEquals(1, response.items.size)
        assertEquals(10L, response.items.single().productId)
        assertEquals(2, response.items.single().quantity)
        Mockito.verify(cartService).clearCart("buyer")
    }

    @Test
    fun `processPayment rejects missing member`() {
        // Verifies checkout fails before cart lookup when the username is unknown.
        Mockito.`when`(memberRepository.findByUsername("ghost")).thenReturn(Optional.empty())

        val exception = assertFailsWith<IllegalArgumentException> {
            paymentService.processPayment("ghost", PaymentRequest())
        }

        assertEquals("회원을 찾을 수 없습니다", exception.message)
    }

    @Test
    fun `processPayment rejects missing cart`() {
        // Verifies checkout requires an existing cart for the member.
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member()))
        Mockito.`when`(cartRepository.findByMemberId(1L)).thenReturn(Optional.empty())

        val exception = assertFailsWith<IllegalArgumentException> {
            paymentService.processPayment("buyer", PaymentRequest())
        }

        assertEquals("장바구니를 찾을 수 없습니다", exception.message)
    }

    @Test
    fun `processPayment rejects empty cart`() {
        // Verifies payment is not saved when the cart has no items.
        val member = member()
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member))
        Mockito.`when`(cartRepository.findByMemberId(1L)).thenReturn(Optional.of(Cart(id = 100L, member = member)))

        val exception = assertFailsWith<IllegalArgumentException> {
            paymentService.processPayment("buyer", PaymentRequest())
        }

        assertEquals("장바구니가 비어있습니다", exception.message)
        Mockito.verify(paymentRepository, Mockito.never()).save(anyValue())
        Mockito.verify(cartService, Mockito.never()).clearCart(Mockito.anyString())
    }

    @Test
    fun `getPaymentHistory maps payments to history responses`() {
        // Verifies payment history exposes total amount, total quantity, status, and created time per payment.
        val member = member()
        val payment = paymentWithItem(id = 501L, member = member, createdAt = LocalDateTime.now())
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member))
        Mockito.`when`(paymentRepository.findByMemberIdOrderByCreatedAtDesc(1L)).thenReturn(listOf(payment))

        val responses = paymentService.getPaymentHistory("buyer")

        assertEquals(1, responses.size)
        assertEquals(501L, responses.single().id)
        assertEquals(30_000, responses.single().totalAmount)
        assertEquals(2, responses.single().totalQuantity)
        assertEquals(PaymentStatus.COMPLETED, responses.single().status)
    }

    @Test
    fun `getPaymentDetail returns owned payment detail`() {
        // Verifies users can view their own payment detail including purchased items.
        val member = member()
        val payment = paymentWithItem(id = 501L, member = member, createdAt = LocalDateTime.now())
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member))
        Mockito.`when`(paymentRepository.findById(501L)).thenReturn(Optional.of(payment))

        val response = paymentService.getPaymentDetail("buyer", 501L)

        assertEquals(501L, response.id)
        assertEquals("결제 내역 조회 성공", response.message)
        assertEquals(1, response.items.size)
        assertEquals("Paid Product", response.items.single().productName)
    }

    @Test
    fun `getPaymentDetail rejects missing payment`() {
        // Verifies a missing payment ID returns the expected not-found service error.
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member()))
        Mockito.`when`(paymentRepository.findById(404L)).thenReturn(Optional.empty())

        val exception = assertFailsWith<IllegalArgumentException> {
            paymentService.getPaymentDetail("buyer", 404L)
        }

        assertEquals("결제 내역을 찾을 수 없습니다: 404", exception.message)
    }

    @Test
    fun `getPaymentDetail rejects payment owned by another member`() {
        // Verifies users cannot view another member's payment detail.
        val currentMember = member()
        val otherMember = member(id = 2L, username = "other", email = "other@example.com")
        val payment = paymentWithItem(id = 501L, member = otherMember, createdAt = LocalDateTime.now())
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(currentMember))
        Mockito.`when`(paymentRepository.findById(501L)).thenReturn(Optional.of(payment))

        val exception = assertFailsWith<IllegalArgumentException> {
            paymentService.getPaymentDetail("buyer", 501L)
        }

        assertEquals("해당 결제 내역에 접근할 권한이 없습니다", exception.message)
    }

    private fun member(
        id: Long = 1L,
        username: String = "buyer",
        email: String = "buyer@example.com"
    ) = Member(
        id = id,
        username = username,
        password = "encoded-password",
        name = "Buyer",
        email = email
    )

    private fun product() = Product(
        id = 10L,
        name = "Paid Product",
        price = 15_000,
        description = "Paid product description"
    )

    private fun cartWithItem(member: Member): Cart {
        val cart = Cart(id = 100L, member = member)
        cart.items.add(CartItem(id = 200L, cart = cart, product = product(), quantity = 2, price = 15_000))
        return cart
    }

    private fun paymentWithItem(id: Long, member: Member, createdAt: LocalDateTime): Payment {
        val payment = Payment(id = id, member = member, totalAmount = 30_000, status = PaymentStatus.COMPLETED, createdAt = createdAt)
        payment.items.add(
            gdfs.shopping.backend.domain.payment.PaymentItem(
                id = 601L,
                payment = payment,
                product = product(),
                quantity = 2,
                unitPrice = 15_000,
                totalPrice = 30_000
            )
        )
        return payment
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyValue(): T {
        Mockito.any<T>()
        return null as T
    }
}
