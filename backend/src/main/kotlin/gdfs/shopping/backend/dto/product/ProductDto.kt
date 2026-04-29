package gdfs.shopping.backend.dto.product

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "상품 생성 요청")
data class ProductCreateRequest(
    @field:Schema(
        description = "상품명",
        example = "스마트폰 케이스",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "상품명은 필수입니다")
    val name: String,

    @field:Schema(
        description = "가격",
        example = "15000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotNull(message = "가격은 필수입니다")
    @field:Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    val price: Int,

    @field:Schema(
        description = "상품 설명",
        example = "고품질 실리콘 소재의 스마트폰 케이스입니다.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "상품 설명은 필수입니다")
    val description: String
)

@Schema(description = "상품 수정 요청")
data class ProductUpdateRequest(
    @field:Schema(
        description = "상품명",
        example = "스마트폰 케이스 (신형)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "상품명은 필수입니다")
    val name: String,

    @field:Schema(
        description = "가격",
        example = "18000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotNull(message = "가격은 필수입니다")
    @field:Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    val price: Int,

    @field:Schema(
        description = "상품 설명",
        example = "업그레이드된 신형 스마트폰 케이스입니다.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "상품 설명은 필수입니다")
    val description: String
)

@Schema(description = "상품 목록 응답")
data class ProductListResponse(
    @field:Schema(description = "상품 ID", example = "1")
    val id: Long,

    @field:Schema(description = "상품명", example = "스마트폰 케이스")
    val name: String,

    @field:Schema(description = "가격", example = "15000")
    val price: Int,

    @field:Schema(description = "이미지 URL", example = "/uploads/products/image.jpg")
    val imageUrl: String?
)

@Schema(description = "상품 상세 응답")
data class ProductDetailResponse(
    @field:Schema(description = "상품 ID", example = "1")
    val id: Long,

    @field:Schema(description = "상품명", example = "스마트폰 케이스")
    val name: String,

    @field:Schema(description = "가격", example = "15000")
    val price: Int,

    @field:Schema(description = "상품 설명", example = "고품질 실리콘 소재의 스마트폰 케이스입니다.")
    val description: String,

    @field:Schema(description = "이미지 URL", example = "/uploads/products/image.jpg")
    val imageUrl: String?
)

@Schema(description = "상품 생성/수정 응답")
data class ProductResponse(
    @field:Schema(description = "상품 ID", example = "1")
    val id: Long,

    @field:Schema(description = "상품명", example = "스마트폰 케이스")
    val name: String,

    @field:Schema(description = "가격", example = "15000")
    val price: Int,

    @field:Schema(description = "상품 설명", example = "고품질 실리콘 소재의 스마트폰 케이스입니다.")
    val description: String,

    @field:Schema(description = "이미지 URL", example = "/uploads/products/image.jpg")
    val imageUrl: String?,

    @field:Schema(description = "메시지", example = "상품이 생성되었습니다")
    val message: String
)
