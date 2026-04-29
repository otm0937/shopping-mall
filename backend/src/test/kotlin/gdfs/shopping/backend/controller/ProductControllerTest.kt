package gdfs.shopping.backend.controller

import gdfs.shopping.backend.dto.product.ProductDetailResponse
import gdfs.shopping.backend.dto.product.ProductListResponse
import gdfs.shopping.backend.security.JwtTokenProvider
import gdfs.shopping.backend.service.ProductService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ProductController::class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val productService: ProductService
) {

    @BeforeEach
    fun resetMocks() {
        Mockito.reset(productService)
    }

    @Test
    fun `getAllProducts endpoint returns product list`() {
        // Verifies GET /api/products serializes every product returned by the service.
        Mockito.`when`(productService.getAllProducts()).thenReturn(
            listOf(
                ProductListResponse(id = 1L, name = "Keyboard", price = 50_000, imageUrl = "/uploads/products/keyboard.png"),
                ProductListResponse(id = 2L, name = "Mouse", price = 20_000, imageUrl = null)
            )
        )

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Keyboard"))
            .andExpect(jsonPath("$[0].imageUrl").value("/uploads/products/keyboard.png"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].imageUrl").doesNotExist())
    }

    @Test
    fun `getAllProducts endpoint returns empty list`() {
        // Verifies an empty catalog is represented as an empty JSON array.
        Mockito.`when`(productService.getAllProducts()).thenReturn(emptyList())

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `getProduct endpoint returns product detail`() {
        // Verifies GET /api/products/{id} returns the detail DTO for the requested product ID.
        Mockito.`when`(productService.getProduct(1L)).thenReturn(
            ProductDetailResponse(
                id = 1L,
                name = "Keyboard",
                price = 50_000,
                description = "Mechanical keyboard",
                imageUrl = "/uploads/products/keyboard.png"
            )
        )

        mockMvc.perform(get("/api/products/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Keyboard"))
            .andExpect(jsonPath("$.description").value("Mechanical keyboard"))
    }

    @Test
    fun `getProduct endpoint rejects non numeric id`() {
        // Verifies path-variable type conversion failures produce a bad request before service lookup.
        mockMvc.perform(get("/api/products/not-a-number"))
            .andExpect(status().isBadRequest)

        Mockito.verifyNoInteractions(productService)
    }

    @TestConfiguration
    class MockConfig {
        @Bean
        fun productService(): ProductService = Mockito.mock(ProductService::class.java)

        @Bean
        fun jwtTokenProvider(): JwtTokenProvider = Mockito.mock(JwtTokenProvider::class.java)
    }
}
