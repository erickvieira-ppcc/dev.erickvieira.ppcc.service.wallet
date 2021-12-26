package dev.erickvieira.ppcc.service.wallet.domain.repository

import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface WalletRepository : JpaRepository<Wallet, UUID> {

    fun findFirstByUserIdAndIdAndDeletedAtIsNull(userId: UUID?, id: UUID?): Wallet?

    fun findFirstByUserIdAndDeletedAtIsNull(userId: UUID?): Wallet?

    fun findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId: UUID?, surname: String): Wallet?

    @Suppress("SpringDataMethodInconsistencyInspection")
    fun findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(userId: UUID?): Wallet?

    fun findAllByUserIdAndDeletedAtIsNull(userId: UUID?, pageable: Pageable): Page<Wallet>

    fun findAllByUserIdAndSurnameAndDeletedAtIsNull(userId: UUID?, surname: String, pageable: Pageable): Page<Wallet>

}