package gdfs.shopping.backend.domain

import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.payment.Payment
import gdfs.shopping.backend.domain.payment.PaymentItem
import gdfs.shopping.backend.domain.payment.PaymentStatus
import gdfs.shopping.backend.domain.product.Product
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PaymentTest {

    @Test
    fun `new payment defaults to pending status`() {
        // Verifies payment creation starts in PENDING unless the service explicitly marks it completed.
        val payment = Payment(
            member = member(),
            totalAmount = 25_000
        )

        assertEquals(PaymentStatus.PENDING, payment.status)
        assertEquals(25_000, payment.totalAmount)
        assertEquals(0, payment.items.size)
        assertNotNull(payment.createdAt)
        assertNotNull(payment.updatedAt)
    }

    @Test
    fun `completed payment can contain purchased item snapshots`() {
        // Verifies completed payments retain item snapshots for payment history and detail APIs.
        val payment = Payment(
            member = member(),
            totalAmount = 40_000,
            status = PaymentStatus.COMPLETED
        )
        val item = PaymentItem(
            payment = payment,
            product = product(),
            quantity = 2,
            unitPrice = 20_000,
            totalPrice = 40_000
        )

        payment.items.add(item)

        assertEquals(PaymentStatus.COMPLETED, payment.status)
        assertEquals(1, payment.items.size)
        assertEquals(40_000, payment.items.single().totalPrice)
    }

    @Test
    fun `payment item stores unit price quantity and total price independently`() {
        // Verifies a payment item preserves the exact checkout price snapshot even if product state later changes.
        val payment = Payment(member = member(), totalAmount = 30_000)
        val item = PaymentItem(
            id = 99L,
            payment = payment,
            product = product(price = 15_000),
            quantity = 2,
            unitPrice = 15_000,
            totalPrice = 30_000
        )

        assertEquals(99L, item.id)
        assertEquals(2, item.quantity)
        assertEquals(15_000, item.unitPrice)
        assertEquals(30_000, item.totalPrice)
        assertNotNull(item.createdAt)
    }

    @Test
    fun `payment status can move to failure states`() {
        // Verifies mutable status supports later payment failure or cancellation workflows.
        val payment = Payment(member = member(), totalAmount = 10_000)

        payment.status = PaymentStatus.FAILED
        assertEquals(PaymentStatus.FAILED, payment.status)

        payment.status = PaymentStatus.CANCELLED
        assertEquals(PaymentStatus.CANCELLED, payment.status)
    }

    private fun member() = Member(
        id = 1L,
        username = "payment-user",
        password = "encoded-password",
        name = "Payment User",
        email = "payment-user@example.com"
    )

    private fun product(price: Int = 20_000) = Product(
        id = 1L,
        name = "Payment Product",
        price = price,
        description = "Payment product description"
    )
}
