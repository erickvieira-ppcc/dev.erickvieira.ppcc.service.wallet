package dev.erickvieira.ppcc.service.wallet.domain.port.rabbitmq.dispatcher.impl

import com.google.gson.Gson
import dev.erickvieira.ppcc.service.wallet.adapter.rabbitmq.RabbitDispatcherAdapter
import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet
import dev.erickvieira.ppcc.service.wallet.domain.port.rabbitmq.dispatcher.WalletRabbitDispatcherPort
import org.springframework.stereotype.Component

@Component
class WalletRabbitDispatcherPortImpl(
    private val rabbitDispatcherAdapter: RabbitDispatcherAdapter,
    private val walletQueueName: String
) : WalletRabbitDispatcherPort {

    private val gson: Gson = Gson()

    override fun dispatch(wallet: Wallet) =
        rabbitDispatcherAdapter.dispatch(queue = walletQueueName, message = gson.toJson(wallet))

}