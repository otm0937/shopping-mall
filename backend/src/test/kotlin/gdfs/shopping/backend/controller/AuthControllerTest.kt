package gdfs.shopping.backend.controller

import gdfs.shopping.backend.dto.AuthResponse
import gdfs.shopping.backend.dto.LoginRequest
import gdfs.shopping.backend.dto.SignupRequest
import gdfs.shopping.backend.security.JwtTokenProvider
import gdfs.shopping.backend.service.MemberService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val memberService: MemberService
) {

    @BeforeEach
    fun resetMocks() {
        Mockito.reset(memberService)
    }

    @Test
    fun `signup endpoint returns created member response`() {
        // Verifies POST /api/auth/signup accepts a valid JSON body and returns the service response.
        val request = SignupRequest("newuser", "password123", "New User", "newuser@example.com")
        Mockito.`when`(memberService.signup(request)).thenReturn(
            AuthResponse(message = "회원가입이 완료되었습니다", username = "newuser", name = "New User")
        )

        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다"))
            .andExpect(jsonPath("$.username").value("newuser"))
            .andExpect(jsonPath("$.name").value("New User"))
            .andExpect(jsonPath("$.token").doesNotExist())
    }

    @Test
    fun `signup endpoint rejects invalid request body`() {
        // Verifies bean validation rejects blank username, short password, blank name, and invalid email before service call.
        val invalidJson = """
            {
              "username": "",
              "password": "123",
              "name": "",
              "email": "not-an-email"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)

        Mockito.verifyNoInteractions(memberService)
    }

    @Test
    fun `login endpoint returns jwt token response`() {
        // Verifies POST /api/auth/login accepts valid credentials and serializes the JWT response.
        val request = LoginRequest("newuser", "password123")
        Mockito.`when`(memberService.login(request)).thenReturn(
            AuthResponse(message = "로그인 성공", username = "newuser", name = "New User", token = "jwt-token")
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("로그인 성공"))
            .andExpect(jsonPath("$.username").value("newuser"))
            .andExpect(jsonPath("$.token").value("jwt-token"))
    }

    @Test
    fun `login endpoint rejects blank password`() {
        // Verifies login validation catches malformed input before authentication logic runs.
        val invalidJson = """
            {
              "username": "newuser",
              "password": ""
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)

        Mockito.verifyNoInteractions(memberService)
    }

    @Test
    fun `logout endpoint returns stateless logout response`() {
        // Verifies POST /api/auth/logout delegates to the stateless logout service method.
        Mockito.`when`(memberService.logout()).thenReturn(AuthResponse(message = "로그아웃 되었습니다"))

        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("로그아웃 되었습니다"))
    }

    @TestConfiguration
    class MockConfig {
        @Bean
        fun memberService(): MemberService = Mockito.mock(MemberService::class.java)

        @Bean
        fun jwtTokenProvider(): JwtTokenProvider = Mockito.mock(JwtTokenProvider::class.java)
    }
}
