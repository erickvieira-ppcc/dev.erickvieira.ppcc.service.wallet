package dev.erickvieira.ppcc.service.wallet.unit.extension

import dev.erickvieira.ppcc.service.wallet.WalletServiceApplication
import dev.erickvieira.ppcc.service.wallet.unit.WalletUnitTests
import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet
import dev.erickvieira.ppcc.service.wallet.domain.exception.*
import dev.erickvieira.ppcc.service.wallet.extension.executeOrLog
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LoggerExtensionTests : WalletUnitTests() {

    private val logger: Logger = LoggerFactory.getLogger(WalletServiceApplication::class.java)

    @Test
    fun `executeOrLog - must return the value`() {
        val wallet = Wallet.randomize()
        assert(wallet === logger.executeOrLog { wallet })
        val walletPage = generateWalletPage()
        assert(walletPage === logger.executeOrLog { walletPage })
    }

    @Test
    fun `executeOrLog - must return void`() = assert(Unit == logger.executeOrLog { println("Testing...") })

    @Test
    fun `executeOrLog - must throw BaseException`() {
        assertThrows<BaseException> {
            logger.executeOrLog { throw WalletNotFoundException() }
        }
        assertThrows<BaseException> {
            logger.executeOrLog { throw DuplicatedWalletSurnameException(surname = "") }
        }
        assertThrows<BaseException> {
            logger.executeOrLog { throw NullPayloadException(payload = Wallet::class.java.name) }
        }
    }

    @Test
    fun `executeOrLog - must throw UnexpectedException`() {
        assertThrows<UnexpectedException> {
            logger.executeOrLog { throw Exception() }
        }
        assertThrows<UnexpectedException> {
            logger.executeOrLog { throw Error() }
        }
    }

}