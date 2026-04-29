package gdfs.shopping.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 쇼핑몰 백엔드 애플리케이션의 메인 진입점
 * 
 * Spring Boot 기반의 REST API 서버로 다음 기능을 제공합니다:
 * - 사용자 인증 (회원가입, 로그인, JWT 토큰 기반 인증)
 * - 상품 관리 (조회, 등록, 수정, 삭제)
 * - 장바구니 기능 (추가, 조회, 삭제)
 * - 결제 처리 (Mock 결제, 결제 내역 조회)
 * 
 * 기술 스택:
 * - Kotlin 1.9+
 * - Spring Boot 3.2+
 * - Spring Data JPA
 * - Spring Security (JWT)
 * - MariaDB
 * 
 * @author Shopping Mall Team
 * @since 1.0.0
 */
@SpringBootApplication
class BackendApplication

/**
 * 애플리케이션 시작점
 * 
 * @param args 커맨드 라인 인자
 */
fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
