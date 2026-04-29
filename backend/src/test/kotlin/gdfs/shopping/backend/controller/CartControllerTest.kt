package gdfs.shopping.backend.controller

import gdfs.shopping.backend.dto.cart.AddToCartRequest
import gdfs.shopping.backend.dto.cart.CartItemResponse
import gdfs.shopping.backend.dto.cart.CartResponse
import gdfs.shopping.backend.security.JwtTokenProvider
import gdfs.shopping.backend.service.CartService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@WebMvcTest(CartController::class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val cartService: CartService
) {

    @BeforeEach
    fun resetMocks() {
        Mockito.reset(cartService)
    }

    @Test
    @WithMockUser(username = "buyer")
    fun `addToCart endpoint uses authenticated username and returns cart`() {
        // Verifies POST /api/cart binds @AuthenticationPrincipal and forwards the request to CartService.
        val request = AddToCartRequest(productId = 10L, quantity = 2)
        Mockito.`when`(cartService.addToCart("buyer", request)).thenReturn(cartResponse(message = "장바구니에 추가되었습니다"))

        mockMvc.perform(
            post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.message").value("장바구니에 추가되었습니다"))
            .andExpect(jsonPath("$.items[0].productId").value(10))
            .andExpect(jsonPath("$.totalPrice").value(30000))

        Mockito.verify(cartService).addToCart("buyer", request)
    }

    @Test
    @WithMockUser(username = "buyer")
    fun `addToCart endpoint rejects invalid quantity`() {
        // Verifies quantity validation prevents zero-quantity cart additions before service invocation.
        val invalidJson = """
            {
              "productId": 10,
              "quantity": 0
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)

        Mockito.verifyNoInteractions(cartService)
    }

    @Test
    @WithMockUser(username = "buyer")
    fun `getCart endpoint returns authenticated users cart`() {
        // Verifies GET /api/cart reads the username from the security principal.
        Mockito.`when`(cartService.getCart("buyer")).thenReturn(cartResponse(message = null))

        mockMvc.perform(get("/api/cart"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.items[0].productName").value("Cart Product"))
            .andExpect(jsonPath("$.message").doesNotExist())

        Mockito.verify(cartService).getCart("buyer")
    }

    @Test
    @WithMockUser(username = "buyer")
    fun `removeFromCart endpoint deletes requested item`() {
        // Verifies DELETE /api/cart/items/{cartItemId} forwards the authenticated username and path variable.
        Mockito.`when`(cartService.removeFromCart("buyer", 200L)).thenReturn(
            CartResponse(id = 100L, items = emptyList(), totalPrice = 0, message = "상품이 장바구니에서 제거되었습니다")
        )

        mockMvc.perform(delete("/api/cart/items/200"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.items").isEmpty)
            .andExpect(jsonPath("$.message").value("상품이 장바구니에서 제거되었습니다"))

        Mockito.verify(cartService).removeFromCart("buyer", 200L)
    }

    private fun cartResponse(message: String?) = CartResponse(
        id = 100L,
        items = listOf(
            CartItemResponse(
                id = 200L,
                productId = 10L,
                productName = "Cart Product",
                price = 15_000,
                quantity = 2,
                totalPrice = 30_000
            )
        ),
        totalPrice = 30_000,
        message = message
    )

    @TestConfiguration
    class MockConfig {
        @Bean
        fun cartService(): CartService = Mockito.mock(CartService::class.java)

        @Bean
        fun jwtTokenProvider(): JwtTokenProvider = Mockito.mock(JwtTokenProvider::class.java)
    }
}
