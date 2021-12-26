package dev.erickvieira.ppcc.service.wallet.domain.entity

import com.google.gson.Gson
import org.hibernate.Hibernate
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(
    name = "tb_wallet",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "surname"])]
)
data class Wallet(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID? = null,

    @Column(name = "surname", nullable = false)
    val surname: String = String(),

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "is_default", nullable = false)
    val isDefault: Boolean = false,

    @Column(name = "min_balance", nullable = false)
    val minBalance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "accept_bank_transfer")
    val acceptBankTransfer: Boolean = true,

    @Column(name = "accept_payments")
    val acceptPayments: Boolean = true,

    @Column(name = "accept_withdrawing")
    val acceptWithdrawing: Boolean = true,

    @Column(name = "accept_deposit")
    val acceptDeposit: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime? = null,

    @Column(name = "updated_at")
    val updatedAt: OffsetDateTime? = null,

    @Column(name = "deleted_at")
    val deletedAt: OffsetDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Wallet

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String = Gson().toJson(this)

    companion object {}
}
