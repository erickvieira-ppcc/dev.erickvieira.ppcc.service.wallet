package dev.erickvieira.ppcc.service.wallet.adapter.rabbitmq

interface RabbitDispatcherAdapter {

    fun dispatch(queue: String, message: String)

}