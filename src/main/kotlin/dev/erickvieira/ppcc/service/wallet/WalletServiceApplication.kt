package dev.erickvieira.ppcc.service.wallet

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.ComponentScan
import java.net.InetAddress
import java.net.UnknownHostException

@SpringBootApplication
@EnableEurekaClient
@ComponentScan(basePackages = ["dev.erickvieira.ppcc", "dev.erickvieira.ppcc.service.wallet"])
open class WalletServiceApplication

val logger: Logger = LoggerFactory.getLogger(WalletServiceApplication::class.java)

fun main(
    args: Array<String>
) = SpringApplication(WalletServiceApplication::class.java).run(*args).environment.let { env ->
    val baseUrl = (if (env.getProperty("server.ssl.key-store") != null) "https" else "http") +
            "://${
                try {
                    InetAddress.getLocalHost().hostAddress.takeUnless {
                        it.contains("127.0.1.1")
                    } ?: throw UnknownHostException()
                } catch (e: UnknownHostException) {
                    "localhost"
                }
            }:${env.getProperty("local.server.port")}"

    val contextPath = env.getProperty("server.servlet.context-path").let {
        if (it.isNullOrBlank()) "api/v1/wallet" else it
    }
    val swaggerUiPath = env.getProperty("springfox.documentation.swagger-ui.base-url") ?: "swagger-ui"

    logger.info(
        "The ${env.getProperty("spring.application.name")} is running over $baseUrl/$contextPath/. " +
                "You can read the API docs by accessing $baseUrl/$swaggerUiPath/."
    )
}
