package dev.erickvieira.ppcc.service.wallet.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "ppcc")
data class PpccProperties @ConstructorBinding constructor(
    private val userQueue: String?,
    private val walletQueue: String?,
    private val bankingQueue: String?,
)