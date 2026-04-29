package gdfs.shopping.backend.controller

import gdfs.shopping.backend.controller.admin.AdminProductController
import gdfs.shopping.backend.dto.product.ProductResponse
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
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AdminProductController::class)
@AutoConfigureMockMvc(addFilters = false)
class AdminProductControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val productService: ProductService
) {

    @BeforeEach
    fun resetMocks() {
        Mockito.reset(productService)
    }

    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
    fun `createProduct endpoint accepts multipart form data`() {
        // Verifies admin product creation binds form fields and optional image before returning ProductResponse.
        Mockito.`when`(productService.createProduct(anyValue(), anyValue())).thenReturn(
            productResponse(id = 1L, name = "Keyboard", message = "상품이 생성되었습니다")
        )

        mockMvc.perform(
            multipart("/api/admin/products")
                .file(imageFile())
                .param("name", "Keyboard")
                .param("price", "50000")
                .param("description", "Mechanical keyboard")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Keyboard"))
            .andExpect(jsonPath("$.message").value("상품이 생성되었습니다"))
    }

    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
    fun `createProduct endpoint rejects missing price parameter`() {
        // Verifies required multipart form fields are validated by Spring MVC before service invocation.
        mockMvc.perform(
            multipart("/api/admin/products")
                .param("name", "Keyboard")
                .param("description", "Mechanical keyboard")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isBadRequest)

        Mockito.verifyNoInteractions(productService)
    }

    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
    fun `updateProduct endpoint accepts multipart form data`() {
        // Verifies admin product update binds the path ID and multipart form fields.
        Mockito.`when`(productService.updateProduct(Mockito.eq(1L), anyValue(), anyValue())).thenReturn(
            productResponse(id = 1L, name = "Updated Keyboard", message = "상품이 수정되었습니다")
        )

        mockMvc.perform(
            multipart(HttpMethod.PUT, "/api/admin/products/{id}", 1L)
                .file(imageFile())
                .param("name", "Updated Keyboard")
                .param("price", "55000")
                .param("description", "Updated description")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Updated Keyboard"))
            .andExpect(jsonPath("$.message").value("상품이 수정되었습니다"))
    }

    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
    fun `updateProduct endpoint rejects missing name parameter`() {
        // Verifies update requests missing required form fields fail before service invocation.
        mockMvc.perform(
            multipart(HttpMethod.PUT, "/api/admin/products/{id}", 1L)
                .param("price", "55000")
                .param("description", "Updated description")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isBadRequest)

        Mockito.verifyNoInteractions(productService)
    }

    @Test
    @WithMockUser(username = "admin", authorities = ["ADMIN"])
    fun `deleteProduct endpoint returns no content`() {
        // Verifies admin product deletion delegates to ProductService and returns HTTP 204.
        mockMvc.perform(delete("/api/admin/products/1"))
            .andExpect(status().isNoContent)

        Mockito.verify(productService).deleteProduct(1L)
    }

    private fun productResponse(id: Long, name: String, message: String) = ProductResponse(
        id = id,
        name = name,
        price = 50_000,
        description = "$name description",
        imageUrl = "/uploads/products/product.png",
        message = message
    )

    private fun imageFile() = MockMultipartFile(
        "image",
        "product.png",
        "image/png",
        byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    )

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyValue(): T {
        Mockito.any<T>()
        return null as T
    }

    @TestConfiguration
    class MockConfig {
        @Bean
        fun productService(): ProductService = Mockito.mock(ProductService::class.java)

        @Bean
        fun jwtTokenProvider(): JwtTokenProvider = Mockito.mock(JwtTokenProvider::class.java)
    }
}
