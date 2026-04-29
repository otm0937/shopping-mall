package gdfs.shopping.backend.domain.member

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun findByUsername(username: String): Optional<Member>
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
