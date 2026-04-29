package gdfs.shopping.backend.domain.cart

import gdfs.shopping.backend.domain.member.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "carts")
data class Cart(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<CartItem> = mutableListOf(),

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun getTotalPrice(): Int {
        return items.sumOf { it.getTotalPrice() }
    }
}
