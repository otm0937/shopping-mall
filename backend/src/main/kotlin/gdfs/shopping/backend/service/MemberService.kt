package gdfs.shopping.backend.service

import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.member.MemberRepository
import gdfs.shopping.backend.dto.AuthResponse
import gdfs.shopping.backend.dto.LoginRequest
import gdfs.shopping.backend.dto.SignupRequest
import gdfs.shopping.backend.security.JwtTokenProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) : UserDetailsService {

    @Transactional
    fun signup(request: SignupRequest): AuthResponse {
        if (memberRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("이미 사용 중인 아이디입니다")
        }

        if (memberRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다")
        }

        val member = Member(
            username = request.username,
            password = passwordEncoder.encode(request.password)!!,
            name = request.name,
            email = request.email
        )

        memberRepository.save(member)

        return AuthResponse(
            message = "회원가입이 완료되었습니다",
            username = member.username,
            name = member.name
        )
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val member = memberRepository.findByUsername(request.username)
            .orElseThrow { BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다") }

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다")
        }

        val token = jwtTokenProvider.generateToken(member.username, member.role)

        return AuthResponse(
            message = "로그인 성공",
            username = member.username,
            name = member.name,
            token = token
        )
    }

    fun logout(): AuthResponse {
        return AuthResponse(message = "로그아웃 되었습니다")
    }

    override fun loadUserByUsername(username: String): UserDetails {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("사용자를 찾을 수 없습니다: $username") }

        return User.builder()
            .username(member.username)
            .password(member.password)
            .roles(member.role.name)
            .build()
    }
}
