package gdfs.shopping.backend.domain.cart

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CartRepository : JpaRepository<Cart, Long> {
    fun findByMemberId(memberId: Long): Optional<Cart>
}

@Repository
interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findByCartIdAndProductId(cartId: Long, productId: Long): Optional<CartItem>
}
