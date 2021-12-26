package dev.erickvieira.ppcc.service.wallet.domain.exception

import dev.erickvieira.ppcc.service.wallet.web.api.model.ApiErrorType

open class ConflictException(message: String, type: ApiErrorType) : BaseException(message, type)
