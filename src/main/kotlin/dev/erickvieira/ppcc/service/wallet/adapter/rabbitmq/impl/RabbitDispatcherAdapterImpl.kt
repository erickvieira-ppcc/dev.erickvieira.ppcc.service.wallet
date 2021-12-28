package dev.erickvieira.ppcc.service.wallet.adapter.rabbitmq.impl

import dev.erickvieira.ppcc.service.wallet.adapter.rabbitmq.RabbitDispatcherAdapter
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component("dev.erickvieira.ppcc.adapter")
class RabbitDispatcherAdapterImpl(
    private val rabbitTemplate: RabbitTemplate
) : RabbitDispatcherAdapter {

    override fun dispatch(queue: String, message: String) = rabbitTemplate.convertAndSend(queue, message)

}