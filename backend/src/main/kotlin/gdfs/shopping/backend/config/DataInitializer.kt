package gdfs.shopping.backend.config

import gdfs.shopping.backend.domain.member.Member
import gdfs.shopping.backend.domain.member.MemberRepository
import gdfs.shopping.backend.domain.member.MemberRole
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DataInitializer(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.admin.username}") private val adminUsername: String,
    @Value("\${app.admin.password}") private val adminPassword: String,
    @Value("\${app.admin.name}") private val adminName: String,
    @Value("\${app.admin.email}") private val adminEmail: String
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments) {
        createAdminAccount()
    }

    private fun createAdminAccount() {
        if (!memberRepository.existsByUsername(adminUsername)) {
            val admin = Member(
                username = adminUsername,
                password = passwordEncoder.encode(adminPassword)!!,
                name = adminName,
                email = adminEmail,
                role = MemberRole.ADMIN
            )
            memberRepository.save(admin)
            println("관리자 계정이 생성되었습니다: $adminUsername")
        }
    }
}
