package dev.erickvieira.ppcc.service.wallet.unit.domain.extension

import dev.erickvieira.ppcc.service.wallet.unit.WalletUnitTests
import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet
import dev.erickvieira.ppcc.service.wallet.domain.exception.DefaultWalletDeletionException
import dev.erickvieira.ppcc.service.wallet.domain.extension.*
import dev.erickvieira.ppcc.service.wallet.domain.model.WalletToggle
import dev.erickvieira.ppcc.service.wallet.web.api.model.WalletFields
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class WalletExtensionsTests : WalletUnitTests() {

    @Test
    fun `fromWalletCreationDTO - must return an Wallet instance from a given WalletCreationDTO`() {
        val walletCreationDTO = generateWalletCreationDTO()
        val userId = generateUserId()

        Wallet.fromWalletCreationDTO(userId = userId, input = walletCreationDTO).let { wallet ->
            assertNotNull(userId)
            assertEquals(walletCreationDTO.isActive, wallet.isActive)
            assertEquals(walletCreationDTO.surname.lowercase().trim(), wallet.surname)
            assertEquals(walletCreationDTO.minBalance, wallet.minBalance.toDouble())
            assertNotNull(wallet.createdAt)
            assertNull(wallet.updatedAt)
            assertNull(wallet.deletedAt)
        }
    }

    @Test
    fun `toWalletDTO - must return an WalletDTO according to both Wallet X and Y`() {
        generateWalletX().apply { toWalletDTO().assertReturnedWallet(wallet = this) }
        generateWalletY().apply { toWalletDTO().assertReturnedWallet(wallet = this) }
    }

    @Test
    fun `withUpdatedValues - must return an updated Wallet according to an WalletPartialUpdateDTO instance`() {
        val wallet = generateWalletX()
        val walletPartialUpdateDTO = generateWalletPartialUpdateDTO(
            surname = "Home Office",
            minBalance = 30.00,
            isActive = false,
            acceptBankTransfer = false,
            acceptPayments = true,
            acceptWithdrawing = false,
            acceptDeposit = true
        )

        wallet.withUpdatedValues(walletPartialUpdateDTO).let { updatedWallet ->
            assert(wallet !== updatedWallet)
            updatedWallet.toWalletDTO().assertWalletUpdate(
                original = wallet,
                updated = updatedWallet,
                WalletFields.surname,
                WalletFields.minBalance,
            )
        }
    }

    @Test
    fun `withUpdatedValues - must return an updated Wallet according to an WalletUpdateDTO instance`() {
        val wallet = generateWalletX()
        val walletUpdateDTO = generateWalletUpdateDTO(
            surname = "Banana",
            minBalance = 10.00,
            isActive = true,
            acceptBankTransfer = true,
            acceptPayments = false,
            acceptWithdrawing = true,
            acceptDeposit = false,
        )

        wallet.withUpdatedValues(walletUpdateDTO).let { updatedWallet ->
            assert(wallet !== updatedWallet)
            updatedWallet.toWalletDTO().assertWalletUpdate(
                original = wallet,
                updated = updatedWallet,
                WalletFields.surname,
                WalletFields.minBalance,
                WalletFields.isActive,
                WalletFields.acceptBankTransfer,
                WalletFields.acceptPayments,
                WalletFields.acceptWithdrawing,
                WalletFields.acceptDeposit,
            )
        }
    }

    @Test
    fun `asDeleted - must return an Wallet instance with non-null deletedAt date`() {
        val wallet = Wallet.randomize()

        wallet.asDeleted().let { deletedWallet ->
            assert(wallet !== deletedWallet)
            assertEquals("del:${wallet.surname}", deletedWallet.surname)
            assertNotNull(deletedWallet.deletedAt)
        }
    }

    @Test
    fun `toggleSetting - must return an Wallet with toggled isDefault attribute`() {
        val wallet = Wallet.randomize()

        wallet.toggle(WalletToggle.IS_DEFAULT).let { updatedWallet ->
            assert(wallet !== updatedWallet)
            assertEquals(wallet.isDefault, !updatedWallet.isDefault)
        }
    }

    @Test
    fun `toggleSetting - must return an Wallet with toggled isActive attribute`() {
        val wallet = Wallet.randomize()

        wallet.toggle(WalletToggle.IS_ACTIVE).let { updatedWallet ->
            assert(wallet !== updatedWallet)
            assertEquals(wallet.isActive, !updatedWallet.isActive)
        }
    }

    @Test
    fun `toggleSetting - must return an Wallet with toggled acceptBankTransfer attribute`() {
        val wallet = Wallet.randomize()

        wallet.toggle(WalletToggle.ACCEPT_BANK_TRANSFER).let { updatedWallet ->
            assert(wallet !== updatedWallet)
            assertEquals(wallet.acceptBankTransfer, !updatedWallet.acceptBankTransfer)
        }
    }

    @Test
    fun `toggleSetting - must return an Wallet with toggled acceptPayments attribute`() {
        val wallet = Wallet.randomize()

        wallet.toggle(WalletToggle.ACCEPT_PAYMENTS).let { updatedWallet ->
            assert(wallet !== updatedWallet)
            assertEquals(wallet.acceptPayments, !updatedWallet.acceptPayments)
        }
    }

    @Test
    fun `toggleSetting - must return an Wallet with toggled acceptWithdrawing attribute`() {
        val wallet = Wallet.randomize()

        wallet.toggle(WalletToggle.ACCEPT_WITHDRAWING).let { updatedWallet ->
            assert(wallet !== updatedWallet)
            assertEquals(wallet.acceptWithdrawing, !updatedWallet.acceptWithdrawing)
        }
    }

    @Test
    fun `toggleSetting - must return an Wallet with toggled acceptDeposit attribute`() {
        val wallet = Wallet.randomize()

        wallet.toggle(WalletToggle.ACCEPT_DEPOSIT).let { updatedWallet ->
            assert(wallet !== updatedWallet)
            assertEquals(wallet.acceptDeposit, !updatedWallet.acceptDeposit)
        }
    }

    @Test
    fun `ifIsDefault - must call the parameter function`() {
        val wallet = Wallet.randomize(isDefault = true)

        assertThrows<DefaultWalletDeletionException> {
            wallet.ifIsDefault { throw DefaultWalletDeletionException(id = id, surname = surname) }
        }
    }

    @Test
    fun `ifIsDefault - must not call the parameter function`() {
        val wallet = Wallet.randomize(isDefault = false)

        assertDoesNotThrow {
            wallet.ifIsDefault { throw DefaultWalletDeletionException(id = id, surname = surname) }
        }
    }

    private fun generateWalletX() = Wallet.randomize(
        surname = "Health",
        minBalance = BigDecimal(100.00),
        isActive = false,
        acceptBankTransfer = false,
        acceptPayments = true,
        acceptWithdrawing = false,
        acceptDeposit = true,
    )

    private fun generateWalletY() = Wallet.randomize(
        surname = "Education",
        minBalance = BigDecimal(230.50),
        isActive = true,
        acceptBankTransfer = true,
        acceptPayments = false,
        acceptWithdrawing = true,
        acceptDeposit = false,
    )

}