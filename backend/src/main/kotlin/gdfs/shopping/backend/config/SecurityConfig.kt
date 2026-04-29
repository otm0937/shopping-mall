package gdfs.shopping.backend.config

import gdfs.shopping.backend.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Spring Security 설정 클래스
 * 
 * JWT 기반 Stateless 인증을 구현합니다.
 * 모든 요청은 JWT 토큰을 통해 인증되며, 세션은 사용하지 않습니다.
 * 
 * 보안 정책:
 * 1. CSRF 비활성화 (JWT 사용으로 인해 불필요)
 * 2. 세션 Stateless 설정
 * 3. URL 기반 접근 권한 관리
 * 4. JWT 필터를 UsernamePasswordAuthenticationFilter 전에 추가
 * 
 * @property userDetailsService 사용자 정보 로드 서비스
 * @property passwordEncoder 비밀번호 암호화 인코더
 * @property jwtAuthenticationFilter JWT 인증 필터
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    /**
     * AuthenticationManager 빈 생성
     * 
     * DaoAuthenticationProvider를 사용하여 데이터베이스 기반 인증을 처리합니다.
     * 사용자 이름과 비밀번호를 검증하여 인증을 수행합니다.
     * 
     * @return AuthenticationManager 인스턴스
     */
    @Bean
    fun authenticationManager(): AuthenticationManager {
        val authProvider = DaoAuthenticationProvider(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder)
        return ProviderManager(authProvider)
    }

    /**
     * SecurityFilterChain 빈 생성
     * 
     * HTTP 요청에 대한 보안 필터 체인을 구성합니다.
     * 
     * 접근 권한:
     * - /api/auth/...: 누구나 접근 가능 (로그인, 회원가입)
     * - /api/products/...: 누구나 접근 가능 (상품 조회)
     * - /uploads/...: 누구나 접근 가능 (상품 이미지)
     * - /swagger-ui/...: 누구나 접근 가능 (API 문서)
     * - /api/admin/...: ADMIN 권한 필요
     * - 그 외: 인증 필요
     * 
     * @param http HttpSecurity 인스턴스
     * @return SecurityFilterChain 설정된 필터 체인
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF 비활성화 (JWT 사용으로 인해 불필요)
            .csrf { it.disable() }
            // 세션 Stateless 설정 (JWT 사용)
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // URL 기반 접근 권한 설정
            .authorizeHttpRequests { auth ->
                auth
                    // 인증 API는 누구나 접근 가능
                    .requestMatchers("/api/auth/**").permitAll()
                    // 상품 조회 API는 누구나 접근 가능
                    .requestMatchers("/api/products/**").permitAll()
                    // 업로드된 파일은 누구나 접근 가능
                    .requestMatchers("/uploads/**").permitAll()
                    // Actuator는 누구나 접근 가능
                    .requestMatchers("/actuator/**").permitAll()
                    // Swagger UI는 누구나 접근 가능
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                    // 관리자 API는 ADMIN 권한 필요
                    .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                    // 그 외 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            }
            // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 전에 실행)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
