package gdfs.shopping.backend.controller

import gdfs.shopping.backend.dto.AuthResponse
import gdfs.shopping.backend.dto.LoginRequest
import gdfs.shopping.backend.dto.SignupRequest
import gdfs.shopping.backend.service.MemberService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "회원 인증 API (로그인, 회원가입, 로그아웃)")
class AuthController(
    private val memberService: MemberService
) {

    @PostMapping(
        "/signup",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "회원가입",
        description = "새로운 회원을 등록합니다"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "회원가입 성공",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            ApiResponse(responseCode = "409", description = "이미 존재하는 아이디 또는 이메일")
        ]
    )
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<AuthResponse> {
        val response = memberService.signup(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping(
        "/login",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "로그인",
        description = "아이디와 비밀번호로 로그인합니다. JWT 토큰이 반환됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치")
        ]
    )
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<AuthResponse> {
        val response = memberService.login(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    @Operation(
        summary = "로그아웃",
        description = "클리언트에서 JWT 토큰을 삭제하세요"
    )
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그아웃 성공",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            )
        ]
    )
    fun logout(): ResponseEntity<AuthResponse> {
        val response = memberService.logout()
        return ResponseEntity.ok(response)
    }
}
