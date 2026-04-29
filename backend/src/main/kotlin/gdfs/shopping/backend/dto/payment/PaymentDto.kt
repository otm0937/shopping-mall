package gdfs.shopping.backend.dto.payment

import gdfs.shopping.backend.domain.payment.PaymentStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "결제 요청")
data class PaymentRequest(
    @field:Schema(description = "결제 수단", example = "CARD")
    val paymentMethod: String = "MOCK"
)

@Schema(description = "결제 아이템 응답")
data class PaymentItemResponse(
    @field:Schema(description = "결제 아이템 ID", example = "1")
    val id: Long,

    @field:Schema(description = "상품 ID", example = "1")
    val productId: Long,

    @field:Schema(description = "상품명", example = "스마트폰 케이스")
    val productName: String,

    @field:Schema(description = "수량", example = "2")
    val quantity: Int,

    @field:Schema(description = "단가", example = "15000")
    val unitPrice: Int,

    @field:Schema(description = "총 가격", example = "30000")
    val totalPrice: Int
)

@Schema(description = "결제 응답")
data class PaymentResponse(
    @field:Schema(description = "결제 ID", example = "1")
    val id: Long,

    @field:Schema(description = "총 결제 금액", example = "45000")
    val totalAmount: Int,

    @field:Schema(description = "결제 상태", example = "COMPLETED")
    val status: PaymentStatus,

    @field:Schema(description = "결제 상품 목록")
    val items: List<PaymentItemResponse>,

    @field:Schema(description = "결제 일시")
    val createdAt: LocalDateTime,

    @field:Schema(description = "메시지", example = "결제가 완료되었습니다")
    val message: String
)

@Schema(description = "결제 내역 조회 응답")
data class PaymentHistoryResponse(
    @field:Schema(description = "결제 ID", example = "1")
    val id: Long,

    @field:Schema(description = "총 결제 금액", example = "45000")
    val totalAmount: Int,

    @field:Schema(description = "총 상품 수량", example = "5")
    val totalQuantity: Int,

    @field:Schema(description = "결제 상태", example = "COMPLETED")
    val status: PaymentStatus,

    @field:Schema(description = "결제 일시")
    val createdAt: LocalDateTime
)
