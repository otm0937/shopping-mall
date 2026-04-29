package gdfs.shopping.backend.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Shopping Mall API",
        version = "1.0.0",
        description = "쇼핑몰 백엔드 API 문서",
        contact = Contact(name = "Demo Team")
    )
)
@SecurityScheme(
    name = "bearer-jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT 토큰 인증 - 로그인 후 받은 토큰을 입력하세요"
)
class OpenApiConfig
