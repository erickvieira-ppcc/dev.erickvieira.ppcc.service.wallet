package dev.erickvieira.ppcc.service.wallet.unit.domain.extension

import dev.erickvieira.ppcc.service.wallet.unit.WalletUnitTests
import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet
import dev.erickvieira.ppcc.service.wallet.domain.extension.fromPage
import dev.erickvieira.ppcc.service.wallet.extension.load
import dev.erickvieira.ppcc.service.wallet.web.api.model.PageWalletDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PageWalletExtensionsTests : WalletUnitTests() {

    @Test
    fun `fromPage - must return a PageWalletDTO instance`() {
        val wallets = listOf(
            Wallet.randomize(surname = "A"),
            Wallet.randomize(surname = "B"),
            Wallet.randomize(surname = "C")
        )
        val walletPage = generateWalletPage(content = wallets)

        load<PageWalletDTO> { fromPage(page = walletPage) }.let { pageWalletDTO ->
            pageWalletDTO.assertPagination(page = walletPage)
            assert(pageWalletDTO.content.filter { wallet ->
                wallets.find { it.id == wallet.id && it.surname == wallet.surname } != null
            }.size == wallets.size)
        }
    }

}