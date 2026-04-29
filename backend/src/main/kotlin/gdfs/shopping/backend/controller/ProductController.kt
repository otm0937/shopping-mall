package gdfs.shopping.backend.controller

import gdfs.shopping.backend.dto.product.ProductDetailResponse
import gdfs.shopping.backend.dto.product.ProductListResponse
import gdfs.shopping.backend.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
@Tag(name = "상품", description = "상품 조회 API")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "모든 상품의 목록을 조회합니다 (사진, 이름, 가격)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = ProductListResponse::class))]
            )
        ]
    )
    fun getAllProducts(): ResponseEntity<List<ProductListResponse>> {
        return ResponseEntity.ok(productService.getAllProducts())
    }

    @GetMapping("/{id}")
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다 (사진, 이름, 가격, 설명)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = ProductDetailResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
        ]
    )
    fun getProduct(@PathVariable id: Long): ResponseEntity<ProductDetailResponse> {
        return ResponseEntity.ok(productService.getProduct(id))
    }
}
