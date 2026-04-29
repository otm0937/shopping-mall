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
