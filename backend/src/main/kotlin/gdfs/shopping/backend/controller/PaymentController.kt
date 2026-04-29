package gdfs.shopping.backend.controller

import gdfs.shopping.backend.dto.payment.PaymentHistoryResponse
import gdfs.shopping.backend.dto.payment.PaymentRequest
import gdfs.shopping.backend.dto.payment.PaymentResponse
import gdfs.shopping.backend.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payments")
@Tag(name = "결제", description = "결제 API")
@SecurityRequirement(name = "bearer-jwt")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping
    @Operation(summary = "결제 처리", description = "장바구니의 상품들을 결제합니다 (Mock 결제)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "결제 성공",
                content = [Content(schema = Schema(implementation = PaymentResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (장바구니 비어있음 등)"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
        ]
    )
    fun processPayment(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: PaymentRequest
    ): ResponseEntity<PaymentResponse> {
        return ResponseEntity.ok(paymentService.processPayment(userDetails.username, request))
    }

    @GetMapping
    @Operation(summary = "결제 내역 조회", description = "현재 로그인한 회원의 결제 내역을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = PaymentHistoryResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
        ]
    )
    fun getPaymentHistory(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<PaymentHistoryResponse>> {
        return ResponseEntity.ok(paymentService.getPaymentHistory(userDetails.username))
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "결제 상세 조회", description = "특정 결제의 상세 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = PaymentResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "404", description = "결제 내역을 찾을 수 없음")
        ]
    )
    fun getPaymentDetail(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable paymentId: Long
    ): ResponseEntity<PaymentResponse> {
        return ResponseEntity.ok(paymentService.getPaymentDetail(userDetails.username, paymentId))
    }
}
