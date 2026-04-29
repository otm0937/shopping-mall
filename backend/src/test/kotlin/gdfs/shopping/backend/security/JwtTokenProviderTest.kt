package gdfs.shopping.backend.security

import gdfs.shopping.backend.config.JwtProperties
import gdfs.shopping.backend.domain.member.MemberRole
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JwtTokenProviderTest {

    @Test
    fun `generateToken creates valid token with username and role claims`() {
        // Verifies generated JWTs validate and expose the subject and role needed by JwtAuthenticationFilter.
        val provider = tokenProvider()

        val token = provider.generateToken("jwt-user", MemberRole.USER)

        assertTrue(provider.validateToken(token))
        assertEquals("jwt-user", provider.getUsernameFromToken(token))
        assertEquals(MemberRole.USER, provider.getRoleFromToken(token))
    }

    @Test
    fun `generateToken preserves admin role claim`() {
        // Verifies administrator tokens carry ADMIN so security can authorize admin endpoints.
        val provider = tokenProvider()

        val token = provider.generateToken("admin", MemberRole.ADMIN)

        assertEquals(MemberRole.ADMIN, provider.getRoleFromToken(token))
    }

    @Test
    fun `validateToken rejects malformed token`() {
        // Verifies random strings cannot be accepted as JWTs.
        val provider = tokenProvider()

        assertFalse(provider.validateToken("not-a-token"))
    }

    @Test
    fun `validateToken rejects tampered token`() {
        // Verifies signature validation fails when a token payload or signature is changed.
        val provider = tokenProvider()
        val token = provider.generateToken("jwt-user", MemberRole.USER)
        val tamperedToken = token.dropLast(1) + if (token.last() == 'a') 'b' else 'a'

        assertFalse(provider.validateToken(tamperedToken))
    }

    @Test
    fun `validateToken rejects expired token`() {
        // Verifies expiration is enforced by the JWT parser.
        val provider = tokenProvider(expiration = -1L)

        val expiredToken = provider.generateToken("expired-user", MemberRole.USER)

        assertFalse(provider.validateToken(expiredToken))
    }

    @Test
    fun `claim accessors throw for invalid token`() {
        // Verifies callers cannot extract trusted claims from invalid JWT strings.
        val provider = tokenProvider()

        assertFailsWith<Exception> {
            provider.getUsernameFromToken("not-a-token")
        }
        assertFailsWith<Exception> {
            provider.getRoleFromToken("not-a-token")
        }
    }

    private fun tokenProvider(expiration: Long = 86_400_000L): JwtTokenProvider {
        val properties = JwtProperties().apply {
            secret = "testSecretKeyForShoppingMallApplicationSecureKeyLength256"
            this.expiration = expiration
        }
        return JwtTokenProvider(properties)
    }
}
