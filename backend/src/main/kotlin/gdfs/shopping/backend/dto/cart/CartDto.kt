package gdfs.shopping.backend.dto.cart

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

@Schema(description = "장바구니 상품 추가 요청")
data class AddToCartRequest(
    @field:Schema(
        description = "상품 ID",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotNull(message = "상품 ID는 필수입니다")
    val productId: Long,

    @field:Schema(
        description = "수량",
        example = "2",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotNull(message = "수량은 필수입니다")
    @field:Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    val quantity: Int = 1
)

@Schema(description = "장바구니 아이템 응답")
data class CartItemResponse(
    @field:Schema(description = "장바구니 아이템 ID", example = "1")
    val id: Long,

    @field:Schema(description = "상품 ID", example = "1")
    val productId: Long,

    @field:Schema(description = "상품명", example = "스마트폰 케이스")
    val productName: String,

    @field:Schema(description = "상품 가격", example = "15000")
    val price: Int,

    @field:Schema(description = "수량", example = "2")
    val quantity: Int,

    @field:Schema(description = "총 가격", example = "30000")
    val totalPrice: Int
)

@Schema(description = "장바구니 응답")
data class CartResponse(
    @field:Schema(description = "장바구니 ID", example = "1")
    val id: Long,

    @field:Schema(description = "장바구니 아이템 목록")
    val items: List<CartItemResponse>,

    @field:Schema(description = "총 상품 금액", example = "45000")
    val totalPrice: Int,

    @field:Schema(description = "메시지", example = "장바구니에 추가되었습니다")
    val message: String? = null
)
