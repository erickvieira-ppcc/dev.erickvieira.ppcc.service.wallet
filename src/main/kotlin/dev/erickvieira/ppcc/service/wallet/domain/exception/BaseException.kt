package dev.erickvieira.ppcc.service.wallet.domain.exception

import dev.erickvieira.ppcc.service.wallet.web.api.model.ApiErrorType

open class BaseException constructor(
    @Transient override var message: String,
    val type: ApiErrorType? = null
) : RuntimeException()
