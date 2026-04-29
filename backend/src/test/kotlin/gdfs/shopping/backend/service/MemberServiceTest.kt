package gdfs.shopping.backend.service

import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.member.MemberRepository
import gdfs.shopping.backend.domain.member.MemberRole
import gdfs.shopping.backend.dto.LoginRequest
import gdfs.shopping.backend.dto.SignupRequest
import gdfs.shopping.backend.security.JwtTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class MemberServiceTest {

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var memberService: MemberService

    @BeforeEach
    fun setUp() {
        memberService = MemberService(memberRepository, passwordEncoder, jwtTokenProvider)
    }

    @Test
    fun `signup saves encoded member and returns signup response`() {
        // Verifies successful signup checks duplicates, encodes the password, saves the member, and omits a token.
        val request = SignupRequest(
            username = "newuser",
            password = "plain-password",
            name = "New User",
            email = "newuser@example.com"
        )
        Mockito.`when`(memberRepository.existsByUsername("newuser")).thenReturn(false)
        Mockito.`when`(memberRepository.existsByEmail("newuser@example.com")).thenReturn(false)
        Mockito.`when`(passwordEncoder.encode("plain-password")).thenReturn("encoded-password")

        val response = memberService.signup(request)

        val memberCaptor = ArgumentCaptor.forClass(Member::class.java)
        Mockito.verify(memberRepository).save(memberCaptor.capture())
        assertEquals("회원가입이 완료되었습니다", response.message)
        assertEquals("newuser", response.username)
        assertEquals("New User", response.name)
        assertEquals(null, response.token)
        assertEquals("encoded-password", memberCaptor.value.password)
        assertEquals(MemberRole.USER, memberCaptor.value.role)
    }

    @Test
    fun `signup rejects duplicate username`() {
        // Verifies duplicate usernames fail before password encoding or repository save.
        val request = signupRequest(username = "taken", email = "available@example.com")
        Mockito.`when`(memberRepository.existsByUsername("taken")).thenReturn(true)

        val exception = assertFailsWith<IllegalArgumentException> {
            memberService.signup(request)
        }

        assertEquals("이미 사용 중인 아이디입니다", exception.message)
        Mockito.verify(passwordEncoder, Mockito.never()).encode(Mockito.anyString())
        Mockito.verify(memberRepository, Mockito.never()).save(anyValue())
    }

    @Test
    fun `signup rejects duplicate email`() {
        // Verifies duplicate emails fail after username passes and before member creation.
        val request = signupRequest(username = "available", email = "taken@example.com")
        Mockito.`when`(memberRepository.existsByUsername("available")).thenReturn(false)
        Mockito.`when`(memberRepository.existsByEmail("taken@example.com")).thenReturn(true)

        val exception = assertFailsWith<IllegalArgumentException> {
            memberService.signup(request)
        }

        assertEquals("이미 사용 중인 이메일입니다", exception.message)
        Mockito.verify(passwordEncoder, Mockito.never()).encode(Mockito.anyString())
        Mockito.verify(memberRepository, Mockito.never()).save(anyValue())
    }

    @Test
    fun `login returns jwt token when credentials match`() {
        // Verifies successful login validates the password and delegates token creation with the member role.
        val member = member(username = "login-user", password = "encoded-password")
        Mockito.`when`(memberRepository.findByUsername("login-user")).thenReturn(Optional.of(member))
        Mockito.`when`(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true)
        Mockito.`when`(jwtTokenProvider.generateToken("login-user", MemberRole.USER)).thenReturn("jwt-token")

        val response = memberService.login(LoginRequest("login-user", "plain-password"))

        assertEquals("로그인 성공", response.message)
        assertEquals("login-user", response.username)
        assertEquals("Login User", response.name)
        assertEquals("jwt-token", response.token)
    }

    @Test
    fun `login rejects unknown username`() {
        // Verifies login hides whether the username exists by returning a bad-credentials error.
        Mockito.`when`(memberRepository.findByUsername("missing-user")).thenReturn(Optional.empty())

        val exception = assertFailsWith<BadCredentialsException> {
            memberService.login(LoginRequest("missing-user", "plain-password"))
        }

        assertEquals("아이디 또는 비밀번호가 올바르지 않습니다", exception.message)
        Mockito.verify(jwtTokenProvider, Mockito.never()).generateToken(Mockito.anyString(), anyValue())
    }

    @Test
    fun `login rejects wrong password`() {
        // Verifies an existing member cannot log in when password verification fails.
        val member = member(username = "login-user", password = "encoded-password")
        Mockito.`when`(memberRepository.findByUsername("login-user")).thenReturn(Optional.of(member))
        Mockito.`when`(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false)

        val exception = assertFailsWith<BadCredentialsException> {
            memberService.login(LoginRequest("login-user", "wrong-password"))
        }

        assertEquals("아이디 또는 비밀번호가 올바르지 않습니다", exception.message)
        Mockito.verify(jwtTokenProvider, Mockito.never()).generateToken(Mockito.anyString(), anyValue())
    }

    @Test
    fun `logout returns stateless logout message`() {
        // Verifies logout is intentionally stateless and only returns client guidance.
        val response = memberService.logout()

        assertEquals("로그아웃 되었습니다", response.message)
    }

    @Test
    fun `loadUserByUsername maps member to spring security user details`() {
        // Verifies Spring Security can load persisted members and receive the role-based authority string.
        val admin = member(username = "admin", role = MemberRole.ADMIN)
        Mockito.`when`(memberRepository.findByUsername("admin")).thenReturn(Optional.of(admin))

        val userDetails = memberService.loadUserByUsername("admin")

        assertEquals("admin", userDetails.username)
        assertEquals(admin.password, userDetails.password)
        assertTrue(userDetails.authorities.any { it.authority == "ROLE_ADMIN" })
    }

    @Test
    fun `loadUserByUsername rejects missing user`() {
        // Verifies Spring Security receives UsernameNotFoundException for missing members.
        Mockito.`when`(memberRepository.findByUsername("ghost")).thenReturn(Optional.empty())

        val exception = assertFailsWith<UsernameNotFoundException> {
            memberService.loadUserByUsername("ghost")
        }

        assertEquals("사용자를 찾을 수 없습니다: ghost", exception.message)
    }

    private fun signupRequest(username: String, email: String) = SignupRequest(
        username = username,
        password = "plain-password",
        name = "Signup User",
        email = email
    )

    private fun member(
        username: String,
        password: String = "encoded-password",
        role: MemberRole = MemberRole.USER
    ) = Member(
        id = 1L,
        username = username,
        password = password,
        name = username.split("-").joinToString(" ") { it.replaceFirstChar(Char::uppercase) },
        email = "$username@example.com",
        role = role
    )

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyValue(): T {
        Mockito.any<T>()
        return null as T
    }
}
