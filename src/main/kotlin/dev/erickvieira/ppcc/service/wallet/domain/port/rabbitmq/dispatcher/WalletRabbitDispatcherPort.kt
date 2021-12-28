package dev.erickvieira.ppcc.service.wallet.domain.port.rabbitmq.dispatcher

import dev.erickvieira.ppcc.service.wallet.domain.entity.Wallet

interface WalletRabbitDispatcherPort {

    fun dispatch(wallet: Wallet)

}