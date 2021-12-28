package dev.erickvieira.ppcc.service.wallet.unit

import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet
import dev.erickvieira.ppcc.service.wallet.web.api.model.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@ActiveProfiles("test")
open class WalletUnitTests {

    private val defaultSurname = "Vacations"
    private val defaultIsActive = true
    private val defaultIsDefault = false
    private val defaultMinBalance = 0.0
    private val defaultAcceptBankTransfer = true
    private val defaultAcceptPayments = true
    private val defaultAcceptWithdrawing = true
    private val defaultAcceptDeposit = true
    private val defaultCreatedAt: OffsetDateTime = OffsetDateTime.now().minusHours(1)

    protected fun generateWalletCreationDTO(
        surname: String = defaultSurname,
        isActive: Boolean = defaultIsActive,
        minBalance: Double = defaultMinBalance,
    ) = WalletCreationDTO(
        surname = surname,
        isActive = isActive,
        minBalance = minBalance,
    )

    protected fun generateWalletPartialUpdateDTO(
        surname: String = defaultSurname,
        minBalance: Double = defaultMinBalance,
        isActive: Boolean = defaultIsActive,
        acceptBankTransfer: Boolean = defaultAcceptBankTransfer,
        acceptPayments: Boolean = defaultAcceptPayments,
        acceptWithdrawing: Boolean = defaultAcceptWithdrawing,
        acceptDeposit: Boolean = defaultAcceptDeposit,
    ) = WalletPartialUpdateDTO(
        surname = surname,
        minBalance = minBalance,
        isActive = isActive,
        acceptBankTransfer = acceptBankTransfer,
        acceptPayments = acceptPayments,
        acceptWithdrawing = acceptWithdrawing,
        acceptDeposit = acceptDeposit,
    )

    protected fun generateWalletUpdateDTO(
        surname: String = defaultSurname,
        minBalance: Double = defaultMinBalance,
        isActive: Boolean = defaultIsActive,
        acceptBankTransfer: Boolean = defaultAcceptBankTransfer,
        acceptPayments: Boolean = defaultAcceptPayments,
        acceptWithdrawing: Boolean = defaultAcceptWithdrawing,
        acceptDeposit: Boolean = defaultAcceptDeposit,
    ) = WalletUpdateDTO(
        surname = surname,
        minBalance = minBalance,
        isActive = isActive,
        acceptBankTransfer = acceptBankTransfer,
        acceptPayments = acceptPayments,
        acceptWithdrawing = acceptWithdrawing,
        acceptDeposit = acceptDeposit,
    )

    protected fun Wallet.Companion.randomize(
        id: UUID? = UUID.randomUUID(),
        userId: UUID? = UUID.randomUUID(),
        surname: String = defaultSurname,
        isActive: Boolean = defaultIsActive,
        isDefault: Boolean = defaultIsDefault,
        minBalance: BigDecimal = defaultMinBalance.toBigDecimal(),
        acceptBankTransfer: Boolean = defaultAcceptBankTransfer,
        acceptPayments: Boolean = defaultAcceptPayments,
        acceptWithdrawing: Boolean = defaultAcceptWithdrawing,
        acceptDeposit: Boolean = defaultAcceptDeposit,
        createdAt: OffsetDateTime? = defaultCreatedAt,
        updatedAt: OffsetDateTime? = null,
        deletedAt: OffsetDateTime? = null,
    ) = Wallet(
        id = id,
        userId = userId,
        surname = surname,
        isActive = isActive,
        isDefault = isDefault,
        minBalance = minBalance,
        acceptBankTransfer = acceptBankTransfer,
        acceptPayments = acceptPayments,
        acceptWithdrawing = acceptWithdrawing,
        acceptDeposit = acceptDeposit,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

    protected fun WalletCreationDTO.asSavedWallet(userId: UUID) = Wallet(
        id = UUID.randomUUID(),
        userId = userId,
        surname = surname.lowercase().trim(),
        isActive = isActive ?: false,
        minBalance = minBalance?.toBigDecimal() ?: BigDecimal.ZERO,
        createdAt = defaultCreatedAt
    )

    protected fun generateWalletPage(
        number: Int = 0,
        size: Int = 20,
        content: List<Wallet> = listOf(Wallet.randomize()),
    ): Page<Wallet> = PageImpl(content, PageRequest.of(number, size), content.size.toLong())

    protected fun generateEmptyWalletPage() = generateWalletPage(content = listOf())

    protected fun generateUserId(): UUID = UUID.randomUUID()

    protected fun PageWalletDTO.assertPagination(page: Page<Wallet>) {
        assertEquals(page.pageable.sort.toString(), sortedBy)
        assertEquals(page.totalPages, pageCount)
        assertEquals(page.totalElements, total)
        assertEquals(page.numberOfElements, content.size)
    }

    protected fun WalletDTO.assertWalletCreation(input: WalletCreationDTO, scapeIdNotNullAssert: Boolean = false) {
        if (!scapeIdNotNullAssert) assertNotNull(id)
        assertNotNull(userId)
        assertEquals(input.isActive, isActive)
        assertEquals(input.surname.lowercase().trim(), surname)
        assertEquals(input.minBalance, minBalance)
        assertNotNull(createdAt)
        assertNull(updatedAt)
    }

    protected fun WalletDTO.assertReturnedWallet(wallet: Wallet) {
        assertNotNull(id)
        assertNotNull(wallet.id)
        assertEquals(wallet.id, id)
        assertEquals(wallet.userId, userId)
        assertEquals(wallet.surname, surname)
        assertEquals(wallet.isActive, isActive)
        assertEquals(wallet.isDefault, isDefault)
        assertEquals(wallet.minBalance.toDouble(), minBalance)
        assertEquals(wallet.acceptBankTransfer, acceptBankTransfer)
        assertEquals(wallet.acceptPayments, acceptPayments)
        assertEquals(wallet.acceptWithdrawing, acceptWithdrawing)
        assertEquals(wallet.acceptDeposit, acceptDeposit)
        assertEquals(wallet.createdAt, createdAt)
    }

    protected fun WalletDTO.assertWalletUpdate(original: Wallet, updated: Wallet, vararg changes: WalletFields) {
        assertEquals(original.id, id)
        assertEquals(updated.id, id)
        assertEquals(original.userId, userId)
        assertEquals(updated.userId, userId)
        assertEquals(updated.surname, surname)
        original.let {
            (if (changes.contains(WalletFields.surname)) assertNotEquals(
                it.surname,
                surname
            ) else assertEquals(it.surname, surname))
        }
        assertEquals(updated.isActive, isActive)
        original.let {
            (if (changes.contains(WalletFields.isActive)) assertNotEquals(
                it.isActive,
                isActive
            ) else assertEquals(it.isActive, isActive))
        }
        assertEquals(updated.isDefault, isDefault)
        original.let {
            (if (changes.contains(WalletFields.isDefault)) assertNotEquals(
                it.isDefault,
                isDefault
            ) else assertEquals(it.isDefault, isDefault))
        }
        assertEquals(updated.minBalance, minBalance.toBigDecimal())
        original.let {
            (if (changes.contains(WalletFields.minBalance)) assertNotEquals(
                it.minBalance,
                minBalance.toBigDecimal()
            ) else assertEquals(it.minBalance, minBalance.toBigDecimal()))
        }
        assertEquals(updated.acceptBankTransfer, acceptBankTransfer)
        original.let {
            (if (changes.contains(WalletFields.acceptBankTransfer)) assertNotEquals(
                it.acceptBankTransfer,
                acceptBankTransfer
            ) else assertEquals(it.acceptBankTransfer, acceptBankTransfer))
        }
        assertEquals(updated.acceptPayments, acceptPayments)
        original.let {
            (if (changes.contains(WalletFields.acceptPayments)) assertNotEquals(
                it.acceptPayments,
                acceptPayments
            ) else assertEquals(it.acceptPayments, acceptPayments))
        }
        assertEquals(updated.acceptWithdrawing, acceptWithdrawing)
        original.let {
            (if (changes.contains(WalletFields.acceptWithdrawing)) assertNotEquals(
                it.acceptWithdrawing,
                acceptWithdrawing
            ) else assertEquals(it.acceptWithdrawing, acceptWithdrawing))
        }
        assertEquals(updated.acceptDeposit, acceptDeposit)
        original.let {
            (if (changes.contains(WalletFields.acceptDeposit)) assertNotEquals(
                it.acceptDeposit,
                acceptDeposit
            ) else assertEquals(it.acceptDeposit, acceptDeposit))
        }
        assertNotNull(updatedAt)
    }

}
