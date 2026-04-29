package gdfs.shopping.backend.domain

import gdfs.shopping.backend.domain.cart.Cart
import gdfs.shopping.backend.domain.cart.CartItem
import gdfs.shopping.backend.domain.cart.CartItemRepository
import gdfs.shopping.backend.domain.cart.CartRepository
import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.member.MemberRepository
import gdfs.shopping.backend.domain.product.Product
import gdfs.shopping.backend.domain.product.ProductRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DataJpaTest
@ActiveProfiles("test")
class CartRepositoryTest @Autowired constructor(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val memberRepository: MemberRepository,
    private val productRepository: ProductRepository
) {

    @Test
    fun `saves cart and finds it by member id`() {
        // Verifies each member can retrieve their own cart through the repository query used by CartService.
        val member = memberRepository.save(member("cart-owner", "cart-owner@example.com"))
        val cart = cartRepository.save(Cart(member = member))

        val found = cartRepository.findByMemberId(member.id!!)

        assertTrue(found.isPresent)
        assertEquals(cart.id, found.get().id)
        assertEquals(member.id, found.get().member.id)
    }

    @Test
    fun `findByMemberId returns empty for unknown member`() {
        // Verifies missing carts are represented as Optional.empty so the service can create one lazily.
        val found = cartRepository.findByMemberId(999L)

        assertFalse(found.isPresent)
    }

    @Test
    fun `saves cart item and finds it by cart and product ids`() {
        // Verifies duplicate-cart-item detection can locate an existing item for the same cart and product.
        val member = memberRepository.save(member("item-owner", "item-owner@example.com"))
        val product = productRepository.save(product("Cart Product", 12_000))
        val cart = cartRepository.save(Cart(member = member))
        val item = cartItemRepository.save(CartItem(cart = cart, product = product, quantity = 2, price = product.price))

        val found = cartItemRepository.findByCartIdAndProductId(cart.id!!, product.id!!)

        assertTrue(found.isPresent)
        assertEquals(item.id, found.get().id)
        assertEquals(24_000, found.get().getTotalPrice())
    }

    @Test
    fun `findByCartIdAndProductId returns empty for missing combination`() {
        // Verifies new cart-item creation path is used when the product is not already in the cart.
        val member = memberRepository.save(member("missing-item-owner", "missing-item-owner@example.com"))
        val cart = cartRepository.save(Cart(member = member))

        val found = cartItemRepository.findByCartIdAndProductId(cart.id!!, 999L)

        assertFalse(found.isPresent)
    }

    @Test
    fun `deleting cart cascades to cart items when relationship is populated`() {
        // Verifies Cart.items cascade removes child rows when the aggregate root is deleted.
        val member = memberRepository.save(member("cascade-owner", "cascade-owner@example.com"))
        val product = productRepository.save(product("Cascade Product", 8_000))
        val cart = Cart(member = member)
        val item = CartItem(cart = cart, product = product, quantity = 1, price = product.price)
        cart.items.add(item)
        val savedCart = cartRepository.saveAndFlush(cart)
        val itemId = savedCart.items.single().id!!

        cartRepository.delete(savedCart)
        cartRepository.flush()

        assertFalse(cartItemRepository.findById(itemId).isPresent)
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
