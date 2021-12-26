package dev.erickvieira.ppcc.service.wallet.domain.extension

import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet
import dev.erickvieira.ppcc.service.wallet.domain.model.WalletToggle
import dev.erickvieira.ppcc.service.wallet.domain.model.WalletToggle.*
import dev.erickvieira.ppcc.service.wallet.web.api.model.WalletCreationDTO
import dev.erickvieira.ppcc.service.wallet.web.api.model.WalletDTO
import dev.erickvieira.ppcc.service.wallet.web.api.model.WalletPartialUpdateDTO
import dev.erickvieira.ppcc.service.wallet.web.api.model.WalletUpdateDTO
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

fun Wallet.toWalletDTO() = WalletDTO(
    id = id!!,
    userId = userId!!,
    surname = surname,
    isActive = isActive,
    isDefault = isDefault,
    minBalance = minBalance.toDouble(),
    acceptBankTransfer = acceptBankTransfer,
    acceptPayments = acceptPayments,
    acceptWithdrawing = acceptWithdrawing,
    acceptDeposit = acceptDeposit,
    createdAt = createdAt ?: OffsetDateTime.MIN,
    updatedAt = updatedAt
)

fun Wallet.Companion.fromWalletCreationDTO(userId: UUID, input: WalletCreationDTO) = Wallet(
    surname = input.surname.lowercase().trim(),
    isActive = input.isActive ?: true,
    minBalance = input.minBalance?.toBigDecimal() ?: BigDecimal.ZERO,
    userId = userId,
    createdAt = OffsetDateTime.now()
)

fun Wallet.withUpdatedValues(values: WalletPartialUpdateDTO) = Wallet(
    id = id,
    userId = userId,
    surname = values.surname?.ifBlank { surname } ?: surname,
    isActive = isActive,
    isDefault = isDefault,
    minBalance = values.minBalance?.toBigDecimal() ?: minBalance,
    acceptBankTransfer = values.acceptBankTransfer ?: acceptBankTransfer,
    acceptPayments = values.acceptPayments ?: acceptPayments,
    acceptWithdrawing = values.acceptWithdrawing ?: acceptWithdrawing,
    acceptDeposit = values.acceptDeposit ?: acceptDeposit,
    createdAt = createdAt,
    updatedAt = OffsetDateTime.now()
)

fun Wallet.withUpdatedValues(values: WalletUpdateDTO) = Wallet(
    id = id,
    userId = userId,
    surname = values.surname,
    isActive = values.isActive,
    isDefault = isDefault,
    minBalance = values.minBalance.toBigDecimal(),
    acceptBankTransfer = values.acceptBankTransfer,
    acceptPayments = values.acceptPayments,
    acceptWithdrawing = values.acceptWithdrawing,
    acceptDeposit = values.acceptDeposit,
    createdAt = createdAt,
    updatedAt = OffsetDateTime.now()
)

fun Wallet.asDeleted() = Wallet(
    id = id,
    userId = userId,
    surname = "del:$surname",
    isActive = isActive,
    isDefault = isDefault,
    minBalance = minBalance,
    acceptBankTransfer = acceptBankTransfer,
    acceptPayments = acceptPayments,
    acceptWithdrawing = acceptWithdrawing,
    acceptDeposit = acceptDeposit,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = OffsetDateTime.now()
)

fun Wallet.toggle(walletToggle: WalletToggle) = Wallet(
    id = id,
    userId = userId,
    surname = surname,
    isActive = isActive.let { if (walletToggle == IS_ACTIVE) !it else it },
    isDefault = isDefault.let { if (walletToggle == IS_DEFAULT) !it else it },
    minBalance = minBalance,
    acceptBankTransfer = acceptBankTransfer.let { if (walletToggle == ACCEPT_BANK_TRANSFER) !it else it },
    acceptPayments = acceptPayments.let { if (walletToggle == ACCEPT_PAYMENTS) !it else it },
    acceptWithdrawing = acceptWithdrawing.let { if (walletToggle == ACCEPT_WITHDRAWING) !it else it },
    acceptDeposit = acceptDeposit.let { if (walletToggle == ACCEPT_DEPOSIT) !it else it },
    createdAt = createdAt,
    updatedAt = OffsetDateTime.now()
)

fun Wallet.ifIsDefault(callback: Wallet.() -> Unit) = takeIf { it.isDefault }?.let { callback() }