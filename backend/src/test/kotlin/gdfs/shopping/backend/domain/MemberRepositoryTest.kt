package gdfs.shopping.backend.domain

import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.member.MemberRepository
import gdfs.shopping.backend.domain.member.MemberRole
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@DataJpaTest
@ActiveProfiles("test")
class MemberRepositoryTest @Autowired constructor(
    private val memberRepository: MemberRepository
) {

    @Test
    fun `saves and finds member by generated id`() {
        // Verifies basic CRUD persistence assigns an ID and retrieves the same member data.
        val saved = memberRepository.save(member(username = "saved-user", email = "saved@example.com"))

        val found = memberRepository.findById(saved.id!!)

        assertTrue(found.isPresent)
        assertEquals("saved-user", found.get().username)
        assertEquals("saved@example.com", found.get().email)
    }

    @Test
    fun `findByUsername returns matching member`() {
        // Verifies login and security lookups can find a persisted member by username.
        memberRepository.save(member(username = "lookup-user", email = "lookup@example.com"))

        val found = memberRepository.findByUsername("lookup-user")

        assertTrue(found.isPresent)
        assertEquals("Lookup User", found.get().name)
    }

    @Test
    fun `exists checks detect username and email duplicates`() {
        // Verifies signup duplicate checks are backed by repository existence queries.
        memberRepository.save(member(username = "duplicate-user", email = "duplicate@example.com"))

        assertTrue(memberRepository.existsByUsername("duplicate-user"))
        assertTrue(memberRepository.existsByEmail("duplicate@example.com"))
        assertFalse(memberRepository.existsByUsername("missing-user"))
        assertFalse(memberRepository.existsByEmail("missing@example.com"))
    }

    @Test
    fun `unique username constraint rejects duplicate usernames`() {
        // Verifies the database enforces username uniqueness even if service validation is bypassed.
        memberRepository.saveAndFlush(member(username = "unique-user", email = "first@example.com"))

        assertFailsWith<DataIntegrityViolationException> {
            memberRepository.saveAndFlush(member(username = "unique-user", email = "second@example.com"))
        }
    }

    @Test
    fun `unique email constraint rejects duplicate emails`() {
        // Verifies the database enforces email uniqueness even if service validation is bypassed.
        memberRepository.saveAndFlush(member(username = "email-user-1", email = "same@example.com"))

        assertFailsWith<DataIntegrityViolationException> {
            memberRepository.saveAndFlush(member(username = "email-user-2", email = "same@example.com"))
        }
    }

    @Test
    fun `persists admin role`() {
        // Verifies administrator accounts keep their role through JPA persistence.
        val saved = memberRepository.save(member(username = "admin-user", email = "admin@example.com", role = MemberRole.ADMIN))

        val found = memberRepository.findById(saved.id!!).get()

        assertEquals(MemberRole.ADMIN, found.role)
    }

    private fun member(
        username: String,
        email: String,
        role: MemberRole = MemberRole.USER
    ) = Member(
        username = username,
        password = "encoded-password",
        name = username.split("-").joinToString(" ") { it.replaceFirstChar(Char::uppercase) },
        email = email,
        role = role
    )
}
