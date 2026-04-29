package gdfs.shopping.backend.domain.payment

import gdfs.shopping.backend.domain.product.Product
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment_items")
data class PaymentItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    val payment: Payment,

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false)
    val quantity: Int,

    @Column(nullable = false)
    val unitPrice: Int,

    @Column(nullable = false)
    val totalPrice: Int,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
