package gdfs.shopping.backend.domain.payment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long): List<Payment>
}
