package gdfs.shopping.backend.controller

import gdfs.shopping.backend.dto.cart.AddToCartRequest
import gdfs.shopping.backend.dto.cart.CartResponse
import gdfs.shopping.backend.service.CartService
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cart")
@Tag(name = "장바구니", description = "장바구니 API")
@SecurityRequirement(name = "bearer-jwt")
class CartController(
    private val cartService: CartService
) {

    @PostMapping
    @Operation(summary = "장바구니에 상품 추가", description = "상품을 장바구니에 추가합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "추가 성공",
                content = [Content(schema = Schema(implementation = CartResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
        ]
    )
    fun addToCart(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: AddToCartRequest
    ): ResponseEntity<CartResponse> {
        return ResponseEntity.ok(cartService.addToCart(userDetails.username, request))
    }

    @GetMapping
    @Operation(summary = "장바구니 조회", description = "현재 로그인한 회원의 장바구니를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = CartResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
        ]
    )
    fun getCart(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<CartResponse> {
        return ResponseEntity.ok(cartService.getCart(userDetails.username))
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니에서 상품 제거", description = "장바구니에서 특정 상품을 제거합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "제거 성공",
                content = [Content(schema = Schema(implementation = CartResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "404", description = "아이템을 찾을 수 없음")
        ]
    )
    fun removeFromCart(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable cartItemId: Long
    ): ResponseEntity<CartResponse> {
        return ResponseEntity.ok(cartService.removeFromCart(userDetails.username, cartItemId))
    }
}
