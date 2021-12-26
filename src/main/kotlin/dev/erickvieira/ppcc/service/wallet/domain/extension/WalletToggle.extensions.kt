package dev.erickvieira.ppcc.service.wallet.domain.extension

import dev.erickvieira.ppcc.service.wallet.domain.model.WalletToggle
import dev.erickvieira.ppcc.service.wallet.web.api.model.WalletFields

fun WalletToggle.toWalletField() = when (this) {
    WalletToggle.IS_ACTIVE -> WalletFields.isActive
    WalletToggle.IS_DEFAULT -> WalletFields.isDefault
    WalletToggle.ACCEPT_BANK_TRANSFER -> WalletFields.acceptBankTransfer
    WalletToggle.ACCEPT_PAYMENTS -> WalletFields.acceptPayments
    WalletToggle.ACCEPT_WITHDRAWING -> WalletFields.acceptWithdrawing
    WalletToggle.ACCEPT_DEPOSIT -> WalletFields.acceptDeposit
}