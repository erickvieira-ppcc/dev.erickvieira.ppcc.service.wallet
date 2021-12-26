package dev.erickvieira.ppcc.service.wallet.unit.domain.extension

import dev.erickvieira.ppcc.service.wallet.unit.WalletUnitTests
import dev.erickvieira.ppcc.service.wallet.domain.extension.toWalletField
import dev.erickvieira.ppcc.service.wallet.domain.model.WalletToggle
import dev.erickvieira.ppcc.service.wallet.web.api.model.WalletFields
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class WalletToggleExtensionsTests : WalletUnitTests() {

    @Test
    fun `toWalletFields - must return an WalletFields enum instance from a given WalletToggle`() {
        assertEquals(WalletToggle.IS_ACTIVE.toWalletField(), WalletFields.isActive)
        assertEquals(WalletToggle.IS_DEFAULT.toWalletField(), WalletFields.isDefault)
        assertEquals(WalletToggle.ACCEPT_BANK_TRANSFER.toWalletField(), WalletFields.acceptBankTransfer)
        assertEquals(WalletToggle.ACCEPT_PAYMENTS.toWalletField(), WalletFields.acceptPayments)
        assertEquals(WalletToggle.ACCEPT_WITHDRAWING.toWalletField(), WalletFields.acceptWithdrawing)
        assertEquals(WalletToggle.ACCEPT_DEPOSIT.toWalletField(), WalletFields.acceptDeposit)
    }

}