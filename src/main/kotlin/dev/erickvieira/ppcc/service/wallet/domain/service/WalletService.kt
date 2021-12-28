package dev.erickvieira.ppcc.service.wallet.domain.service

import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet
import dev.erickvieira.ppcc.service.wallet.domain.exception.*
import dev.erickvieira.ppcc.service.wallet.domain.extension.*
import dev.erickvieira.ppcc.service.wallet.domain.model.WalletToggle.*
import dev.erickvieira.ppcc.service.wallet.domain.port.rabbitmq.WalletRabbitDispatcherPort
import dev.erickvieira.ppcc.service.wallet.domain.repository.WalletRepository
import dev.erickvieira.ppcc.service.wallet.extension.*
import dev.erickvieira.ppcc.service.wallet.web.api.WalletApiDelegate
import dev.erickvieira.ppcc.service.wallet.web.api.model.*
import io.swagger.annotations.Api
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*

@Service
@Api(value = "Wallet", description = "the Wallet API", tags = ["Wallet"])
class WalletService(
    private val walletRepository: WalletRepository,
    private val walletDispatcher: WalletRabbitDispatcherPort
) : WalletApiDelegate {

    private val logger: Logger = LoggerFactory.getLogger(WalletService::class.java)

    @Throws(
        WalletNotFoundException::class,
        UnexpectedException::class
    )
    override fun searchWallets(
        userId: UUID,
        surname: String?,
        page: Int,
        size: Int,
        sort: WalletFields,
        direction: Direction
    ): ResponseEntity<PageWalletDTO> = logger.executeOrLog {
        val method = "method" to "searchWallets"
        val search = arrayOf(
            "userId" to userId,
            "surname" to surname,
            "page" to page,
            "size" to size,
            "sort" to sort,
            "direction" to direction
        )
        val pageable = PageRequest(pagination = search.toMap())
        logger.custom.info(method, *search)

        val pagedResult = surname?.let {
            walletRepository.findAllByUserIdAndSurnameAndDeletedAtIsNull(
                userId = userId,
                surname = surname.lowercase().trim(),
                pageable = pageable
            )
        } ?: walletRepository.findAllByUserIdAndDeletedAtIsNull(userId = userId, pageable = pageable)

        logger.custom.info(method, "totalElements" to pagedResult.totalElements, "totalPages" to pagedResult.totalPages)

        if (pagedResult.isEmpty) throw WalletNotFoundException(search = search)

        ResponseEntity.ok(load { fromPage(page = pagedResult) })
    }

    @Throws(
        UserNotFoundException::class,
        NullPayloadException::class,
        DuplicatedWalletSurnameException::class,
        UnexpectedException::class
    )
    override fun createWallet(
        userId: UUID,
        walletCreationDTO: WalletCreationDTO?
    ): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val method = "method" to "createWallet"
        walletCreationDTO.ensurePayloadNotNull { payload ->
            logger.custom.info(method, "userId" to userId, *payload.toPairArray())
            walletRepository.findFirstByUserIdAndDeletedAtIsNull(
                userId = userId
            ) ?: throw UserNotFoundException("userId" to userId)
            Wallet.fromWalletCreationDTO(
                userId = userId,
                input = payload
            ).ensureSurnameUniquenessForTheGivenUser { wallet ->
                saveAndDispatch(wallet).let { savedWallet ->
                    ResponseEntity.created(
                        ServletUriComponentsBuilder
                            .fromCurrentRequestUri()
                            .path("/${savedWallet.id}")
                            .build()
                            .toUri()
                    ).body(savedWallet.toWalletDTO())
                }
            }
        }
    }

    @Throws(
        WalletNotFoundException::class,
        UnexpectedException::class
    )
    override fun retrieveWallet(
        userId: UUID,
        walletId: UUID
    ): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val method = "method" to "retrieveWallet"
        logger.custom.info(method, "userId" to userId, "walletId" to walletId)
        walletRepository.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = userId, id = walletId)?.let {
            ResponseEntity.ok(it.toWalletDTO())
        } ?: throw WalletNotFoundException("userId" to userId, "id" to walletId)
    }

    @Throws(
        WalletNotFoundException::class,
        UnexpectedException::class
    )
    override fun retrieveDefaultWallet(
        userId: UUID
    ): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val method = "method" to "retrieveDefaultWallet"
        logger.custom.info(method, "userId" to userId)
        walletRepository.findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(userId = userId)?.let {
            ResponseEntity.ok(it.toWalletDTO())
        } ?: throw WalletNotFoundException("userId" to userId, "default" to true)
    }

    @Throws(
        WalletNotFoundException::class,
        NullPayloadException::class,
        DuplicatedWalletSurnameException::class,
        UnexpectedException::class
    )
    override fun partiallyUpdateWallet(
        userId: UUID,
        walletId: UUID,
        walletPartialUpdateDTO: WalletPartialUpdateDTO?
    ): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val method = "method" to "partiallyUpdateWallet"
        walletPartialUpdateDTO.ensurePayloadNotNull { payload ->
            logger.custom.info(method, "userId" to userId, "walletId" to walletId)
            logger.custom.info(method, *payload.toPairArray())
            walletRepository.findFirstByUserIdAndIdAndDeletedAtIsNull(
                userId = userId,
                id = walletId
            )?.ensureSurnameUniquenessForTheGivenUser { wallet ->
                saveAndDispatch(wallet.withUpdatedValues(values = payload))
                    .let { ResponseEntity.ok(it.toWalletDTO()) }
            } ?: throw WalletNotFoundException("userId" to userId, "id" to walletId)
        }
    }

    @Throws(
        WalletNotFoundException::class,
        NullPayloadException::class,
        DuplicatedWalletSurnameException::class,
        UnexpectedException::class
    )
    override fun updateWallet(
        userId: UUID,
        walletId: UUID,
        walletUpdateDTO: WalletUpdateDTO?
    ): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val method = "method" to "updateWallet"
        walletUpdateDTO.ensurePayloadNotNull { payload ->
            logger.custom.info(method, "userId" to userId, "walletId" to walletId)
            logger.custom.info(method, *payload.toPairArray())
            walletRepository.findFirstByUserIdAndIdAndDeletedAtIsNull(
                userId = userId,
                id = walletId
            )?.ensureSurnameUniquenessForTheGivenUser { wallet ->
                saveAndDispatch(wallet.withUpdatedValues(values = payload))
                    .let { ResponseEntity.ok(it.toWalletDTO()) }
            } ?: throw WalletNotFoundException("userId" to userId, "id" to walletId)
        }
    }

    @Throws(
        WalletNotFoundException::class,
        DefaultWalletDeletionException::class,
        UnexpectedException::class
    )
    override fun deleteWallet(userId: UUID, walletId: UUID): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val method = "method" to "deleteWallet"
        logger.custom.info(method, "userId" to userId, "walletId" to walletId)
        walletRepository.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = userId, id = walletId)?.let { wallet ->
            wallet.ifIsDefault { throw DefaultWalletDeletionException(id = walletId, surname = wallet.surname) }
            saveAndDispatch(wallet.asDeleted())
            ResponseEntity.ok(wallet.toWalletDTO())
        } ?: throw WalletNotFoundException("userId" to userId, "id" to walletId)
    }

    @Throws(
        WalletNotFoundException::class,
        UnexpectedException::class
    )
    override fun setDefaultWallet(userId: UUID, walletId: UUID): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val updatedWallets = mutableListOf<Wallet>()
        val method = "method" to "setDefaultWallet"
        logger.custom.info(method, "userId" to userId, "walletId" to walletId)

        walletRepository.findFirstByUserIdAndIdAndDeletedAtIsNull(
            userId = userId,
            id = walletId
        )?.toggle(IS_DEFAULT)?.let { newDefault ->
            updatedWallets.add(newDefault)

            walletRepository
                .findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(userId = userId)
                ?.toggle(IS_DEFAULT)?.let { oldDefault -> updatedWallets.add(oldDefault) }

            walletRepository.saveAll(updatedWallets).takeIf {
                it.isNotEmpty()
            }?.run {
                forEach { walletDispatcher.dispatch(wallet = it) }
                ResponseEntity.ok(newDefault.toWalletDTO())
            }
        } ?: throw WalletNotFoundException("userId" to userId, "id" to walletId)
    }

    @Throws(
        WalletNotFoundException::class,
        UnexpectedException::class
    )
    override fun toggleActive(
        userId: UUID,
        walletId: UUID
    ): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val method = "method" to "toggleActive"
        logger.custom.info(method, "userId" to userId, "walletId" to walletId)

        walletRepository.findFirstByUserIdAndIdAndDeletedAtIsNull(
            userId = userId,
            id = walletId
        )?.let { wallet ->
            saveAndDispatch(wallet.toggle(IS_ACTIVE))
                .let { ResponseEntity.ok(it.toWalletDTO()) }
        } ?: throw WalletNotFoundException("userId" to userId, "id" to walletId)
    }

    @Throws(
        WalletNotFoundException::class,
        UnexpectedException::class
    )
    override fun toggleAcceptBankTransfer(
        userId: UUID,
        walletId: UUID
    ): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val method = "method" to "toggleAcceptBankTransfer"
        logger.custom.info(method, "userId" to userId, "walletId" to walletId)

        walletRepository.findFirstByUserIdAndIdAndDeletedAtIsNull(
            userId = userId,
            id = walletId
        )?.let { wallet ->
            saveAndDispatch(wallet.toggle(ACCEPT_BANK_TRANSFER))
                .let { ResponseEntity.ok(it.toWalletDTO()) }
        } ?: throw WalletNotFoundException("userId" to userId, "id" to walletId)
    }

    @Throws(
        WalletNotFoundException::class,
        UnexpectedException::class
    )
    override fun toggleAcceptPayments(
        userId: UUID,
        walletId: UUID
    ): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val method = "method" to "toggleAcceptPayments"
        logger.custom.info(method, "userId" to userId, "walletId" to walletId)

        walletRepository.findFirstByUserIdAndIdAndDeletedAtIsNull(
            userId = userId,
            id = walletId
        )?.let { wallet ->
            saveAndDispatch(wallet.toggle(ACCEPT_PAYMENTS))
                .let { ResponseEntity.ok(it.toWalletDTO()) }
        } ?: throw WalletNotFoundException("userId" to userId, "id" to walletId)
    }

    @Throws(
        WalletNotFoundException::class,
        UnexpectedException::class
    )
    override fun toggleAcceptWithdrawing(
        userId: UUID,
        walletId: UUID
    ): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val method = "method" to "toggleAcceptWithDrawing"
        logger.custom.info(method, "userId" to userId, "walletId" to walletId)

        walletRepository.findFirstByUserIdAndIdAndDeletedAtIsNull(
            userId = userId,
            id = walletId
        )?.let { wallet ->
            saveAndDispatch(wallet.toggle(ACCEPT_WITHDRAWING))
                .let { ResponseEntity.ok(it.toWalletDTO()) }
        } ?: throw WalletNotFoundException("userId" to userId, "id" to walletId)
    }

    @Throws(
        WalletNotFoundException::class,
        UnexpectedException::class
    )
    override fun toggleAcceptDeposit(
        userId: UUID,
        walletId: UUID
    ): ResponseEntity<WalletDTO> = logger.executeOrLog {
        val method = "method" to "toggleAcceptDeposit"
        logger.custom.info(method, "userId" to userId, "walletId" to walletId)

        walletRepository.findFirstByUserIdAndIdAndDeletedAtIsNull(
            userId = userId,
            id = walletId
        )?.let { wallet ->
            saveAndDispatch(wallet.toggle(ACCEPT_DEPOSIT))
                .let { ResponseEntity.ok(it.toWalletDTO()) }
        } ?: throw WalletNotFoundException("userId" to userId, "id" to walletId)
    }

    fun saveAndDispatch(wallet: Wallet) = wallet.apply {
        walletRepository.save(this).let { updatedWallet -> walletDispatcher.dispatch(wallet = updatedWallet) }
    }

    @Throws(
        NullPayloadException::class
    )
    private fun <T : Any, S : Any> T?.ensurePayloadNotNull(
        payload: String = Wallet::class.java.name,
        callback: (it: T) -> S
    ) = this?.let {
        callback(it)
    } ?: throw NullPayloadException(payload = payload)

    @Throws(
        DuplicatedWalletSurnameException::class,
        UnexpectedException::class
    )
    private fun <T> Wallet.ensureSurnameUniquenessForTheGivenUser(
        callback: ((it: Wallet) -> T)
    ): T = this.takeIf {
        walletRepository.findFirstByUserIdAndSurnameAndDeletedAtIsNull(
            userId = userId,
            surname = surname
        ) == null
    }?.let {
        callback(it)
    } ?: throw DuplicatedWalletSurnameException(surname = surname)

}