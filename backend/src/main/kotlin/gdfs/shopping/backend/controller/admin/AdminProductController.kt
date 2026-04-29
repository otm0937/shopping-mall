package gdfs.shopping.backend.controller.admin

import gdfs.shopping.backend.dto.product.ProductCreateRequest
import gdfs.shopping.backend.dto.product.ProductResponse
import gdfs.shopping.backend.dto.product.ProductUpdateRequest
import gdfs.shopping.backend.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "관리자 - 상품", description = "상품 관리 API (관리자 전용)")
@SecurityRequirement(name = "bearer-jwt")
class AdminProductController(
    private val productService: ProductService
) {

    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "상품 생성", description = "새로운 상품을 생성합니다 (관리자 전용)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = ProductResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "관리자 권한 필요")
        ]
    )
    fun createProduct(
        @Valid
        @RequestParam
        @Parameter(description = "상품명", required = true)
        name: String,

        @Valid
        @RequestParam
        @Parameter(description = "가격", required = true)
        @NotNull
        @Min(0)
        price: Int,

        @Valid
        @RequestParam
        @Parameter(description = "상품 설명", required = true)
        description: String,

        @RequestPart("image", required = false)
        @Parameter(description = "상품 이미지 파일", required = false)
        image: MultipartFile?
    ): ResponseEntity<ProductResponse> {
        val request = ProductCreateRequest(
            name = name,
            price = price,
            description = description
        )
        return ResponseEntity.ok(productService.createProduct(request, image))
    }

    @PutMapping(
        "/{id}",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "상품 수정", description = "기존 상품을 수정합니다 (관리자 전용)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(schema = Schema(implementation = ProductResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
        ]
    )
    fun updateProduct(
        @PathVariable id: Long,

        @Valid
        @RequestParam
        @Parameter(description = "상품명", required = true)
        name: String,

        @Valid
        @RequestParam
        @Parameter(description = "가격", required = true)
        @NotNull
        @Min(0)
        price: Int,

        @Valid
        @RequestParam
        @Parameter(description = "상품 설명", required = true)
        description: String,

        @RequestPart("image", required = false)
        @Parameter(description = "상품 이미지 파일", required = false)
        image: MultipartFile?
    ): ResponseEntity<ProductResponse> {
        val request = ProductUpdateRequest(
            name = name,
            price = price,
            description = description
        )
        return ResponseEntity.ok(productService.updateProduct(id, request, image))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다 (관리자 전용)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
        ]
    )
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }
}
