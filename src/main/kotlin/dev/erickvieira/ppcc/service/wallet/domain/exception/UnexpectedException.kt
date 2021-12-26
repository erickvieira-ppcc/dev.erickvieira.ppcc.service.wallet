package dev.erickvieira.ppcc.service.wallet.domain.exception

import dev.erickvieira.ppcc.service.wallet.web.api.model.ApiErrorType

open class UnexpectedException(message: String?) : BaseException(
    message = message ?: "Unexpected Error",
    type = ApiErrorType.unexpectedError
)
