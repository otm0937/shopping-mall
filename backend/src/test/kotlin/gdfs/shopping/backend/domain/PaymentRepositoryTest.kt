package gdfs.shopping.backend.domain

import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.member.MemberRepository
import gdfs.shopping.backend.domain.payment.Payment
import gdfs.shopping.backend.domain.payment.PaymentItem
import gdfs.shopping.backend.domain.payment.PaymentRepository
import gdfs.shopping.backend.domain.payment.PaymentStatus
import gdfs.shopping.backend.domain.product.Product
import gdfs.shopping.backend.domain.product.ProductRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest @Autowired constructor(
    private val paymentRepository: PaymentRepository,
    private val memberRepository: MemberRepository,
    private val productRepository: ProductRepository
) {

    @Test
    fun `saves payment with item snapshots`() {
        // Verifies payment aggregate persistence cascades to purchased item snapshots.
        val member = memberRepository.save(member("payment-owner", "payment-owner@example.com"))
        val product = productRepository.save(product("Paid Product", 20_000))
        val payment = Payment(member = member, totalAmount = 40_000, status = PaymentStatus.COMPLETED)
        payment.items.add(
            PaymentItem(payment = payment, product = product, quantity = 2, unitPrice = 20_000, totalPrice = 40_000)
        )

        val saved = paymentRepository.saveAndFlush(payment)

        assertEquals(1, saved.items.size)
        assertEquals(40_000, saved.items.single().totalPrice)
        assertTrue(saved.items.single().id != null)
    }

    @Test
    fun `findByMemberIdOrderByCreatedAtDesc returns newest payments first`() {
        // Verifies payment history is ordered with the latest checkout first.
        val member = memberRepository.save(member("history-owner", "history-owner@example.com"))
        val oldPayment = Payment(
            member = member,
            totalAmount = 10_000,
            status = PaymentStatus.COMPLETED,
            createdAt = LocalDateTime.now().minusDays(1)
        )
        val newPayment = Payment(
            member = member,
            totalAmount = 30_000,
            status = PaymentStatus.COMPLETED,
            createdAt = LocalDateTime.now()
        )
        paymentRepository.saveAll(listOf(oldPayment, newPayment))
        paymentRepository.flush()

        val payments = paymentRepository.findByMemberIdOrderByCreatedAtDesc(member.id!!)

        assertEquals(listOf(30_000, 10_000), payments.map { it.totalAmount })
    }

    @Test
    fun `findByMemberIdOrderByCreatedAtDesc excludes other members payments`() {
        // Verifies users only see their own payment history rows.
        val owner = memberRepository.save(member("owner", "owner@example.com"))
        val other = memberRepository.save(member("other", "other@example.com"))
        paymentRepository.save(Payment(member = owner, totalAmount = 10_000, status = PaymentStatus.COMPLETED))
        paymentRepository.save(Payment(member = other, totalAmount = 99_000, status = PaymentStatus.COMPLETED))

        val payments = paymentRepository.findByMemberIdOrderByCreatedAtDesc(owner.id!!)

        assertEquals(1, payments.size)
        assertEquals(10_000, payments.single().totalAmount)
    }

    @Test
    fun `findByMemberIdOrderByCreatedAtDesc returns empty for member without payments`() {
        // Verifies empty payment history is returned as an empty list, not an error.
        val member = memberRepository.save(member("empty-history", "empty-history@example.com"))

        val payments = paymentRepository.findByMemberIdOrderByCreatedAtDesc(member.id!!)

        assertTrue(payments.isEmpty())
    }

    @Test
    fun `deletes payment record`() {
        // Verifies standard repository delete removes a payment row.
        val member = memberRepository.save(member("delete-payment", "delete-payment@example.com"))
        val payment = paymentRepository.save(Payment(member = member, totalAmount = 12_000, status = PaymentStatus.COMPLETED))

        paymentRepository.delete(payment)
        paymentRepository.flush()

        assertFalse(paymentRepository.findById(payment.id!!).isPresent)
    }

    private fun member(username: String, email: String) = Member(
        username = username,
        password = "encoded-password",
        name = username,
        email = email
    )

    private fun product(name: String, price: Int) = Product(
        name = name,
        price = price,
        description = "$name description"
    )
}
