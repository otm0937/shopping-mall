package gdfs.shopping.backend.service

import gdfs.shopping.backend.domain.cart.Cart
import gdfs.shopping.backend.domain.cart.CartItem
import gdfs.shopping.backend.domain.cart.CartItemRepository
import gdfs.shopping.backend.domain.cart.CartRepository
import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.member.MemberRepository
import gdfs.shopping.backend.domain.product.Product
import gdfs.shopping.backend.domain.product.ProductRepository
import gdfs.shopping.backend.dto.cart.AddToCartRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class CartServiceTest {

    @Mock
    private lateinit var cartRepository: CartRepository

    @Mock
    private lateinit var cartItemRepository: CartItemRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var memberRepository: MemberRepository

    private lateinit var cartService: CartService

    @BeforeEach
    fun setUp() {
        cartService = CartService(cartRepository, cartItemRepository, productRepository, memberRepository)
    }

    @Test
    fun `addToCart creates cart and item when product is not already present`() {
        // Verifies first add creates a cart, saves a new item, and returns a calculated cart response.
        val member = member()
        val product = product()
        val savedCart = Cart(id = 100L, member = member)
        val savedItem = CartItem(id = 200L, cart = savedCart, product = product, quantity = 2, price = product.price)
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member))
        Mockito.`when`(productRepository.findById(10L)).thenReturn(Optional.of(product))
        Mockito.`when`(cartRepository.findByMemberId(1L)).thenReturn(Optional.empty())
        Mockito.`when`(cartRepository.save(anyValue())).thenReturn(savedCart)
        Mockito.`when`(cartItemRepository.findByCartIdAndProductId(100L, 10L)).thenReturn(Optional.empty())
        Mockito.`when`(cartItemRepository.findAll()).thenReturn(listOf(savedItem))

        val response = cartService.addToCart("buyer", AddToCartRequest(productId = 10L, quantity = 2))

        val itemCaptor = ArgumentCaptor.forClass(CartItem::class.java)
        Mockito.verify(cartItemRepository).save(itemCaptor.capture())
        assertEquals(10L, itemCaptor.value.product.id)
        assertEquals(2, itemCaptor.value.quantity)
        assertEquals("장바구니에 추가되었습니다", response.message)
        assertEquals(30_000, response.totalPrice)
        assertEquals(1, response.items.size)
    }

    @Test
    fun `addToCart increases quantity when cart item already exists`() {
        // Verifies adding the same product increments the existing cart item rather than creating a duplicate.
        val member = member()
        val product = product()
        val cart = Cart(id = 100L, member = member)
        val existingItem = CartItem(id = 200L, cart = cart, product = product, quantity = 1, price = product.price)
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member))
        Mockito.`when`(productRepository.findById(10L)).thenReturn(Optional.of(product))
        Mockito.`when`(cartRepository.findByMemberId(1L)).thenReturn(Optional.of(cart))
        Mockito.`when`(cartItemRepository.findByCartIdAndProductId(100L, 10L)).thenReturn(Optional.of(existingItem))
        Mockito.`when`(cartItemRepository.findAll()).thenReturn(listOf(existingItem))

        val response = cartService.addToCart("buyer", AddToCartRequest(productId = 10L, quantity = 3))

        assertEquals(4, existingItem.quantity)
        assertEquals(60_000, response.totalPrice)
        Mockito.verify(cartItemRepository).save(existingItem)
    }

    @Test
    fun `addToCart rejects missing member`() {
        // Verifies cart operations fail clearly when the authenticated username cannot be found.
        Mockito.`when`(memberRepository.findByUsername("ghost")).thenReturn(Optional.empty())

        val exception = assertFailsWith<IllegalArgumentException> {
            cartService.addToCart("ghost", AddToCartRequest(productId = 10L, quantity = 1))
        }

        assertEquals("회원을 찾을 수 없습니다", exception.message)
    }

    @Test
    fun `addToCart rejects missing product`() {
        // Verifies adding an unknown product returns a product-specific failure.
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member()))
        Mockito.`when`(productRepository.findById(404L)).thenReturn(Optional.empty())

        val exception = assertFailsWith<IllegalArgumentException> {
            cartService.addToCart("buyer", AddToCartRequest(productId = 404L, quantity = 1))
        }

        assertEquals("상품을 찾을 수 없습니다: 404", exception.message)
    }

    @Test
    fun `getCart creates empty cart when member has no cart`() {
        // Verifies cart lookup lazily creates and returns an empty cart for first-time users.
        val member = member()
        val savedCart = Cart(id = 100L, member = member)
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member))
        Mockito.`when`(cartRepository.findByMemberId(1L)).thenReturn(Optional.empty())
        Mockito.`when`(cartRepository.save(anyValue())).thenReturn(savedCart)
        Mockito.`when`(cartItemRepository.findAll()).thenReturn(emptyList())

        val response = cartService.getCart("buyer")

        assertEquals(100L, response.id)
        assertTrue(response.items.isEmpty())
        assertEquals(0, response.totalPrice)
    }

    @Test
    fun `removeFromCart deletes item owned by current member cart`() {
        // Verifies users can remove only items belonging to their own cart.
        val member = member()
        val product = product()
        val cart = Cart(id = 100L, member = member)
        val item = CartItem(id = 200L, cart = cart, product = product, quantity = 1, price = product.price)
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member))
        Mockito.`when`(cartRepository.findByMemberId(1L)).thenReturn(Optional.of(cart))
        Mockito.`when`(cartItemRepository.findById(200L)).thenReturn(Optional.of(item))
        Mockito.`when`(cartItemRepository.findAll()).thenReturn(emptyList())

        val response = cartService.removeFromCart("buyer", 200L)

        Mockito.verify(cartItemRepository).delete(item)
        assertEquals("상품이 장바구니에서 제거되었습니다", response.message)
        assertEquals(0, response.totalPrice)
    }

    @Test
    fun `removeFromCart rejects item from another cart`() {
        // Verifies cart item ownership is checked before deletion.
        val member = member()
        val ownCart = Cart(id = 100L, member = member)
        val otherCart = Cart(id = 999L, member = member.copy(id = 2L, username = "other", email = "other@example.com"))
        val foreignItem = CartItem(id = 200L, cart = otherCart, product = product(), quantity = 1, price = 15_000)
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member))
        Mockito.`when`(cartRepository.findByMemberId(1L)).thenReturn(Optional.of(ownCart))
        Mockito.`when`(cartItemRepository.findById(200L)).thenReturn(Optional.of(foreignItem))

        val exception = assertFailsWith<IllegalArgumentException> {
            cartService.removeFromCart("buyer", 200L)
        }

        assertEquals("해당 아이템에 접근할 권한이 없습니다", exception.message)
        Mockito.verify(cartItemRepository, Mockito.never()).delete(foreignItem)
    }

    @Test
    fun `clearCart deletes all cart items and empties aggregate`() {
        // Verifies checkout cleanup removes persisted cart items and clears the in-memory relationship.
        val member = member()
        val cart = Cart(id = 100L, member = member)
        cart.items.add(CartItem(id = 200L, cart = cart, product = product(), quantity = 2, price = 15_000))
        Mockito.`when`(memberRepository.findByUsername("buyer")).thenReturn(Optional.of(member))
        Mockito.`when`(cartRepository.findByMemberId(1L)).thenReturn(Optional.of(cart))

        cartService.clearCart("buyer")

        Mockito.verify(cartItemRepository).deleteAll(anyValue<Iterable<CartItem>>())
        assertTrue(cart.items.isEmpty())
    }

    private fun member() = Member(
        id = 1L,
        username = "buyer",
        password = "encoded-password",
        name = "Buyer",
        email = "buyer@example.com"
    )

    private fun product() = Product(
        id = 10L,
        name = "Cart Product",
        price = 15_000,
        description = "Cart product description"
    )

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyValue(): T {
        Mockito.any<T>()
        return null as T
    }
}
