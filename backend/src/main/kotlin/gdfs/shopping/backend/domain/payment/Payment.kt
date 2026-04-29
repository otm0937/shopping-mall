package gdfs.shopping.backend.domain.payment

import gdfs.shopping.backend.domain.member.Member
import jakarta.persistence.*
import java.time.LocalDateTime

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED, CANCELLED
}

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = false)
    val totalAmount: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @OneToMany(mappedBy = "payment", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<PaymentItem> = mutableListOf(),

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
