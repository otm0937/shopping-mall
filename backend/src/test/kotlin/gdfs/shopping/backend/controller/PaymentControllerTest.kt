package gdfs.shopping.backend.controller

import gdfs.shopping.backend.domain.payment.PaymentStatus
import gdfs.shopping.backend.dto.payment.PaymentHistoryResponse
import gdfs.shopping.backend.dto.payment.PaymentItemResponse
import gdfs.shopping.backend.dto.payment.PaymentRequest
import gdfs.shopping.backend.dto.payment.PaymentResponse
import gdfs.shopping.backend.security.JwtTokenProvider
import gdfs.shopping.backend.service.PaymentService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime

@WebMvcTest(PaymentController::class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val paymentService: PaymentService
) {

    @BeforeEach
    fun resetMocks() {
        Mockito.reset(paymentService)
    }

    @Test
    @WithMockUser(username = "buyer")
    fun `processPayment endpoint uses authenticated username`() {
        // Verifies POST /api/payments forwards the authenticated username and request body to PaymentService.
        val request = PaymentRequest(paymentMethod = "MOCK")
        Mockito.`when`(paymentService.processPayment("buyer", request)).thenReturn(paymentResponse(message = "결제가 완료되었습니다"))

        mockMvc.perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(500))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.totalAmount").value(30000))
            .andExpect(jsonPath("$.items[0].productName").value("Paid Product"))

        Mockito.verify(paymentService).processPayment("buyer", request)
    }

    @Test
    @WithMockUser(username = "buyer")
    fun `processPayment endpoint accepts empty body with default mock payment method`() {
        // Verifies PaymentRequest default value allows clients to submit an empty JSON object for mock payments.
        Mockito.`when`(paymentService.processPayment("buyer", PaymentRequest())).thenReturn(paymentResponse(message = "결제가 완료되었습니다"))

        mockMvc.perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("결제가 완료되었습니다"))
    }

    @Test
    @WithMockUser(username = "buyer")
    fun `getPaymentHistory endpoint returns authenticated users payments`() {
        // Verifies GET /api/payments serializes the payment history list returned for the principal username.
        Mockito.`when`(paymentService.getPaymentHistory("buyer")).thenReturn(
            listOf(
                PaymentHistoryResponse(
                    id = 500L,
                    totalAmount = 30_000,
                    totalQuantity = 2,
                    status = PaymentStatus.COMPLETED,
                    createdAt = LocalDateTime.parse("2024-01-01T12:00:00")
                )
            )
        )

        mockMvc.perform(get("/api/payments"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(500))
            .andExpect(jsonPath("$[0].totalAmount").value(30000))
            .andExpect(jsonPath("$[0].totalQuantity").value(2))
            .andExpect(jsonPath("$[0].status").value("COMPLETED"))
    }

    @Test
    @WithMockUser(username = "buyer")
    fun `getPaymentDetail endpoint returns requested payment`() {
        // Verifies GET /api/payments/{paymentId} forwards the authenticated username and path variable.
        Mockito.`when`(paymentService.getPaymentDetail("buyer", 500L)).thenReturn(paymentResponse(message = "결제 내역 조회 성공"))

        mockMvc.perform(get("/api/payments/500"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(500))
            .andExpect(jsonPath("$.message").value("결제 내역 조회 성공"))

        Mockito.verify(paymentService).getPaymentDetail("buyer", 500L)
    }

    @Test
    @WithMockUser(username = "buyer")
    fun `getPaymentDetail endpoint rejects non numeric id`() {
        // Verifies invalid payment ID path variables fail before service lookup.
        mockMvc.perform(get("/api/payments/not-a-number"))
            .andExpect(status().isBadRequest)

        Mockito.verifyNoInteractions(paymentService)
    }

    private fun paymentResponse(message: String) = PaymentResponse(
        id = 500L,
        totalAmount = 30_000,
        status = PaymentStatus.COMPLETED,
        items = listOf(
            PaymentItemResponse(
                id = 600L,
                productId = 10L,
                productName = "Paid Product",
                quantity = 2,
                unitPrice = 15_000,
                totalPrice = 30_000
            )
        ),
        createdAt = LocalDateTime.parse("2024-01-01T12:00:00"),
        message = message
    )

    @TestConfiguration
    class MockConfig {
        @Bean
        fun paymentService(): PaymentService = Mockito.mock(PaymentService::class.java)

        @Bean
        fun jwtTokenProvider(): JwtTokenProvider = Mockito.mock(JwtTokenProvider::class.java)
    }
}
