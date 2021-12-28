package dev.erickvieira.ppcc.service.wallet.config

import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RabbitMQConfig {

    @Value("\${ppcc.walletqueue}")
    private lateinit var walletQueueName: String

    @Value("\${ppcc.userqueue}")
    private lateinit var userQueueName: String

    @Bean
    open fun walletQueueName() = walletQueueName

    @Bean
    open fun userQueueName() = userQueueName

    @Bean
    open fun walletQueue() = Queue(walletQueueName, false)

    @Bean
    open fun userQueue() = Queue(userQueueName, false)

}