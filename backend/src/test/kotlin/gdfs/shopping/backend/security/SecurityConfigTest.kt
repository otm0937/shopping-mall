package gdfs.shopping.backend.security

import gdfs.shopping.backend.domain.member.MemberRole
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Test
    fun `public product endpoint is accessible without token`() {
        // Verifies /api/products/** is permitted to anonymous users by SecurityConfig.
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk)
    }

    @Test
    fun `cart endpoint rejects anonymous users`() {
        // Verifies protected user endpoints require authentication when no Bearer token is supplied.
        mockMvc.perform(get("/api/cart"))
            .andExpect { result ->
                assertTrue(result.response.status == 401 || result.response.status == 403)
            }
    }

    @Test
    fun `cart endpoint accepts valid jwt token`() {
        // Verifies JwtAuthenticationFilter authenticates a valid token and exposes the username to controllers.
        val token = jwtTokenProvider.generateToken("root", MemberRole.ADMIN)

        mockMvc.perform(
            get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items").isArray)
    }

    @Test
    fun `admin endpoint rejects non admin jwt token`() {
        // Verifies /api/admin/** requires ADMIN authority and rejects authenticated users with USER role.
        val token = jwtTokenProvider.generateToken("regular-user", MemberRole.USER)

        mockMvc.perform(
            multipart("/api/admin/products")
                .param("name", "Forbidden Product")
                .param("price", "1000")
                .param("description", "Should not be created")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `admin endpoint accepts admin jwt token`() {
        // Verifies an ADMIN token passes security and reaches the real product creation endpoint.
        val token = jwtTokenProvider.generateToken("root", MemberRole.ADMIN)

        mockMvc.perform(
            multipart("/api/admin/products")
                .param("name", "Security Product")
                .param("price", "1000")
                .param("description", "Created by security integration test")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Security Product"))
            .andExpect(jsonPath("$.message").value("상품이 생성되었습니다"))
    }
}
