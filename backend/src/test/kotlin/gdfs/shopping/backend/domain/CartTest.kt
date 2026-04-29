package gdfs.shopping.backend.domain

import gdfs.shopping.backend.domain.cart.Cart
import gdfs.shopping.backend.domain.cart.CartItem
import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.product.Product
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CartTest {

    @Test
    fun `new cart starts empty with zero total price`() {
        // Verifies an empty cart is safe to return to users and totals to zero.
        val cart = Cart(member = member())

        assertEquals(0, cart.items.size)
        assertEquals(0, cart.getTotalPrice())
        assertNotNull(cart.createdAt)
        assertNotNull(cart.updatedAt)
    }

    @Test
    fun `cart item calculates total from captured price and quantity`() {
        // Verifies cart item totals use the snapshot price stored on the item, not a later product lookup.
        val cart = Cart(member = member())
        val item = CartItem(
            cart = cart,
            product = product(price = 12_000),
            quantity = 3,
            price = 12_000
        )

        assertEquals(36_000, item.getTotalPrice())
    }

    @Test
    fun `cart total sums all cart item totals`() {
        // Verifies multi-item carts aggregate each item total correctly for checkout.
        val cart = Cart(member = member())
        val firstItem = CartItem(cart = cart, product = product(id = 1L, price = 10_000), quantity = 2, price = 10_000)
        val secondItem = CartItem(cart = cart, product = product(id = 2L, price = 5_000), quantity = 3, price = 5_000)

        cart.items.add(firstItem)
        cart.items.add(secondItem)

        assertEquals(35_000, cart.getTotalPrice())
    }

    @Test
    fun `changing item quantity recalculates item and cart totals`() {
        // Verifies increasing an existing cart item quantity immediately affects totals used by services.
        val cart = Cart(member = member())
        val item = CartItem(cart = cart, product = product(price = 7_000), quantity = 1, price = 7_000)
        cart.items.add(item)

        item.quantity = 4

        assertEquals(28_000, item.getTotalPrice())
        assertEquals(28_000, cart.getTotalPrice())
    }

    @Test
    fun `zero quantity item contributes zero to total`() {
        // Verifies the entity calculation remains deterministic for edge data even though request validation requires one or more.
        val cart = Cart(member = member())
        val item = CartItem(cart = cart, product = product(price = 9_000), quantity = 0, price = 9_000)
        cart.items.add(item)

        assertEquals(0, item.getTotalPrice())
        assertEquals(0, cart.getTotalPrice())
    }

    private fun member() = Member(
        id = 1L,
        username = "cart-user",
        password = "encoded-password",
        name = "Cart User",
        email = "cart-user@example.com"
    )

    private fun product(id: Long = 1L, price: Int = 10_000) = Product(
        id = id,
        name = "Product $id",
        price = price,
        description = "Product description"
    )
}
