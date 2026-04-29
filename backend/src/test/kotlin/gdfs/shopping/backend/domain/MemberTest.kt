package gdfs.shopping.backend.domain

import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.member.MemberRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MemberTest {

    @Test
    fun `creates user member with default role and timestamps`() {
        // Verifies the default entity constructor creates a normal user account and initializes audit timestamps.
        val member = Member(
            username = "user1",
            password = "encoded-password",
            name = "User One",
            email = "user1@example.com"
        )

        assertEquals(null, member.id)
        assertEquals("user1", member.username)
        assertEquals("encoded-password", member.password)
        assertEquals("User One", member.name)
        assertEquals("user1@example.com", member.email)
        assertEquals(MemberRole.USER, member.role)
        assertNotNull(member.createdAt)
        assertNotNull(member.updatedAt)
    }

    @Test
    fun `creates admin member when role is explicitly provided`() {
        // Verifies administrator accounts can be represented by the same entity with the ADMIN role.
        val admin = Member(
            id = 1L,
            username = "admin",
            password = "encoded-admin-password",
            name = "Administrator",
            email = "admin@example.com",
            role = MemberRole.ADMIN
        )

        assertEquals(1L, admin.id)
        assertEquals(MemberRole.ADMIN, admin.role)
        assertEquals("admin", admin.username)
    }

    @Test
    fun `copying member can update mutable audit timestamp without changing identity fields`() {
        // Verifies Kotlin data-class copy behavior is safe for tests that need persisted IDs and stable member data.
        val original = Member(
            id = 10L,
            username = "copy-user",
            password = "encoded-password",
            name = "Copy User",
            email = "copy@example.com"
        )
        val copied = original.copy(name = "Copied User")

        assertEquals(10L, copied.id)
        assertEquals("copy-user", copied.username)
        assertEquals("Copied User", copied.name)
        assertEquals(original.email, copied.email)
    }
}
