package gdfs.shopping.backend.domain.cart

import gdfs.shopping.backend.domain.product.Product
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cart_items")
data class CartItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    val cart: Cart,

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false)
    var quantity: Int = 1,

    @Column(nullable = false)
    val price: Int,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun getTotalPrice(): Int {
        return price * quantity
    }
}
