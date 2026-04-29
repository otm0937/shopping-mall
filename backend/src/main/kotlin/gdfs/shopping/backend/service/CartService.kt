package gdfs.shopping.backend.service

import gdfs.shopping.backend.domain.cart.Cart
import gdfs.shopping.backend.domain.cart.CartItem
import gdfs.shopping.backend.domain.cart.CartItemRepository
import gdfs.shopping.backend.domain.cart.CartRepository
import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.member.MemberRepository
import gdfs.shopping.backend.domain.product.ProductRepository
import gdfs.shopping.backend.dto.cart.AddToCartRequest
import gdfs.shopping.backend.dto.cart.CartItemResponse
import gdfs.shopping.backend.dto.cart.CartResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val memberRepository: MemberRepository
) {

    @Transactional
    fun addToCart(username: String, request: AddToCartRequest): CartResponse {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { IllegalArgumentException("회원을 찾을 수 없습니다") }

        val product = productRepository.findById(request.productId)
            .orElseThrow { IllegalArgumentException("상품을 찾을 수 없습니다: ${request.productId}") }

        val cart = cartRepository.findByMemberId(member.id!!)
            .orElseGet { cartRepository.save(Cart(member = member)) }

        val existingItem = cartItemRepository.findByCartIdAndProductId(cart.id!!, product.id!!)

        if (existingItem.isPresent) {
            val item = existingItem.get()
            item.quantity += request.quantity
            cartItemRepository.save(item)
        } else {
            val cartItem = CartItem(
                cart = cart,
                product = product,
                quantity = request.quantity,
                price = product.price
            )
            cartItemRepository.save(cartItem)
        }

        return getCartResponse(cart, "장바구니에 추가되었습니다")
    }

    @Transactional(readOnly = true)
    fun getCart(username: String): CartResponse {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { IllegalArgumentException("회원을 찾을 수 없습니다") }

        val cart = cartRepository.findByMemberId(member.id!!)
            .orElseGet { cartRepository.save(Cart(member = member)) }

        return getCartResponse(cart)
    }

    @Transactional
    fun removeFromCart(username: String, cartItemId: Long): CartResponse {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { IllegalArgumentException("회원을 찾을 수 없습니다") }

        val cart = cartRepository.findByMemberId(member.id!!)
            .orElseThrow { IllegalArgumentException("장바구니를 찾을 수 없습니다") }

        val cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow { IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다: $cartItemId") }

        if (cartItem.cart.id != cart.id) {
            throw IllegalArgumentException("해당 아이템에 접근할 권한이 없습니다")
        }

        cartItemRepository.delete(cartItem)

        return getCartResponse(cart, "상품이 장바구니에서 제거되었습니다")
    }

    @Transactional
    fun clearCart(username: String) {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { IllegalArgumentException("회원을 찾을 수 없습니다") }

        val cart = cartRepository.findByMemberId(member.id!!)
            .orElseThrow { IllegalArgumentException("장바구니를 찾을 수 없습니다") }

        cartItemRepository.deleteAll(cart.items)
        cart.items.clear()
    }

    private fun getCartResponse(cart: Cart, message: String? = null): CartResponse {
        val items = cartItemRepository.findAll()
            .filter { it.cart.id == cart.id }
            .map { item ->
                CartItemResponse(
                    id = item.id!!,
                    productId = item.product.id!!,
                    productName = item.product.name,
                    price = item.price,
                    quantity = item.quantity,
                    totalPrice = item.getTotalPrice()
                )
            }

        return CartResponse(
            id = cart.id!!,
            items = items,
            totalPrice = items.sumOf { it.totalPrice },
            message = message
        )
    }
}
