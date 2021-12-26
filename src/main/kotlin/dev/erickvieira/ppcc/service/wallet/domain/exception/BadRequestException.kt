package dev.erickvieira.ppcc.service.wallet.domain.exception

import dev.erickvieira.ppcc.service.wallet.web.api.model.ApiErrorType

open class BadRequestException(message: String, type: ApiErrorType) : BaseException(message, type)
