package dev.erickvieira.ppcc.service.wallet.unit.domain.service

import dev.erickvieira.ppcc.service.wallet.unit.WalletUnitTests
import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet
import dev.erickvieira.ppcc.service.wallet.domain.exception.*
import dev.erickvieira.ppcc.service.wallet.domain.extension.*
import dev.erickvieira.ppcc.service.wallet.domain.model.WalletToggle.*
import dev.erickvieira.ppcc.service.wallet.domain.port.rabbitmq.WalletRabbitDispatcherPort
import dev.erickvieira.ppcc.service.wallet.domain.repository.WalletRepository
import dev.erickvieira.ppcc.service.wallet.domain.service.WalletService
import dev.erickvieira.ppcc.service.wallet.web.api.model.Direction
import dev.erickvieira.ppcc.service.wallet.web.api.model.WalletFields
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

class WalletServiceTests : WalletUnitTests() {

    private val walletRepositoryMock: WalletRepository = mockk()
    private val walletDispatcherMock: WalletRabbitDispatcherPort = mockk()
    private val walletServiceMock = WalletService(
        walletRepository = walletRepositoryMock,
        walletDispatcher = walletDispatcherMock
    )

    @Before
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `searchWallets - must return a page of all wallets`() {
        val pagedResultMock = generateWalletPage()

        every {
            walletRepositoryMock.findAllByUserIdAndDeletedAtIsNull(userId = any(), pageable = any())
        } returns pagedResultMock

        walletServiceMock.searchWalletsWithDefaults().let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.assertPagination(page = pagedResultMock)
        }

        verify(exactly = 1) { walletRepositoryMock.findAllByUserIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletRepositoryMock.findAllByUserIdAndSurnameAndDeletedAtIsNull(any(), any(), any()) }
    }

    @Test
    fun `searchWallets - must return a page of wallets according to surname`() {
        val pagedResultMock = generateWalletPage()

        every {
            walletRepositoryMock.findAllByUserIdAndSurnameAndDeletedAtIsNull(
                userId = any(),
                surname = any(),
                pageable = any()
            )
        } returns pagedResultMock

        walletServiceMock.searchWalletsWithDefaults(surname = "Erick").let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.assertPagination(page = pagedResultMock)
        }

        verify(exactly = 0) { walletRepositoryMock.findAllByUserIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletRepositoryMock.findAllByUserIdAndSurnameAndDeletedAtIsNull(any(), any(), any()) }
    }

    @Test
    fun `searchWallets - must throw WalletNotFoundException`() {
        val pagedResultMock = generateEmptyWalletPage()

        every {
            walletRepositoryMock.findAllByUserIdAndSurnameAndDeletedAtIsNull(
                userId = any(),
                surname = any(),
                pageable = any()
            )
        } returns pagedResultMock

        assertThrows<WalletNotFoundException> { walletServiceMock.searchWalletsWithDefaults(surname = "Recreation") }

        verify(exactly = 0) { walletRepositoryMock.findAllByUserIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletRepositoryMock.findAllByUserIdAndSurnameAndDeletedAtIsNull(any(), any(), any()) }
    }

    @Test
    fun `createWallet - must return the newly created wallet`() {
        val walletCreationDTOMock = generateWalletCreationDTO()
        val userIdMock = generateUserId()
        val walletMock = walletCreationDTOMock.asSavedWallet(userId = userIdMock)

        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))

        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns null
        every {
            walletRepositoryMock.findFirstByUserIdAndDeletedAtIsNull(userId = any())
        } returns walletMock
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns walletMock

        assertThrows<Exception> {
            walletServiceMock.createWallet(
                userId = userIdMock,
                walletCreationDTO = walletCreationDTOMock
            ).let { responseEntity ->
                assertEquals(HttpStatus.CREATED.value(), responseEntity.statusCodeValue)
                responseEntity.body?.assertWalletCreation(input = walletCreationDTOMock)
            }
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndDeletedAtIsNull(any()) }
        verify(exactly = 1) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 1) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `createWallet - must throw DuplicatedWalletSurnameException`() {
        val walletCreationDTOMock = generateWalletCreationDTO()
        val userIdMock = generateUserId()
        val walletMock = Wallet.fromWalletCreationDTO(userId = userIdMock, input = walletCreationDTOMock)

        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns walletMock
        every {
            walletRepositoryMock.findFirstByUserIdAndDeletedAtIsNull(userId = any())
        } returns mockk()
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<DuplicatedWalletSurnameException> {
            walletServiceMock.createWallet(
                userId = userIdMock,
                walletCreationDTO = walletCreationDTOMock
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndDeletedAtIsNull(any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `createWallet - must throw NullPayloadException`() {
        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns mockk()
        every {
            walletRepositoryMock.findFirstByUserIdAndDeletedAtIsNull(userId = any())
        } returns mockk()
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<NullPayloadException> {
            walletServiceMock.createWallet(
                userId = UUID.randomUUID(),
                walletCreationDTO = null
            )
        }

        verify(exactly = 0) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletRepositoryMock.findFirstByUserIdAndDeletedAtIsNull(any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `createWallet - must throw UserNotFoundException`() {
        val walletCreationDTOMock = generateWalletCreationDTO()

        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns mockk()
        every {
            walletRepositoryMock.findFirstByUserIdAndDeletedAtIsNull(userId = any())
        } returns null
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<UserNotFoundException> {
            walletServiceMock.createWallet(
                userId = UUID.randomUUID(),
                walletCreationDTO = walletCreationDTOMock
            )
        }

        verify(exactly = 0) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndDeletedAtIsNull(any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `retrieveWallet - must return the wallet according to its UUID`() {
        val walletMock = Wallet.randomize()

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock

        walletServiceMock.retrieveWallet(
            userId = walletMock.userId!!,
            walletId = walletMock.id!!
        ).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.assertReturnedWallet(wallet = walletMock)
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
    }

    @Test
    fun `retrieveWallet - must throw WalletNotFoundException`() {
        val walletMock = Wallet.randomize()

        every { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any()) } returns null

        assertThrows<WalletNotFoundException> {
            walletServiceMock.retrieveWallet(userId = walletMock.userId!!, walletId = walletMock.id!!)
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
    }

    @Test
    fun `retrieveDefaultWallet - must return the default wallet of a certain user`() {
        val walletMock = Wallet.randomize(isDefault = true)

        every { walletRepositoryMock.findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(any()) } returns walletMock

        walletServiceMock.retrieveDefaultWallet(walletMock.userId!!).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.let { responseBody ->
                responseBody.assertReturnedWallet(wallet = walletMock)
                assertEquals(true, responseBody.isDefault)
            }
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(any()) }
    }

    @Test
    fun `retrieveDefaultWallet - must throw WalletNotFoundException`() {
        val walletMock = Wallet.randomize()

        every { walletRepositoryMock.findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(any()) } returns null

        assertThrows<WalletNotFoundException> { walletServiceMock.retrieveDefaultWallet(walletMock.userId!!) }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(any()) }
    }

    @Test
    fun `updateWallet - must return the updated wallet`() {
        val walletUpdateDTOMock = generateWalletUpdateDTO(
            surname = "Health",
            minBalance = 100.0,
            isActive = false,
            acceptBankTransfer = false,
            acceptPayments = false,
            acceptWithdrawing = false,
            acceptDeposit = false,
        )
        val walletMock = Wallet.randomize()
        val updatedWalletMock = walletMock.withUpdatedValues(walletUpdateDTOMock)

        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns null
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns updatedWalletMock

        walletServiceMock.updateWallet(
            userId = walletMock.userId!!,
            walletId = walletMock.id!!,
            walletUpdateDTO = walletUpdateDTOMock
        ).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.assertWalletUpdate(
                original = walletMock,
                updated = updatedWalletMock,
                WalletFields.surname,
                WalletFields.minBalance,
                WalletFields.isActive,
                WalletFields.acceptBankTransfer,
                WalletFields.acceptPayments,
                WalletFields.acceptWithdrawing,
                WalletFields.acceptDeposit,
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 1) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `updateWallet - must throw WalletNotFoundException`() {
        val walletUpdateDTOMock = generateWalletUpdateDTO()

        every { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any()) } returns null
        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns mockk()
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<WalletNotFoundException> {
            walletServiceMock.updateWallet(
                userId = UUID.randomUUID(),
                walletId = UUID.randomUUID(),
                walletUpdateDTO = walletUpdateDTOMock,
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `updateWallet - must throw DuplicatedWalletSurnameException`() {
        val walletUpdateDTOMock = generateWalletUpdateDTO(surname = "Health")
        val walletMock = Wallet.randomize()
        val duplicatedWalletMock = Wallet.randomize(surname = walletUpdateDTOMock.surname)

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns duplicatedWalletMock
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<DuplicatedWalletSurnameException> {
            walletServiceMock.updateWallet(
                userId = UUID.randomUUID(),
                walletId = UUID.randomUUID(),
                walletUpdateDTO = walletUpdateDTOMock,
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `updateWallet - must throw NullPayloadException`() {
        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns mockk()
        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns mockk()
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<NullPayloadException> {
            walletServiceMock.updateWallet(
                userId = UUID.randomUUID(),
                walletId = UUID.randomUUID(),
                walletUpdateDTO = null,
            )
        }

        verify(exactly = 0) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `partiallyUpdateWallet - must return the updated wallet with differences only in surname and min balance`() {
        val walletPartialUpdateDTOMock = generateWalletPartialUpdateDTO(surname = "Health", minBalance = 100.0)
        val walletMock = Wallet.randomize()
        val updatedWalletMock = walletMock.withUpdatedValues(walletPartialUpdateDTOMock)

        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns null
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns updatedWalletMock

        walletServiceMock.partiallyUpdateWallet(
            userId = walletMock.userId!!,
            walletId = walletMock.id!!,
            walletPartialUpdateDTO = walletPartialUpdateDTOMock
        ).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.assertWalletUpdate(
                original = walletMock,
                updated = updatedWalletMock,
                WalletFields.surname,
                WalletFields.minBalance
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 1) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `partiallyUpdateWallet - must throw WalletNotFoundException`() {
        val walletPartialUpdateDTOMock = generateWalletPartialUpdateDTO()

        every { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any()) } returns null
        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns mockk()
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<WalletNotFoundException> {
            walletServiceMock.partiallyUpdateWallet(
                userId = UUID.randomUUID(),
                walletId = UUID.randomUUID(),
                walletPartialUpdateDTO = walletPartialUpdateDTOMock,
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `partiallyUpdateWallet - must throw DuplicatedWalletSurnameException`() {
        val walletPartialUpdateDTOMock = generateWalletPartialUpdateDTO(surname = "Health")
        val walletMock = Wallet.randomize()
        val duplicatedWalletMock = Wallet.randomize(surname = walletPartialUpdateDTOMock.surname!!)

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns duplicatedWalletMock
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<DuplicatedWalletSurnameException> {
            walletServiceMock.partiallyUpdateWallet(
                userId = UUID.randomUUID(),
                walletId = UUID.randomUUID(),
                walletPartialUpdateDTO = walletPartialUpdateDTOMock,
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `partiallyUpdateWallet - must throw NullPayloadException`() {
        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns mockk()
        every {
            walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(userId = any(), surname = any())
        } returns mockk()
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<NullPayloadException> {
            walletServiceMock.partiallyUpdateWallet(
                userId = UUID.randomUUID(),
                walletId = UUID.randomUUID(),
                walletPartialUpdateDTO = null,
            )
        }

        verify(exactly = 0) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletRepositoryMock.findFirstByUserIdAndSurnameAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `deleteWallet - must return the deleted wallet`() {
        val walletMock = Wallet.randomize(isDefault = false)
        val deletedWalletMock = walletMock.asDeleted()

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns deletedWalletMock

        walletServiceMock.deleteWallet(
            userId = walletMock.userId!!,
            walletId = walletMock.id!!
        ).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.let { responseBody ->
                responseBody.assertReturnedWallet(wallet = walletMock)
                assertNotNull(deletedWalletMock.deletedAt)
            }
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 1) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `deleteWallet - must throw WalletNotFoundException`() {
        val walletMock = Wallet.randomize()

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns null
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<WalletNotFoundException> {
            walletServiceMock.deleteWallet(userId = walletMock.userId!!, walletId = walletMock.id!!)
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `deleteWallet - must throw DefaultWalletDeletionException`() {
        val walletMock = Wallet.randomize(isDefault = true)

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<DefaultWalletDeletionException> {
            walletServiceMock.deleteWallet(userId = walletMock.userId!!, walletId = walletMock.id!!)
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `setDefaultWallet - must return the new default wallet of a certain user`() {
        val walletMock = Wallet.randomize()
        val setting = IS_DEFAULT
        val newDefaultWalletMock = walletMock.toggle(setting)

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every {
            walletRepositoryMock.findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(any())
        } returns null
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.saveAll(any<List<Wallet>>()) } returnsArgument 0

        walletServiceMock.setDefaultWallet(
            userId = walletMock.userId!!,
            walletId = walletMock.id!!
        ).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body
            responseEntity.body?.assertWalletUpdate(
                original = walletMock,
                updated = newDefaultWalletMock,
                WalletFields.isDefault
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(any()) }
        verify(exactly = 1) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 1) { walletRepositoryMock.saveAll(any<List<Wallet>>()) }
    }

    @Test
    fun `setDefaultWallet - must return the new default wallet of a certain user by replacing the old one`() {
        val walletMock = Wallet.randomize(isDefault = false)
        val currentDefaultWalletMock = Wallet.randomize(isDefault = true)
        val setting = IS_DEFAULT
        val newDefaultWalletMock = walletMock.toggle(setting)

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every {
            walletRepositoryMock.findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(any())
        } returns currentDefaultWalletMock
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.saveAll(any<List<Wallet>>()) } returnsArgument 0

        walletServiceMock.setDefaultWallet(
            userId = walletMock.userId!!,
            walletId = walletMock.id!!
        ).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.assertWalletUpdate(
                original = walletMock,
                updated = newDefaultWalletMock,
                setting.toWalletField()
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(any()) }
        verify(exactly = 2) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 1) { walletRepositoryMock.saveAll(any<List<Wallet>>()) }
    }

    @Test
    fun `setDefaultWallet - must throw WalletNotFoundException`() {
        val walletMock = Wallet.randomize()

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns null
        every {
            walletRepositoryMock.findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(any())
        } returns null
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.saveAll(any<List<Wallet>>()) } returnsArgument 0

        assertThrows<WalletNotFoundException> {
            walletServiceMock.setDefaultWallet(userId = walletMock.userId!!, walletId = walletMock.id!!)
        }

        verify(exactly = 0) { walletRepositoryMock.findFirstByUserIdAndIsDefaultIsTrueAndDeletedAtIsNull(any()) }
        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.saveAll(any<List<Wallet>>()) }
    }

    @Test
    fun `toggleActive - must return the wallet with toggled isActive attribute`() {
        val walletMock = Wallet.randomize()
        val setting = IS_ACTIVE
        val updatedWalletMock = walletMock.toggle(setting)

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns updatedWalletMock

        walletServiceMock.toggleActive(
            userId = walletMock.userId!!,
            walletId = walletMock.id!!
        ).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.assertWalletUpdate(
                original = walletMock,
                updated = updatedWalletMock,
                setting.toWalletField()
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 1) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `toggleActive - must throw WalletNotFoundException`() {
        val walletMock = Wallet.randomize()

        every { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any()) } returns null

        assertThrows<WalletNotFoundException> {
            walletServiceMock.toggleActive(userId = walletMock.userId!!, walletId = walletMock.id!!)
        }
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `toggleAcceptBankTransfer - must return the wallet with toggled acceptBankTransfer attribute`() {
        val walletMock = Wallet.randomize()
        val setting = ACCEPT_BANK_TRANSFER
        val updatedWalletMock = walletMock.toggle(setting)

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns updatedWalletMock

        walletServiceMock.toggleAcceptBankTransfer(
            userId = walletMock.userId!!,
            walletId = walletMock.id!!
        ).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.assertWalletUpdate(
                original = walletMock,
                updated = updatedWalletMock,
                setting.toWalletField()
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 1) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `toggleAcceptBankTransfer - must throw WalletNotFoundException`() {
        val walletMock = Wallet.randomize()

        every { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any()) } returns null

        assertThrows<WalletNotFoundException> {
            walletServiceMock.toggleAcceptBankTransfer(userId = walletMock.userId!!, walletId = walletMock.id!!)
        }
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `toggleAcceptPayments - must return the wallet with toggled acceptPayments attribute`() {
        val walletMock = Wallet.randomize()
        val setting = ACCEPT_PAYMENTS
        val updatedWalletMock = walletMock.toggle(setting)

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns updatedWalletMock

        walletServiceMock.toggleAcceptPayments(
            userId = walletMock.userId!!,
            walletId = walletMock.id!!
        ).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.assertWalletUpdate(
                original = walletMock,
                updated = updatedWalletMock,
                setting.toWalletField()
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 1) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `toggleAcceptPayments - must throw WalletNotFoundException`() {
        val walletMock = Wallet.randomize()

        every { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any()) } returns null

        assertThrows<WalletNotFoundException> {
            walletServiceMock.toggleAcceptPayments(userId = walletMock.userId!!, walletId = walletMock.id!!)
        }
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `toggleAcceptWithdrawing - must return the wallet with toggled acceptWithdrawing attribute`() {
        val walletMock = Wallet.randomize()
        val setting = ACCEPT_WITHDRAWING
        val updatedWalletMock = walletMock.toggle(setting)

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns updatedWalletMock

        walletServiceMock.toggleAcceptWithdrawing(
            userId = walletMock.userId!!,
            walletId = walletMock.id!!
        ).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.assertWalletUpdate(
                original = walletMock,
                updated = updatedWalletMock,
                setting.toWalletField()
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 1) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `toggleAcceptWithdrawing - must throw WalletNotFoundException`() {
        val walletMock = Wallet.randomize()

        every { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any()) } returns null
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        assertThrows<WalletNotFoundException> {
            walletServiceMock.toggleAcceptPayments(userId = walletMock.userId!!, walletId = walletMock.id!!)
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `toggleAcceptDeposit - must return the wallet with toggled acceptDeposit attribute`() {
        val walletMock = Wallet.randomize()
        val setting = ACCEPT_DEPOSIT
        val updatedWalletMock = walletMock.toggle(setting)

        every {
            walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any())
        } returns walletMock
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns updatedWalletMock

        walletServiceMock.toggleAcceptDeposit(
            userId = walletMock.userId!!,
            walletId = walletMock.id!!
        ).let { responseEntity ->
            assertEquals(HttpStatus.OK.value(), responseEntity.statusCodeValue)
            responseEntity.body?.assertWalletUpdate(
                original = walletMock,
                updated = updatedWalletMock,
                setting.toWalletField()
            )
        }

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 1) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 1) { walletRepositoryMock.save(any()) }
    }

    @Test
    fun `toggleAcceptDeposit - must throw WalletNotFoundException`() {
        val walletMock = Wallet.randomize()

        every { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(userId = any(), id = any()) } returns null

        assertThrows<WalletNotFoundException> {
            walletServiceMock.toggleAcceptPayments(userId = walletMock.userId!!, walletId = walletMock.id!!)
        }
        every { walletDispatcherMock.dispatch(any()) } returns Unit
        every { walletRepositoryMock.save(any()) } returns mockk()

        verify(exactly = 1) { walletRepositoryMock.findFirstByUserIdAndIdAndDeletedAtIsNull(any(), any()) }
        verify(exactly = 0) { walletDispatcherMock.dispatch(any()) }
        verify(exactly = 0) { walletRepositoryMock.save(any()) }
    }

    private fun WalletService.searchWalletsWithDefaults(
        userId: UUID = UUID.randomUUID(),
        surname: String? = null,
        page: Int = 0,
        size: Int = 20,
        sort: WalletFields = WalletFields.surname,
        direction: Direction = Direction.asc
    ) = this.searchWallets(
        userId = userId,
        surname = surname,
        page = page,
        size = size,
        sort = sort,
        direction = direction
    )

}