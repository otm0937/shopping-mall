package gdfs.shopping.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "로그인 요청")
data class LoginRequest(
    @field:Schema(
        description = "사용자 아이디",
        example = "testuser",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "아이디는 필수입니다")
    val username: String,

    @field:Schema(
        description = "비밀번호",
        example = "password123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String
)

@Schema(description = "회원가입 요청")
data class SignupRequest(
    @field:Schema(
        description = "사용자 아이디 (4~20자)",
        example = "testuser",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "아이디는 필수입니다")
    @field:Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다")
    val username: String,

    @field:Schema(
        description = "비밀번호 (최소 6자)",
        example = "password123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    val password: String,

    @field:Schema(
        description = "이름",
        example = "홍길동",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "이름은 필수입니다")
    val name: String,

    @field:Schema(
        description = "이메일 주소",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String
)

@Schema(description = "인증 응답")
data class AuthResponse(
    @field:Schema(
        description = "응답 메시지",
        example = "로그인 성공",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    val message: String,

    @field:Schema(
        description = "사용자 아이디",
        example = "testuser",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    val username: String? = null,

    @field:Schema(
        description = "사용자 이름",
        example = "홍길동",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    val name: String? = null,

    @field:Schema(
        description = "JWT 토큰 (Bearer 스킴)",
        example = "eyJhbGciOiJIUzI1NiJ9...",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    val token: String? = null
)
