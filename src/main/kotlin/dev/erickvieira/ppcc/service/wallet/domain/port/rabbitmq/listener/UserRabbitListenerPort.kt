package dev.erickvieira.ppcc.service.wallet.domain.port.rabbitmq.listener

import com.rabbitmq.client.Channel
import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet
import dev.erickvieira.ppcc.service.wallet.domain.extension.defaultWallet
import dev.erickvieira.ppcc.service.wallet.domain.port.rabbitmq.dispatcher.WalletRabbitDispatcherPort
import dev.erickvieira.ppcc.service.wallet.domain.repository.WalletRepository
import dev.erickvieira.ppcc.service.wallet.extension.custom
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserRabbitListenerPort(
    private val walletRepository: WalletRepository,
    private val walletDispatcher: WalletRabbitDispatcherPort,
) {

    private val logger: Logger = LoggerFactory.getLogger(UserRabbitListenerPort::class.java)

    @RabbitListener(
        bindings = [QueueBinding(
            value = Queue(
                value = "\${ppcc.userqueue}",
                durable = "false"
            ),
            exchange = Exchange(
                value = "\${ppcc.userqueue}.exchange",
                durable = "false"
            ),
        )]
    )
    @RabbitHandler
    fun receive(@Payload message: String, channel: Channel, @Headers headers: Map<String, Any>) {
        try {
            logger.custom.info("fromRabbitMQ" to message)
            UUID.fromString(message).let { userId ->
                walletRepository.save(Wallet.defaultWallet(userId = userId)).let { createdWallet ->
                    walletDispatcher.dispatch(wallet = createdWallet)
                }
            }
            channel.basicAck(headers[AmqpHeaders.DELIVERY_TAG] as Long, false)
        } catch (e: Exception) {
            logger.custom.error(e.message)
        } catch (e: Error) {
            logger.custom.error(e.message)
        }
    }

}